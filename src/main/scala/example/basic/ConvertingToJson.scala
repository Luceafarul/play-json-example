package example.basic

import play.api.libs.json._

object ConvertingToJson extends App {

  // String parsing
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
  println(parsedFromString)

  // Class construction
  val fromClassConstuction: JsValue = JsObject(
    Seq(
      "name" -> JsString("Watership Down"),
      "location" -> JsObject(
        Seq("lat" -> JsNumber(51.235685), "long" -> JsNumber(-1.309197))
      ),
      "residents" -> JsArray(
        IndexedSeq(
          JsObject(
            Seq(
              "name" -> JsString("Fiver"),
              "age" -> JsNumber(4),
              "role" -> JsNull
            )
          ),
          JsObject(
            Seq(
              "name" -> JsString("Bigwig"),
              "age" -> JsNumber(6),
              "role" -> JsString("Owsla")
            )
          )
        )
      )
    )
  )
  println(fromClassConstuction)

  // With factory methods

  val withFactoryMethods: JsValue = Json.obj(
    "name" -> "Watership Down",
    "location" -> Json.obj("lat" -> 51.235685, "long" -> -1.309197),
    "residents" -> Json.arr(
      Json.obj(
        "name" -> "Fiver",
        "age" -> 4,
        "role" -> JsNull
      ),
      Json.obj(
        "name" -> "Bigwig",
        "age" -> 6,
        "role" -> "Owsla"
      )
    )
  )
  println(withFactoryMethods)

  // Writes converters
  // Writes[T] can convert a T to a JsValue
  // The Play JSON API provides implicit Writes for most basic types, such as Int, Double, String, and Boolean.
  // It also supports Writes for collections of any type T that a Writes[T] exists.

  // basic types
  val jsonString = Json.toJson("Fiver")
  val jsonNumber = Json.toJson(4)
  val jsonBoolean = Json.toJson(false)
  println(jsonString)
  println(jsonNumber)
  println(jsonBoolean)

  // collections of basic types
  val jsonArrayOfInts = Json.toJson(Seq(1, 2, 3, 4))
  val jsonArrayOfStrings = Json.toJson(List("Fiver", "Bigwig"))
  println(jsonArrayOfInts)
  println(jsonArrayOfStrings)

  // To convert your own models to JsValues, you must define implicit Writes converters and provide them in scope.
  import example.Api._
  import Writers._

  val place = Place(
    "Watership Down",
    Location(51.235685, -1.309197),
    Seq(
      Resident("Fiver", 4, None),
      Resident("Bigwig", 6, Some("Owsla"))
    )
  )

  val placeJson = Json.toJson(place)
  println(placeJson)
}
