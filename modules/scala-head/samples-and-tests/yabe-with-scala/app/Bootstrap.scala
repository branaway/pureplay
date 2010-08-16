import play.jobs._
import play.test._
import models._
//this is necessary only if you need to interact with Models written in java
import play.db.jpa.QueryOn
 
@OnApplicationStart
class Bootstrap extends Job{
 
    override def doJob {
        //showing off the other API which is useful for existing java models *only*
        //for scala models you should relay on finder methods coming from the Model class
        // ie User.count == 0
        if(QueryOn[User].count == 0) {
            Fixtures load "initial-data.yml"
        }
    }
 
}
