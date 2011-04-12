package bran;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import play.Invoker;
import play.Play;
import play.Play.Mode;
import play.mvc.Http.Request;
import play.server.NettyInvocationDirect;

/**
 * The object keeps a map with the topic string as the key and a list of session
 * ids as the value.
 * 
 * Map<topic, list<session id>>
 * 
 * The session id is the string from Session.getId() or its derived,
 * "sub-session/multiple browser window id" for example;
 * 
 * TODO: add no message timeout!
 * @author Bing Ran<bing_ran@hotmail.com>
 * 
 */
public class Subscriptions {
	public static final String MSGS = "_msgs";
	/**
	 * Map<topic, list<session id>> XXX: need to expire staled ones!
	 */
	ConcurrentHashMap<String, ConcurrentSkipListSet<SessionInfo>> registry = new ConcurrentHashMap<String, ConcurrentSkipListSet<SessionInfo>>();
	/**
	 * store the session id and the current invocation for the session
	 * 
	 * <sid, invocation
	 */
	ConcurrentHashMap<String, SessionInfo> sessionStore = new ConcurrentHashMap<String, SessionInfo>();
	/**
	 * <sid, list<Msg>>, user's mail box, empty after read
	 */
//	ConcurrentHashMap<String, BlockingQueue<Message>> sessionMessages = new ConcurrentHashMap<String, BlockingQueue<Message>>();

	// TODO: consider moving session messages to sessionStore
	// XXX eventually the inbox should be in some distributed place. 
	/**
	 * record the last time a session id is checked in, for use to invalidate
	 * sessions. not used.
	 */
	// ConcurrentHashMap<String, Long> sessionCheckinTime = new
	// ConcurrentHashMap<String, Long>();

	/**
	 * store messages to all topics. supposed to be persistent, as in DB, or
	 * redis etc. For history retrieval.
	 */
	ConcurrentHashMap<String, BlockingQueue<Message>> topicMessages = new ConcurrentHashMap<String, BlockingQueue<Message>>();

	/**
	 * executor to push a message to topics
	 */
	private ExecutorService pubExecutor;

	/**
	 * the executor that checks messages for new invocations
	 */
	private ExecutorService checkMsgExecutor;

	private static Subscriptions instance = new Subscriptions();

	Thread sessionCleaner;

	private Subscriptions() {
		int core = Integer.parseInt(Play.configuration.getProperty("play.pool",
				Play.mode == Mode.DEV ? "1" : ((Runtime.getRuntime().availableProcessors()) + "")));
		System.out.println("Number of threads to run event processor: " + core);
		pubExecutor = Executors.newFixedThreadPool(core); 
//			new CachedScheduledThreadPoolExecutor(core, new ThreadPoolExecutor.AbortPolicy());
		checkMsgExecutor = Executors.newFixedThreadPool(core); 
//			new ScheduledThreadPoolExecutor(core, new ThreadPoolExecutor.AbortPolicy());

		Runnable sessionCleaningRunner = new Runnable() {
			@Override
			public void run() {
				while (true) {
					Set<Entry<String, SessionInfo>> sessions = sessionStore.entrySet();
					Iterator<Entry<String, SessionInfo>> it = sessions.iterator();
					long now = System.currentTimeMillis();
					while (it.hasNext()) {
						Entry<String, SessionInfo> s = it.next();
						SessionInfo val = s.getValue();
						if (val.expired(now)) {
							it.remove();
							Set<String> topics = val.topics;
							for (String t : topics) {
//								ConcurrentSkipListSet<SessionInfo> topicSubs = getTopicSubs(t);
								ConcurrentSkipListSet<SessionInfo> topicSubs = registry.get(t);
								if (topicSubs != null)
									topicSubs.remove(val);
							}
						}
					}
					try {
						Thread.sleep(20000);
					} catch (InterruptedException e) {}
				}
			}
		};
		
		sessionCleaner = new Thread(sessionCleaningRunner, "subscription session cleaner");
		sessionCleaner.setDaemon(true);
		sessionCleaner.start();
	}

	public static Subscriptions instance() {
		return instance;
	}

	/**
	 * 1. update the session id expiration 2. ask the mail check to check
	 * messages 3. start listening.
	 * 
	 * TODO: find some previous messages for the topic in the topic queue.
	 * 
	 * @param topic
	 * @param sessionId
	 *            kind of token id, not really the web session id
	 */
	public void subscribe(final String topic, final String sid,NettyInvocationDirect invoke) {
		SessionInfo si = getSessionInfo(sid);
		si.addInterest(topic);
		ConcurrentSkipListSet<SessionInfo> subs = getTopicSubs(topic);
		subs.add(si);
		checkMessages(si, invoke);
	}

	/**
	 * called after sub
	 * 
	 * @param sid
	 * @param invoke
	 */
	protected void checkMessages(final SessionInfo si,NettyInvocationDirect invoke) {
		si.checkin(invoke);
		checkMsgExecutor.submit(new CheckMail(si));
	}

	public void checkMessages(String sid, NettyInvocationDirect invoke) {
		checkMessages(getSessionInfo(sid), invoke);
	}
	
	class CheckMail implements Runnable {
		SessionInfo sess;

		public CheckMail(SessionInfo sess) {
			this.sess = sess;
		}

		@Override
		public void run() {
			BlockingQueue<Message> inbox = sess.inbox;
			if (inbox.isEmpty()) {
				return;
			} else {
				NettyInvocationDirect inv = sess.checkout();
				if (inv != null) {
					// if the client has disconnected
					if (!inv.getCtx().getChannel().isConnected()) {
						try {
							inv.getCtx().getChannel().close();
						} catch (Throwable e) {
						}
						return;
					} else {
						List<Message> msgs = new ArrayList<Message>();
						inbox.drainTo(msgs);

						Message[] _msgs = new Message[msgs.size()];
						msgs.toArray(_msgs);
						// append the new one at the end
						Request request = inv.getRequest();
						request.args.put("_msgs", _msgs);
						// queue it to run.
						Invoker.executor.submit(inv);
					}
				}
			}
		}
	}

	public void unSubscribe(String topic, String sessionId) {
		ConcurrentSkipListSet<SessionInfo> subs = registry.get(topic);
		if (subs != null) {
			subs.remove(getSessionInfo(sessionId));
		}
		sessionStore.remove(sessionId);
	}

//	private void checkin(String sessionId,StaticActionNettyInvocation invoke) {
//		SessionInfo ih = getSessionInfo(sessionId);
//		ih.checkin(invoke);
//	}

	/**
	 * @param sessionId
	 * @return
	 */
	private SessionInfo getSessionInfo(String sessionId) {
		SessionInfo ih = sessionStore.get(sessionId);
		if (ih == null) {
			ih = new SessionInfo(sessionId);
			SessionInfo putIfAbsent = sessionStore.putIfAbsent(sessionId, ih);
			ih = putIfAbsent == null? ih : putIfAbsent;
		}
		return ih;
	}

	public void clearInvocation(String sessionId) {
		sessionStore.remove(sessionId);
	}

	/**
	 * 1. put in the msg in the topic queue 2. wake up the invocation 3.
	 * 
	 * @param topic
	 * @param msg
	 */
	public void publish(final String topic, final Message msg) {
		// pass it to another worker thread to do the job.
		class Notifier implements Runnable {

			@Override
			public void run() {
				// append to the topic's message queue;
				BlockingQueue<Message> list = getMessagesOfTopic(topic);
				list.add(msg);

				ConcurrentSkipListSet<SessionInfo> sis = getTopicSubs(topic);
				
				for (SessionInfo si : sis) {
					// get any previous messages
					NettyInvocationDirect inv = si.checkout();
					if (inv != null) {
						// if the client has disconnected
						if (!inv.getCtx().getChannel().isConnected()) {
							try {
								inv.getCtx().getChannel().close();
							} catch (Throwable e) {
							}
							return;
						} else {
							BlockingQueue<Message> inbox = si.inbox;	
							List<Message> msgs = new ArrayList<Message>();
							inbox.drainTo(msgs);
							msgs.add(msg);

							Message[] _msgs = new Message[msgs.size()];
							msgs.toArray(_msgs);
							// append the new one at the end
							Request request = inv.getRequest();
							request.args.put(MSGS, _msgs);
							// queue it to run.
							Invoker.executor.submit(inv);
						}
					} else {
						// OK the invocation has not come yet. let's queue it up
						// in the inbox for future consumption
						si.queueMsg(msg);
					}
				}
			}

		}

		pubExecutor.submit(new Notifier());
	}
	
	/**
	 * @param topic
	 * @return
	 */
	private ConcurrentSkipListSet<SessionInfo> getTopicSubs(final String topic) {
		ConcurrentSkipListSet<SessionInfo> sids = registry.get(topic);
		if(sids == null) {
			sids = new ConcurrentSkipListSet<SessionInfo>();
			ConcurrentSkipListSet<SessionInfo> putIfAbsent = registry.putIfAbsent(topic, sids);
			sids = putIfAbsent == null? sids : putIfAbsent;
		}
		return sids;
	}

	/**
	 * @param topic
	 * @return
	 */
	private BlockingQueue<Message> getMessagesOfTopic(final String topic) {
		BlockingQueue<Message> list = topicMessages.get(topic);
		if (list == null) {
			list = new LinkedBlockingQueue<Message>();
			BlockingQueue<Message> putIfAbsent = topicMessages.putIfAbsent(topic, list);
			list = putIfAbsent == null? list : putIfAbsent; 
		}
		return list;
	}

}