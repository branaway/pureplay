package play.utils

/**
 * Scala utils
 */
trait Scala {
 

 /**
  * adding timeout handling and fixing resource closing for scala.io.Source.fromURL
  * @param String the url to read from
  * @param readTimeout
  * @param connectionTimeOut
  */

  def fromURLPath(url: String, readTimeout: Int = 5000, connectionTimeOut: Int = 3000):io.Source = {
    import io.Source.{fromInputStream,DefaultBufSize}
    val conn = new java.net.URL(url).openConnection()
    conn.setReadTimeout(readTimeout)
    conn.setConnectTimeout(connectionTimeOut)
    val inputStream = conn.getInputStream
    fromInputStream(inputStream)
   }


/**
 *  based on <a href="http://www.saager.org/2007/12/30/automatic-resource-management-blocks.html">this article</a>
 * <br><br>
 *  generic ARM block to support calls like
 * <pre>
 * for (conn &lt;- using (ds.getConnection)   { //do something with datasource }
 * </pre>
 * or a nested one
 * <pre>
 * for (outer <- using (new PipeStream())   {
 *  for (inner <- using (new PipeStream())   {
 * //do something with outer and inner
 * }
 * }
 * </pre>
 */
  /**
   * @param reasource that needs to be wrapped around
   */
  case class using[T <: {def close()}](resource: T) {
    /**
     * execute block in the proper scope
     */
    def foreach(f: T => Unit): Unit =
      try {
        f(resource)
      } finally {
        resource.close()
      }
  }


/**
 * based on <a href="http://stackoverflow.com/questions/1163393/best-scala-imitation-of-groovys-safe-dereference-operator">this article</a>
 * <br><br>
 * wrap chained and null method calls into an Option type
 * after importing this
 * <pre>
 * val whatsthis = ?(method.a.b.c) match   { case Some(s) =>s;case None=>"boooo" }
 * </pre>
 * @param block
 */
  def ?[T](block: => T): Option[T] =
    try {
      val memo = block
      if (memo == null) None
      else Some(memo)
    }
    catch {case e: NullPointerException => None}

}

object Scala extends Scala
