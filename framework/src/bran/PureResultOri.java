package bran;

import java.io.Serializable;

import play.mvc.Http;
import play.mvc.results.Result;

/**
 * similar to the {@link Result} but not {@link Exception} based, for better performance
 * 
 * @author Bing Ran<bing_ran@hotmail.com>
 *
 */
public abstract class PureResultOri implements Serializable{

	private static final long serialVersionUID = 7243166324870534970L;

	public abstract void apply(Http.Request request, Http.Response response);

	protected void setContentTypeIfNotSet(Http.Response response, String contentType) {
		response.setContentTypeIfNotSet(contentType);
	}

}
