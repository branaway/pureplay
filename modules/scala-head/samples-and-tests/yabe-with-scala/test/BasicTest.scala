import java.util._
import play.test._
import models._

import scala.collection.JavaConversions._

import org.scalatest.{FlatSpec,BeforeAndAfterEach}
import org.scalatest.matchers.ShouldMatchers
import scala.collection.mutable.Stack

class BasicTest extends UnitTest with FlatSpec with ShouldMatchers with BeforeAndAfterEach {
    
    override def beforeEach() {
        Fixtures.deleteAll()
    }
 
    it should "create and retrieve a user" in {
        // Create a new user and save it
        new User("bob@gmail.com", "secret", "Bob").save()

        // Retrieve the user with bob username
        var bob = User.find("byEmail", "bob@gmail.com").first

        // Test 
        bob should not be (null)
        "Bob" should equal ( bob.fullname)
    }
    
    
    it should "call connect on User" in {
        // Create a new user and save it
        new User("bob@gmail.com", "secret", "Bob").save()

        // Test 
        User.connect("bob@gmail.com", "secret") should not be (null)
        User.connect("bob@gmail.com", "badpassword") should be (null)
        User.connect("tom@gmail.com", "secret") should be (null)
    }
    
    it should "create Post" in {
        // Create a new user and save it
        var bob = new User("bob@gmail.com", "secret", "Bob").save()

        // Create a new post
        new Post(bob, "My first post", "Hello world").save()

        // Test that the post has been created
        1 should equal ( Post.count())

        // Retrieve all post created by bob
        var bobPosts = Post.find("byAuthor", bob).fetch

        // Tests
        1 should equal ( bobPosts.size)
        var firstPost = bobPosts(0)
        firstPost should not be (null)
        bob should equal (firstPost.author)
        "My first post" should equal ( firstPost.title)
        "Hello world" should equal ( firstPost.content)
        firstPost.postedAt should not be (null)
    }
    
    it should "post Comments" in {
        // Create a new user and save it
        var bob = new User("bob@gmail.com", "secret", "Bob").save()

        // Create a new post
        var bobPost = new Post(bob, "My first post", "Hello world").save()

        // Post a first comment
        new Comment(bobPost, "Jeff", "Nice post").save()
        new Comment(bobPost, "Tom", "I knew that !").save()

        // Retrieve all comments
        var bobPostComments = Comment.find("byPost", bobPost).fetch()

        // Tests
        2 should equal ( bobPostComments.size)

        var firstComment = bobPostComments(0)
        firstComment should not be (null)
        "Jeff" should equal ( firstComment.author)
        "Nice post" should equal ( firstComment.content)
        firstComment.postedAt should not be (null)

        var secondComment = bobPostComments(1)
        secondComment should not be (null)
        "Tom" should equal ( secondComment.author)
        "I knew that !" should equal ( secondComment.content)
        secondComment.postedAt should not be (null)
    }
    
    it should "use the comments relation" in {
        // Create a new user and save it
        var bob = new User("bob@gmail.com", "secret", "Bob").save()

        // Create a new post
        var bobPost = new Post(bob, "My first post", "Hello world").save()

        // Post a first comment
        bobPost.addComment("Jeff", "Nice post")
        bobPost.addComment("Tom", "I knew that !")

        // Count things
        1 should equal (User.count())
        1 should equal (Post.count())
        2 should equal (Comment.count())

        // Retrieve the bob post
        bobPost = Post.find("byAuthor", bob).first
        bobPost should not be (null)

        // Navigate to comments
        2  should equal (bobPost.comments.size)
        "Jeff" should equal (bobPost.comments(0).author)

        // Delete the post
        bobPost.delete()

        // Chech the all comments have been deleted
        1 should equal (User.count())
        0 should equal (Post.count())
        0 should equal (Comment.count())
    }
    
    it should "work if things combined together" in {
        Fixtures.load("data.yml")

        // Count things
        2 should equal (User.count())
        3 should equal (Post.count())
        3 should equal (Comment.count())

        // Try to connect as users
        User.connect("bob@gmail.com", "secret") should not be (null)
        User.connect("jeff@gmail.com", "secret") should not be (null)
        User.connect("jeff@gmail.com", "badpassword") should be (null)
        User.connect("tom@gmail.com", "secret") should be (null)

        // Find all bob posts
        var bobPosts = Post.find("author.email", "bob@gmail.com").fetch
        2 should equal (bobPosts.size)

        // Find all comments related to bob posts
        var bobComments = Comment.find("post.author.email", "bob@gmail.com").fetch
        3 should equal (bobComments.size)

        // Find the most recent post
        var frontPost = Post.find("order by postedAt desc").first

        frontPost should not be (null)
        "About the model layer" should equal (frontPost.title)

        // Check that this post has two comments
        2 should equal (frontPost.comments.size)

        // Post a new comment
        frontPost.addComment("Jim", "Hello guys")
        3 should equal (frontPost.comments.size)
        4 should equal (Comment.count())
    }
    
    it should "be able to handle Tags" in {
        // Create a new user and save it
        var bob = new User("bob@gmail.com", "secret", "Bob").save()
        
        // Create a new post
        var bobPost = new Post(bob, "My first post", "Hello world").save()
        var anotherBobPost = new Post(bob, "My second post post", "Hello world").save()
        
        // Well
        0 should equal (Post.findTaggedWith("Red").size)
        
        // Tag it now
        bobPost.tagItWith("Red").tagItWith("Blue").save()
        anotherBobPost.tagItWith("Red").tagItWith("Green").save()
        
        Tag.findAll.size should equal (3)

        // Check
        2 should equal (Post.findTaggedWith("Red").size)        
        1 should equal (Post.findTaggedWith("Blue").size)
        1 should equal (Post.findTaggedWith("Green").size)
        
        1 should equal (Post.findTaggedWith("Red", "Blue").size)   
        1 should equal (Post.findTaggedWith("Red", "Green").size)   
        0 should equal (Post.findTaggedWith("Red", "Green", "Blue").size)  
        0 should equal (Post.findTaggedWith("Green", "Blue").size)    
        
        var cloud = Tag.cloud
        "List({tag=Red, pound=2}, {tag=Blue, pound=1}, {tag=Green, pound=1})" should equal (cloud.toString())
        
    }
 
}
