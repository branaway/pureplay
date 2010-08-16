package play.db.jpa

import javax.persistence.GeneratedValue;
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import java.lang.annotation.Annotation
import play.data.validation.Validation
import play.mvc.Scope.Params

/**
* this class wraps around the the basic JPA model implementation.
* it was really needed due to the differences on how java and scala are handling fluid APIs
*/
@MappedSuperclass
class ScalaModel extends JPABase {
 
  /**
  * holds entity managers
  */
  def em() = JPA.em()

  /**
  * refreshes current instance 
  * @return current type
  */
  def refresh(): this.type = {
    em().refresh(this) 
    this
  }
  
  /**
  * merges current instance
  * @return current type
  */
  def merge(): this.type = {
    em().merge(this)
    this
  }
  
  /**
  * saves current instance
  * @return current type
  */
  def save(): this.type = {
    _save() 
    this
  }

  /**
  * deletes current instance
  * @return current type
  */
  def delete(): this.type = {
    _delete() 
    this
  }
  

  /**
  * edit current instance, this is mainly used by CRUD. Apps are usually using save.
  * @name name
  * @params parameters
  * @return current type
  */
  def edit(name: String, params: java.util.Map[String,Array[String]]): this.type = {
     JPASupport.edit(this, name, params, Array[Annotation]())
     this
  } 

  /**
  * valides before saving
  * @return true if validation and saving were sucessfull, otherwise returns false 
  */

  def validateAndSave(): Boolean = {
    if (Validation.current().valid(this).ok) {
        _save()
        true
    } else false
  }

    @Id
    @GeneratedValue
    var id:Long=_

    def getId():Long = id

}
