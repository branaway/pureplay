import org.junit._
import play.test._
import play.mvc._
import play.mvc.Http._
import models._
import play.test.FunctionalTest._

class RenderMethodsTest extends FunctionalTest {

    @Test
    def testFirstRenderJSON() {
        var response = GET("/application/json1")
        assertIsOk(response)
        assertContentType("application/json", response)
        assertCharset("utf-8", response)
        assertContentEquals("{'name':'guillaume'}", response)
    }
    
    @Test
    def testSecondRenderJSON() {
        var response = GET("/application/json2") 
        assertIsOk(response)
        assertContentType("application/json", response)
        assertCharset("utf-8", response)
        assertContentEquals("{\"isAdmin\":false,\"fullname\":\"Guillaume\",\"password\":\"12e\",\"email\":\"guillaume@gmail.com\",\"id\":0}", response)
    }

    @Test
    def testFirstRender() {
        var response = GET("/application/simpleNameBinding")
        assertIsOk(response)
        assertContentType("text/html", response)
        assertCharset("utf-8", response)
        assertContentEquals("<h1>Yop</h1>", response)
    }
    
    @Test
    def testSecondRender() {
        var response = GET("/application/complexNameBinding")
        assertIsOk(response)
        assertContentType("text/html", response)
        assertCharset("utf-8", response)
        assertContentEquals("<h1>Yop</h1>", response)
    }

}
