
import models.Contact;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart 
public class Bootstrap extends Job {
//
    @Override 
    public void doJob() {
        if (Contact.count() == 0) 
        	Fixtures.load("data.yml");
    }

}