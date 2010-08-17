import play._
import play.jobs._
import play.test._
import controllers.{Increment,GetCount}
import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.actor.Actor._
 
 /**
  * This class runs when the play! app starts.  All of the logic in this job is used to setup the ClusterHitCounter example.
 **/
@OnApplicationStart
class Bootstrap extends Job {
 
    override def doJob(): Unit = {
        import se.scalablesolutions.akka.remote.RemoteNode

        //when changes in code are detected, this job will run after recompilation.
        //this check makes sure that if the Node is already running, it won't get
        //started again
        if(!ClusterHitCounterActor.clusterStarted){
            ClusterHitCounterActor.clusterStarted = true
            
            //this starts a new remote node using the settings in the application.conf file under akka.remote.server
            //we are passing it the classloader that loaded this class, because it should be able to load all classes
            //we are using in this example.
            RemoteNode.start(this.getClass.getClassLoader)
            
            //here we are registering a new instance of ClusterHitCounter with our remote node, which will be looked up
            //by the ClusterHitCounter.index method (this is a controller, in the controllers directory).  This actor 
            //keeps track of and reports hits.  This is an example of Server Managed Remote Actors
            RemoteNode.register("cluster-hit-counter", actorOf(new ClusterHitCounterActor).start)
        }
    }
 
}

//only used to make sure that the RemoteNode will be started once and only once (only applicable during development)
object ClusterHitCounterActor{
    @volatile var clusterStarted = false
}

/**
 * This actor keeps track of hits
**/
class ClusterHitCounterActor extends Actor{
    
    //private member var to keep track of hits.
    private var count = 0
    
    
    //the case objects, Increment and GetCount, are defined in ClusterHitCounter.scala
    def receive = {
        case Increment => count = count + 1
        case GetCount => self.reply(count)
    }
    
}

