package example.basic

import play.api.libs.json._
import example.Api._

object Writers {
  implicit val locationWrites: Writes[Location] = new Writes[Location] {
    def writes(o: Location): JsValue = Json.obj(
      "lat" -> o.lat,
      "long" -> o.long
    )
  }

  implicit val residentWrites: Writes[Resident] = new Writes[Resident] {
    def writes(o: Resident): JsValue = Json.obj(
      "name" -> o.name,
      "age" -> o.age,
      "role" -> o.role
    )
  }

  implicit val placeWrites: Writes[Place] = new Writes[Place] {
    def writes(o: Place): JsValue = Json.obj(
      "name" -> o.name,
      "location" -> o.location,
      "residents" -> o.residents
    )
  }
}
