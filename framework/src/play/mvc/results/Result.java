package play.mvc.results;

import play.mvc.Http;

/**
 * Result support
 */
public abstract class Result extends RuntimeException {
    private static final long serialVersionUID = 4226472686642454212L;

    String description;
    
	public Result() {
    } 
    
    public Result(String description) {
        this.description = description;
    }
    
    public abstract void apply(Http.Request request, Http.Response response);
    
    protected void setContentTypeIfNotSet(Http.Response response, String contentType) {
       response.setContentTypeIfNotSet(contentType);
    }

//    // to save a few CPU cycles
//	@Override
//    public synchronized Throwable fillInStackTrace()
//    {
//		return this;
//    }
}
