/**
* overriding Model with Scala version
*/
package play { 
  package db {
    import jpa.ScalaModel

    package object jpa {
        type Model = ScalaModel
      }
    }
}
