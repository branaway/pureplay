package play.mvc.results

import play.mvc.Http.{Request,Response}
import play.templates.Template
import play.exceptions.UnexpectedException
import play.libs.MimeTypes

private[play] class RenderHtml(content:String, contentType: String="text/html") extends Result {

 def apply(request:Request, response:Response) {
     try {
            setContentTypeIfNotSet(response, contentType)
            response.out.write(content.getBytes("utf-8"))
        } catch {
            case e:Exception => throw new UnexpectedException(e)
        }
  }
}
