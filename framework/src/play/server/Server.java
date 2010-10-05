package play.server;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import play.Logger;
import play.Play;
import play.Play.Mode;

public class Server {
	static class MyThreadFactory implements ThreadFactory {
		static final AtomicInteger poolNumber = new AtomicInteger(1);
		final ThreadGroup group;
		final AtomicInteger threadNumber = new AtomicInteger(1);
		final String namePrefix;
		final String desc;
		
		MyThreadFactory(String desc) {
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "my netty pool-" + poolNumber.getAndIncrement() + "-" + desc + "-thread-";
			this.desc = desc;
		}

		@Override
		public Thread newThread(Runnable r) {
			int threadNum = threadNumber.getAndIncrement();
			String tname = namePrefix + threadNum;
			Thread t = new Thread(group, r, tname, 0);
			if (t.isDaemon())
				t.setDaemon(false);
			if (t.getPriority() != Thread.NORM_PRIORITY)
				t.setPriority(Thread.NORM_PRIORITY);
//			System.out.println("my thread factory created thread: " + tname);
			return t;
		}
	}

	public Server() {
		final Properties p = Play.configuration;
		int httpPort = Integer.parseInt(p.getProperty("http.port", "9000"));
		InetAddress address = null;
		if (System.getProperties().containsKey("http.port")) {
			httpPort = Integer.parseInt(System.getProperty("http.port"));
		}
		try {
			if (p.getProperty("http.address") != null) {
				address = InetAddress.getByName(p.getProperty("http.address"));
			}
			if (System.getProperties().containsKey("http.address")) {
				address = InetAddress.getByName(System.getProperty("http.address"));
			}
		} catch (Exception e) {
			Logger.error(e, "Could not understand http.address");
			System.exit(-1);
		}

		try {
		       int core = Integer.parseInt(Play.configuration.getProperty("play.pool", Play.mode == Mode.DEV ? "1" : ((Runtime.getRuntime().availableProcessors()+1) + "")));
		        System.out.println("Number of threads to run workers: " + core);
			
			ExecutorService bossExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), 
                    new MyThreadFactory("boss")
			);

			ExecutorService workerExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
					60L, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>(), 
					new MyThreadFactory("worker")
			);
			
//			ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
//					Executors.newCachedThreadPool()));
			
			// note: the netty server will use core * 2 number of worker thread and one boss thread. 
			// NioServerSocketChannelFactory has a constructor to configure the worker thread number. 
			ServerBootstrap bootstrap = new ServerBootstrap(
					new NioServerSocketChannelFactory(bossExecutor, workerExecutor, core));

			bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
			bootstrap.setOption("child.tcpNoDelay", true);
			// bran: some other options we may consider
			// bootstrap.setOption("child.receiveBufferSize", 1048576); // some
			// linux will auto-tune this
			// bootstrap.setOption("child.keepAlive", true);
			bootstrap.bind(new InetSocketAddress(address, httpPort));

//			// bran: create another instance binding to default port + 1
//			bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),
//					Executors.newCachedThreadPool()));
//			bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
//			bootstrap.setOption("child.tcpNoDelay", true);
//			// bran: some other options we may consider
//			// bootstrap.setOption("child.receiveBufferSize", 1048576); // some
//			// linux will auto-tune this
//			// bootstrap.setOption("child.keepAlive", true);
//			bootstrap.bind(new InetSocketAddress(address, httpPort + 1));
//
//			String ports = httpPort + " and " + (httpPort + 1);
			String ports = httpPort + "";

			if (Play.mode == Mode.DEV) {
				if (address == null) {
					Logger.info("Listening for HTTP on port %s (Waiting a first request to start) ...", ports);
				} else {
					Logger.info("Listening for HTTP at %2$s:%1$s (Waiting a first request to start) ...", ports, address);
				}
			} else {
				if (address == null) {
					Logger.info("Listening for HTTP on port %s ...", ports);
				} else {
					Logger.info("Listening for HTTP at %2$s:%1$s  ...", ports, address);
				}
			}
		} catch (ChannelException e) {
			Logger.error("Could not bind on port " + httpPort, e);
			System.exit(-1);
		}

	}

	public static void main(String[] args) throws Exception {
//		Map<String, String> env = System.getenv();
//		System.out.println("-- envs: ");
//		for (String k : env.keySet()) {
//			System.out.println(k + ":" + env.get(k));
//		}
//		
//		System.out.println("-- props: ");
//		Properties props = System.getProperties();
//		for (Object k : props.keySet()) {
//			System.out.println(k + ":" + props.get(k));
//		}
//		
		
		File root = new File(System.getProperty("application.path"));
		//
		Play.init(root, System.getProperty("play.id", ""));
		
		// if not precompile, start the Netty server
		if (System.getProperty("precompile") == null) {
			new Server();
		} else {
			Logger.info("Done.");
		}
	}
}
