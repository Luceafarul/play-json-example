package example.combinators

import play.api.libs.json._
import example.Api._

object Combinators extends App {
  val parsedFromString: JsValue = Json.parse("""
  {
    "name" : "Watership Down",
    "location" : {
      "lat" : 51.235685,
      "long" : -1.309197
    },
    "residents" : [ {
      "name" : "Fiver",
      "age" : 4,
      "role" : null
    }, {
      "name" : "Bigwig",
      "age" : 6,
      "role" : "Owsla"
    } ]
  }
  """)

  // JsPath
  // JsPath is a core building block for creating Reads/Writes.
  // JsPath represents the location of data in a JsValue structure.
  // You can use the JsPath object (root path) to define a JsPath child instance
  // by using syntax similar to traversing JsValue:

  // Simple path
  val latPath = JsPath \ "location" \ "lat"

  // Recursive path
  val namesPath = JsPath \\ "name"

  // Indexed path
  val firstResidentPat = (JsPath \ "resodents")(1)

  // Double underscore
  val longPath = __ \ "location" \ "long"

  println(parsedFromString)
}
