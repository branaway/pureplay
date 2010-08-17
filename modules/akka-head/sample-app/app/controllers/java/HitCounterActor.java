package controllers.java;

import scala.Option;
import scala.PartialFunction;
import se.scalablesolutions.akka.actor.UntypedActor;

/**
 * This is the actor that acts as our hit counter. It receives two messages
 * defined just below. Increment means increment the count, and GetCount means
 * reply with the count
 **/
// case object Increment{}
// case object GetCount{}
public class HitCounterActor extends UntypedActor {

	// make your vars private!
	private int counter = 0;

	@Override
	public void onReceive(Object o) throws Exception {
		if (o instanceof Increment)
			counter++;
		else if (o instanceof GetCount)
			getContext().replySafe(counter);
	}
}