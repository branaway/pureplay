package controllers 

import play._
import play.mvc._
import play.db.jpa._
import play.data.validation._
import play.libs._
import org.joda.time._
import play.utils.Scala._
import models._

object Application extends Controller with Secure {
    
    val before = new DateTime()
    var counter = 0
    
    private def nuller:String = null

    @Before
    def check {
        renderArgs += ("kiki" -> 9)
    }
    
    def reload = "My Name is, " + Apple1.name 

    
    def elvis() = {
      ?(nuller.length) match  { case Some(s) =>s;case None=>"boo" }
    }
    

    def urlcall() = {
      val res = fromURLPath("http://www.playframework.org/@api/play/Play.html").mkString
      if (res.contains("javadoc")) "yes" else "no"
    }


    private class Resource() {
      var wasCalled:Boolean = false
      def close() { wasCalled = true}
    }

    def arm() = {
      val resource = new Resource
      for (r <- using (resource)) {
        println(r)
      }
      resource.wasCalled.toString
    }

    def index() = {
        val numbers = List(1,2,3)
        numbers foreach { println _ }
        <h1>"Yep! ; " {Apple.name}</h1>  
    }
  
    def suspender() = {
     if (counter == 0) {
      counter = counter + 1
      suspend("2s")
     }
     val after = new DateTime()
     Seconds.secondsBetween(before,after).toStandardDuration.getStandardSeconds.toString
    }

    def json1 {
        val someJson = "{'name':'guillaume'}"
        renderJSON(someJson)
    }
    
    def json2 {
        val user = new User("guillaume@gmail.com", "12e", "Guillaume")
        renderJSON(user)
    }
    
    def simpleNameBinding {
        val name = "Yop"
        "Application/displayName.html".render(name)
    }
    
    def complexNameBinding {
        val name = "Yop"
        for (i <- 1 to 10) {
            name
        }
        "Application/displayName.html".render(name)
    }
    
    def test {        
        response <<< OK
        response <<< "text/plain"
        response <<< ("X-Test" -> "Yop")
        response <<< """|Hello World
                        |
                        |My Name is Guillaume""".stripMargin        
    }
    
    def test2 {        
        response <<< NOT_FOUND
        response <<< "text/html"
        response <<< <h1>Not found, sorry</h1>        
    }
    
    def anotherIndex(@Min(10) nimp: Int = 5, @Required name: String = "Guillaume") {
        println(nimp)
        println(request.path)
        val age = 59 
        var yop = 8
        yop = yop + 3
        println(name)
        
        info("Yop %d", 9)
        
        val users = asScala[User].find("byPassword", "88style").fetch
        
        response <<< OK
        response <<< "YOUHOUxxx" 
        response <<< "X-Yop" -> "hope"
        
        render(name, age, yop, users)
    }
    
    def addOne() {
        val user = new User("guillaume@gmail.com", "88style", "Guillaume")
        user.save()
        index()
    }
    
    def goJojo() {
        anotherIndex(name="Jojo") 
    }
    
    def api = renderXml(<items><item id="3">Yop</item></items>) 
    
    def yop = "@index".render() 
    
    def helloWorld = renderHtml(<h1>Hello world</h1>)
    
    def hello(name: String) = <h1>Hello { if(name != null) name else "Guest" }!</h1>
    
    def captcha = Images.captcha
    
    def reverseByName {
        print("Reverse...")
        val action = reverse(anotherIndex(19))
        val andUrl = reverse(anotherIndex(19)).url
        renderText(action.method + " " + action.url + " (" + andUrl + ")")
    }
    
}
