package example.basic

import play.api.libs.json._

object Traversing extends App {
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

  // Simple path \
  val lat = (placeJson \ "location" \ "lat").get
  println(lat)
  val bigwig = (placeJson \ "residents" \ 1).get
  println(bigwig)

  // Recursive path \\
  val names = placeJson \\ "name"
  println(names)

  // Direct lookup
  val name = placeJson("name")
  println(name)
  val fiver = placeJson("residents")(0)
  println(fiver)
}
