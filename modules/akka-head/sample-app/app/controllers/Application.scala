package controllers

import play._
import play.mvc._

object Application extends Controller {

    def index = 
    	<html>
    		<p>Welcome to the akka play module by <a href="http://twitter.com/dustinwhitney">@dustinwhitney</a></p>
    		<p><a href="/hitCounter">hit counter with transactional memory, in Scala</a></p>
    		<p><a href="/java.HitCounter/index">hit counter with transactional memory, in Java</a></p>
    		<p><a href="/actorHitCounter">hit counter with actor, in Scala</a></p>
    		<p><a href="/java.ActorHitCounter/index">hit counter with actor, in Java</a></p>
    	</html>
    
}