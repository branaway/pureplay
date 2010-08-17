package controllers.java;

import play.mvc.Controller;
import se.scalablesolutions.akka.actor.UntypedActor;
import se.scalablesolutions.akka.actor.UntypedActorRef;

/**
 * This shows some rudimentary Actors stuff. Again this is a hit counter, but
 * here we're using an actor instead of STM.
 **/
public class ActorHitCounter extends Controller {

	// setting up an actor to keep track of hits - don't forget to start it!
	static UntypedActorRef hitCounter = UntypedActor.actorOf(HitCounterActor.class).start();

	public static void index() {

		// increment the hit counter using fire-and-forget semantics
		hitCounter.sendOneWay(new Increment());

		// fetch the count using send-and-receive-eventually semantics
		int count = 0;
		try {
			count = (Integer) hitCounter.sendRequestReply(new GetCount(), 1000);

		} catch (Exception e) {
			System.out.println(e);
		}

		renderText("count; " + count);
	}
}