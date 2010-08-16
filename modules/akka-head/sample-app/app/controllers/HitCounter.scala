package controllers

import play._
import play.mvc._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction._
import se.scalablesolutions.akka.stm.Transaction.Local._


/**
 * This example shows Software Transactional Memory (STM) in action.
 * The Controller has two hit counters, which are incremented every time someone visits the page.
 * One of the hit counters is kept in STM, while the other is not.  If enough hits are sent to the page,
 * the one without STM (dumbCounter) will see problems due to race conditions, while the other will not.
 * Check out the screencast for further discussion: http://vimeo.com/10764693
**/
object HitCounter extends Controller {
    
    //the hit counter using STM
    val ref = TransactionalRef[Int]
    
    //hit counter that does not use STM
    var dumbCount = 0
    
    def index = {
        //bad way to implement a hit counter
        dumbCount = dumbCount + 1
        
        //transaction wrapped around our STM hit counter
        val count = atomic{
            val i = ref.get.getOrElse(0) + 1
            ref.swap(i)
            i
        }
        
        //html displayed to the screen
        <div>
            <h1>Count: {count}</h1>
            <h1>Dumb Count: {dumbCount}</h1>
        </div>
    }
    
}