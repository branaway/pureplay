package controllers

import play._
import play.mvc._
import play.libs._
import play.cache._
import play.data.validation._

import models._

trait Defaults extends Controller {
    
    @Before
    def setDefaults {
        renderArgs += "blogTitle" -> configuration("blog.title")
        renderArgs += "blogBaseline" -> configuration("blog.baseline") 
    }
    
}

object Application extends Controller with Defaults {
 
    def index() { 
        val frontPost = Post.find("order by postedAt desc").first 
        val olderPosts = Post.find("from Post order by postedAt desc").from(1).fetch
        render(frontPost, olderPosts)
    }
    
    def show(id: Long) { 
        val post = Post.findById(id)
        val randomID = Codec.UUID
        render(post, randomID)
    }
    
    def postComment(
        postId: Long, 
        @Required(message="Author is required") author: String, 
        @Required(message="A message is required") content: String, 
        @Required(message="Please type the code") code: String, 
        randomID: String
    ) {
        val post = Post.findById(postId)
        
        Play.id match {            
            case "test" => // skip validation
            case _ => validation.equals(code, Cache get randomID) message "Invalid code. Please type it again"
        }
        
        if(Validation.hasErrors) {
            render("@show", post, randomID)
        }
        
        post.addComment(author, content)        
        flash.success("Thanks for posting %s", author)
        
        show(postId)
    }
    def captcha(id: String) {
        val captchaInstance = Images.captcha
        val code = captchaInstance.getText("#E4EAFD")
        Cache.set(id, code, "30mn")
        renderBinary(captchaInstance)
    }
    
    def listTagged(tag: String) {
        val posts = Post findTaggedWith tag
        render(tag, posts);
    }
 
}
