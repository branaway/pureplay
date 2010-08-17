import play.test._

import org.junit._
import org.scalatest.junit._
import org.scalatest._
import org.scalatest.matchers._

import play.cache._

class CacheTests extends UnitTestCase with ShouldMatchersForJUnit {
    
    @Before def setUp = Cache.clear()
    
    @Test def useTheCacheAPI {     
           
        Cache.get[String]("yop") should be (None)    
        Cache.set("yop", "Coucou")
        Cache.get[String]("yop") should be (Some("Coucou")) 
        
        // Test wrong type (a warn log should be produced as well)
        Cache.get[Int]("yop") should be (None)  
        
        Cache.get[Seq[Int]]("coco") should be (None)
        Cache.get[Seq[Int]]("coco", "1s") {
            for(i <- 0 to 5) yield i
        } should be (Seq(0,1,2,3,4,5))
        Cache.get[String]("coco") should be (None)
        Cache.get[String](null) should be (None)
        Cache.get[Seq[Int]]("coco") should be (Some(Seq(0,1,2,3,4,5)))
        
        // Wait a moment
        Thread.sleep(1100)
        Cache.get[Seq[Int]]("coco") should be (None)
        
    }
    
}

