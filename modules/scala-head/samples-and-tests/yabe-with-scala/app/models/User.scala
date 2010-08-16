package models
 
import java.util._
import javax.persistence._
 
import play.db.jpa._
import play.data.validation._
 
@Entity
class User(

    @Email
    @Required
    var email: String,
    
    @Required
    var password: String,
    
    var fullname: String

) extends Model {
    var isAdmin = false
    
    override def toString() = email
 
}

object User extends QueryOn[User] {
    
    def connect(email: String, password: String) = {
        User.find("byEmailAndPassword", email, password).first
    }
    
}
