package controllers.java;

import java.util.Formatter;
import java.util.concurrent.atomic.AtomicInteger;

import play.mvc.Controller;
import scala.collection.immutable.List;
import se.scalablesolutions.akka.stm.Ref;
import se.scalablesolutions.akka.stm.local.Atomic;

/**
 * This example shows Software Transactional Memory (STM) in action.
 * The Controller has two hit counters, which are incremented every time someone visits the page.
 * One of the hit counters is kept in STM, while the other is not.  If enough hits are sent to the page,
 * the one without STM (dumbCounter) will see problems due to race conditions, while the other will not.
 * Check out the screencast for further discussion: http://vimeo.com/10764693
**/
public class HitCounter extends Controller {
    
    //the hit counter using STM
    final static Ref<Integer> ref = new Ref<Integer>(0);
//    final static AtomicInteger ref = new AtomicInteger(0);
    
    
    //hit counter that does not use STM
    static int dumbCount = 0;
    
    public static void index() {
        //bad way to implement a hit counter
        dumbCount = dumbCount + 1;
        
        //transaction wrapped around our STM hit counter
//        int count = ref.addAndGet(1);
        int count = new Atomic<Integer>() {
            public Integer atomically() {
                int inc = ref.get() + 1;
                ref.set(inc);
                return inc;
            }
        }.execute();
        
        StringBuilder sb = new StringBuilder();

        Formatter formatter = new Formatter(sb);

        formatter.format("<div>\n" + 
        		"            <h1>Count: %1$s</h1>\n" + 
        		"            <h1>Dumb Count: %2$s</h1>\n" + 
        		"        </div>", count, dumbCount);
        renderText(sb.toString());
    
    }
    
}