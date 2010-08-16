package controllers

import play._
import play.mvc._

import se.scalablesolutions.akka.actor._
import se.scalablesolutions.akka.stm._
import se.scalablesolutions.akka.stm.Transaction._
import se.scalablesolutions.akka.remote._

/**
 * This example shows how akka remote actors work by implementing a hit counter that works across a cluster of servers.
 * This controller is preceded by the AkkaBootStrapJob which starts the remote node configured in application.conf, and
 * registers an actor that keeps track of hits.
**/
object ClusterHitCounter extends Controller {
    
    def index = {
        //here we fetch the clusterHitCounterActor
        val clusterHitCounterActor = RemoteClient.actorFor("cluster-hit-counter", "localhost", 9999)
        
        //next we pass it an Increment message using fire-and-forget semantics
        clusterHitCounterActor ! Increment
        
        //then we ask it for the current count, using send-and-receive-eventually semantics, and embed the result
        //in some h1 tags, which are rendered to the screen
        <h1>{(clusterHitCounterActor !! GetCount).getOrElse(0)}</h1>
    }
    
}