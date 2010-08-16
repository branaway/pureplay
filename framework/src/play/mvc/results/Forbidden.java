package play.mvc.results;

import play.exceptions.UnexpectedException;
import play.libs.MimeTypes;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

/**
 * 403 Forbidden
 */
public class Forbidden extends Result {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Forbidden(String reason) {
        super(reason);
    }

    @Override
	public void apply(Request request, Response response) {
        response.status = 403;
        String format = request.format;
        if(request.isAjax() && "html".equals(format)) {
            format = "txt";
        }
        response.contentType = MimeTypes.getContentType("xx."+format);
//        Map<String, Object> binding = Scope.RenderArgs.current().data;
//        binding.put("result", this);
//        binding.put("session", Scope.Session.current());
//        binding.put("request", Http.Request.current());
//        binding.put("flash", Scope.Flash.current());
//        binding.put("params", Scope.Params.current());
//        binding.put("play", new Play());
//        String errorHtml = TemplateLoader.load("errors/403."+format).render(binding);
        String errorHtml = "Access forbidden: 403. " + getMessage() ;
        try {
            response.out.write(errorHtml.getBytes("utf-8"));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
    
}
