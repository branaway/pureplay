package play.mvc

import results.RenderHtml
import scala.xml.NodeSeq
import scala.io.Source
import scala.collection.JavaConversions._

import java.io.InputStream
import java.util.concurrent.Future

import play.mvc.Http._
import play.mvc.Scope._
import play.data.validation.Validation
import play.classloading.enhancers.LocalvariablesNamesEnhancer.{LocalVariablesSupport, LocalVariablesNamesTracer}
import play.classloading.enhancers.ControllersEnhancer.ControllerSupport
import play.WithEscape


/**
 *
 * Represents a Scala based Controller
 */
private[mvc] abstract class ScalaController extends ControllerDelegate with LocalVariablesSupport with ControllerSupport {

  
  /**
   * implicit def to provider an easier way to render arguments 
   */
  implicit def richRenderArgs(x: RenderArgs) = new RichRenderArgs(x)

  /**
   * implicit def to provide some extra syntatic sugar while dealing with Response objects 
   */
  implicit def richResponse(x: Response) = new RichResponse(x)

  /**
   * implicit def to to provide some extra syntatic sugar while dealing with a sessions 
   */
  implicit def richSession(x: Session) = new RichSession(x)

  /**
   * implicit def to wrap a String as template name 
   */
  implicit def stringAsTemplate(x: String) = new StringAsTemplate(x)

  /**
   * implicit def to wrap response into an Option
   */
  implicit def optionToResults[T](x: Option[T]) = new OptionWithResults[T](x)

  /**
   * @returns a play request object
   */
  def request = Request.current()

  /**
   * @returns a play response object
   */
  def response = Response.current()

  /**
   * @returns a session object
   */
  def session = Session.current()

  /**
   * @returns a flash object
   */
  def flash = Flash.current()

  /**
   * @returns parameters
   */
  def params = Params.current()

  /**
   * @returns render argument object
   */
  def renderArgs = RenderArgs.current()

  /**
   * @returns Validation
   */
  def validation = Validation.current()

  /**
   * renders an xml node as xml
   * @param node xml node to be rendered
   */
  def renderXml(node: NodeSeq) {
      renderXml(node.toString)
  }
  
  /**
   * renders an xml node as html
   * @param node xml node to be rendered
   */
  def renderHtml(node: NodeSeq) {
      renderHtml(node.toString)
  }

  /**
   * renders content using the underlying templating language
   * @param args
   */
  def render(args: Any*) {
      renderTemplate(ScalaController.argsToParams(args: _*))
  }
  
  def reverse(action: => Any): play.mvc.Router.ActionDefinition = {
      val actionDefinition = reverse()
      action
      actionDefinition
  }
  
}

object ScalaController {
    
    def argsToParams(args: Any*) = {
        val params = new java.util.HashMap[String,AnyRef]
        for(o <- args) {
            o match {
                  case (name: String, value: Any) => params.put(name, value.asInstanceOf[AnyRef])
                  case _ => val names = LocalVariablesNamesTracer.getAllLocalVariableNames(o)
                            for (name <- names) {
                                params.put(name, o.asInstanceOf[AnyRef])
                            }
              }
        }
        params
    }
    
}
