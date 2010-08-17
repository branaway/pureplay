package xcontrollers

import controllers._
import play._
import play.mvc._
import play.data.validation._

import java.util._
import models._

trait Defaults extends Controller {
    
 @Before
 def setDefaults {
   renderArgs += "appTitle" -> configuration("app.title")
   renderArgs += "appBaseline" -> configuration("app.baseline")
   renderArgs += "email" -> session.get("username")
  }
  
}


/**
 * Created by IntelliJ IDEA.
 * User: Arnaud
 * Date: 22 mars 2010
 * Time: 18:53:36
 */
object Urls extends Defaults {
 def index = {
   render()
 }

 def form(id: Long) {
   //if (!Secure.Security.isConnected()) Secure.login
   val code = "4"
   //checkOwner(page)
   if (code != null) {
     var map = new java.util.HashMap[String, Object]
     map.put("url", code)
     renderArgs += "urlsite" -> Router.reverse("Pages.show", map).url
   }
   render(code)
 }

 def list {
   //val pages: Collection[Page] = user.pages
   val pages = "Page.all.fetch"
   render(pages)
 }

 def show(id: String) {
   val code = "Code findByCode id"
   render(code)
 }

 /*
  Save an url
 */
 def save(id: Long, @Required url: String) {
   Validation.hasErrors match {
     case true => if (request isAjax) error("Invalid Value") else render("@Application.index", url)
     case false =>
       var code = null
       if (id != 0) {
         // edit code
       } else {
         // new code
       }
       if (request.isAjax()) renderText(code)
       show(code)
   }
 }

 def delete(id: Long) {
   
 }


 private[xcontrollers] def getUser: User= {
   return renderArgs.get("user", classOf[User])
 }

 private[xcontrollers] def checkOwner(page: Any): Unit = {
 }
 
}