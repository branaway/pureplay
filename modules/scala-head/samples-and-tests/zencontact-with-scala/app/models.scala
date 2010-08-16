package models

import play.db.jpa._
import play.data.validation._

import java.util._
import javax.persistence._

// ~~~ Contact

@Entity class Contact(	
    @Required var firstname: String,	
    @Required var name: String,
    @Required var birthdate: Date,
    @Required @Email var email: String
) extends Model 

object Contact extends QueryOn[Contact] 


