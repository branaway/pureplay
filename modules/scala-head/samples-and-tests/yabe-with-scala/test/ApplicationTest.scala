import org.junit._
import play.test._
import play.mvc._
import play.mvc.Http._
import models._
import play.test.FunctionalTest._

class ApplicationTest extends FunctionalTest {

    @Test
    def testThatIndexPageWorks() {
        var response = GET("/")
        assertIsOk(response)
        assertContentType("text/html", response)
        assertCharset("utf-8", response)
    }
    
    @Test
    def testAdminSecurity() {
        var response = GET("/admin")
        assertStatus(302, response)
        assertHeaderEquals("Location", "http://localhost/login", response)
    }
    
}
