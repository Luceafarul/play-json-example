package example.basic

import play.api.libs.json._

object ConvertingFromJson extends App {
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

  val minifiedString = Json.stringify(placeJson)
  val readableString = Json.prettyPrint(placeJson)

  println(minifiedString)
  println(readableString)

  // Using JsValue.as/asOpt
  // The simplest way to convert a JsValue to another type is using JsValue.as[T](implicit fjs: Reads[T]): T.
  // This requires an implicit converter of type Reads[T] to convert a JsValue to T (the inverse of Writes[T]).
  // As with Writes, the JSON API provides Reads for basic types.
  val name = (placeJson \ "name").as[String]
  val names = (placeJson \\ "name").map(_.as[String])
  println(name)
  println(names)

  val nameOption = (placeJson \ "name").asOpt[String]
  val bogusOption = (placeJson \ "bogus").asOpt[String]
  println(nameOption)
  println(bogusOption)
}
