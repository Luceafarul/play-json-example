package example.basic

import play.api.libs.json._
import play.api.libs.functional.syntax._
import example.Api._

object Readers {
  implicit val locationReads: Reads[Location] = (
    (JsPath \ "lat").read[Double] and (JsPath \ "long").read[Double]
  )(Location.apply _)

  implicit val residentReads: Reads[Resident] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "age").read[Int] and
      (JsPath \ "role").readNullable[String]
  )(Resident.apply _)

  implicit val placeReads: Reads[Place] = (
    (JsPath \ "name").read[String] and
      (JsPath \ "location").read[Location] and
      (JsPath \ "residents").read[Seq[Resident]]
  )(Place.apply _)
}
