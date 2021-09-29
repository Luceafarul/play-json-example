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

  // Using validation
  // The preferred way to convert from a JsValue to another type
  // is by using its validate method (which takes an argument of type Reads).
  val nameResult = (placeJson \ "name").validate[String]
  val errorResult = (placeJson \ "fail").validate[String]

  nameResult match {
    case JsSuccess(value, path) => println(s"Name: $value, by path: $path}")
    case JsError(errors)        => println(s"Error: $errors")
  }
  errorResult match {
    case JsSuccess(value, path) => println(s"Name: $value, by path: $path")
    case JsError(errors)        => println(s"Error: $errors")
  }

  val nameOrFallback = nameResult.getOrElse("Undefined")

  val nameUpperResult = nameResult.map(name => name.toUpperCase())

  val nameOptionFold = nameResult.fold(
    invalid = { fieldErrors =>
      fieldErrors.foreach { case (field, error) =>
        println(s"field: $field, errors: $error")
      }
      Option.empty[String]
    },
    valid = Some(_)
  )
  println(name)

  // JsValue to a model
  // To convert from JsValue to a model, you must define implicit Reads[T] where T is the type of your model.
  import Readers._

  val residentResult: JsResult[Resident] =
    (placeJson \ "residents")(1).validate[Resident]
  val placeResult: JsResult[Place] =
    placeJson.validate[Place]

  println(residentResult)
  println(placeResult)
}
