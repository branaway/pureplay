package play.db.jpa
import scala.collection.mutable
import JPQL.{instance => i}

/**
*  provides a mini DSL for Model objects
**/
trait QueryOn[T] {
  type M[T] = Manifest[T]
  implicit private def manifest2entity[T](m: M[T]): String = m.erasure.getName()

  /**
  * @return number of records
  **/
  def count()(implicit m: M[T]) = i.count(m)
  
  /**
  * count using a query
  * @ps Array of params
  * @query query
  * @return number of records
  **/

  def count(q: String, ps: AnyRef*)(implicit m: M[T]) = i.count(m, q, ps.toArray)
  
  /**
  * return all records
  */
  def findAll(implicit m: M[T]) = i.all(m).fetch[T]
  
  /**
  * find records by Id
  * @id id
  * @return instance for the given id
  */
  def findById(id: Any)(implicit m: M[T]) = i.findById(m, id).asInstanceOf[T]
  
  /**
  * find a record based on a query
  * @q query
  * @ps Array of params
  * @return a record based on the query and parameters
  */
  def findBy(q: String, ps: AnyRef*)(implicit m: M[T]) = i.findBy(m, q, ps.toArray)
  
  /**
  * this is the most generic finder which is also chainable (ie fetch, all, first etc. can be called on 
  * the return type)  
  * @q query
  * @ps parameters
  * @return @see ScalaQuery
  */
  def find(q: String, ps: AnyRef*)(implicit m: M[T]) = new ScalaQuery[T](i.find(m, q, ps.toArray))
  
  /**
  * generic finder method that can be used with parameter bindings
  * @q query
  * @params parameters for bindings
  * @return ScalaQuery
  */
  def find(q: String, params: Map[String, Any])(implicit m: M[T]): ScalaQuery[T] = {
      val query = find(q)
      params.foreach { case (name, param) => query.bind(name, param)}
      query
  }

  /**
  * returns the wrapper object for all records
  */
  def all(implicit m: M[T]) = i.all(m)
  
  /**
  * deletes records based on thequery and parameters
  * @q query
  * @ps array of parameters
  */
  def delete(q: String, ps: AnyRef*)(implicit m: M[T]) = i.delete(m, q, ps.toArray)
 
  /**
  * deletes all records
  */
  def deleteAll(implicit m: M[T]) = i.deleteAll(m)
  
  /**
  * find a specific record based on a certain criteria
  * @q query
  * @ps array of params
  * @return T where T is the type of model
  */
  def findOneBy(q: String, ps: AnyRef*)(implicit m: M[T]): T = i.findOneBy(m, q, ps.toArray).asInstanceOf[T]

  /**
  * creates record for the given type T
  * @name name
  * @ps play scoped parameters 
  * @return T where T is the type of the current model
  */
  def create(name: String, ps: play.mvc.Scope.Params)(implicit m: M[T]): T = i.create(m, name, ps).asInstanceOf[T]
}

/**
* provides Query functionality via companion object
* it also caches QueryHolders
**/
object QueryOn {
  private var queries = new mutable.HashMap[String, QueryHolder[_]]
  private def memoize[T](key:String ):QueryHolder[T] = {
     if (!queries.contains(key)) {
       queries += (key) -> (new QueryHolder[T])
     }
     queries(key).asInstanceOf[QueryHolder[T]]
  }
  def apply[T](implicit m: scala.reflect.Manifest[T]):QueryHolder[T] = memoize[T](m.toString)
}


private[jpa] class QueryHolder[T] extends QueryOn[T]

private[jpa] class ScalaQuery[T](val query: JPASupport.JPAQuery) {

    //() needed only for java API compatibility
    def first() = query.first().asInstanceOf[T]
    def fetch() = asList[T](query.fetch())
    def all() = fetch()
    def fetch(size: Int) = asList[T](query.fetch(size))
    def from(offset: Int) = {
        query.from(offset)
        this
    }

    def bind(name: String, param: Any) = query.bind(name, param)

    private def asList[T](jlist: java.util.List[T]): List[T] = {
        import scala.collection.mutable.ListBuffer
        val buffer = ListBuffer[T]()
        for(e <- jlist.toArray) {
            buffer += e.asInstanceOf[T]
        }
        buffer.toList
    }
}
