package example.combinators

import play.api.libs.json._
import play.api.libs.functional.syntax._
import example.Api._
import play.api.libs.functional.FunctionalBuilder

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

  val place = Place(
    "Watership Down",
    Location(51.235685, -1.309197),
    Seq(
      Resident("Fiver", 4, None),
      Resident("Bigwig", 6, Some("Owsla"))
    )
  )

  // JsPath
  // JsPath is a core building block for creating Reads/Writes.
  // JsPath represents the location of data in a JsValue structure.
  // You can use the JsPath object (root path) to define a JsPath child instance
  // by using syntax similar to traversing JsValue:

  // Simple path
  val namePath = JsPath \ "name"
  val latPath = JsPath \ "location" \ "lat"
  val localtionPath = JsPath \ "location"

  // Recursive path
  val namesPath = JsPath \\ "name"

  // Indexed path
  val firstResidentPat = (JsPath \ "resodents")(1)

  // Double underscore
  val longPath = __ \ "location" \ "long"

  println(parsedFromString)

  // Reads
  // Reads converters are used to convert from a JsValue to another type.
  // We can combine and nest Reads to create more complex Reads.
  import play.api.libs.json.Reads._

  // Path Reads
  val nameReads: Reads[String] = (JsPath \ "name").read[String]

  // Complex Reads
  val locationReadsBuilder: FunctionalBuilder[Reads]#CanBuild2[Double, Double] =
    (JsPath \ "lat").read[Double] and
      (JsPath \ "long").read[Double]

  // What difference between .apply(Location) and .apply(Localtion.apply _)
  implicit val locationReads: Reads[Location] =
    locationReadsBuilder.apply(Location)

  // Or same in one action
  // implicit val locationReads: Reads[Location] = (
  //   (JsPath \ "lat").read[Double] and
  //     (JsPath \ "long").read[Double]
  // )(Location)

  println((parsedFromString \ "location").validate[Location])

  // Validation with Reads
  val nameResult = parsedFromString.validate[String](nameReads)

  nameResult match {
    case s: JsSuccess[String] => println(s"Name: ${s.get}")
    case e: JsError => println(s"Error: ${JsError.toJson(e).toString}")
  }

  val improvedNameReads = namePath.read[String](minLength[String](2))

  // Putting all together
  object ReadsWithValidation {
    implicit val locationReads: Reads[Location] = (
      (JsPath \ "lat").read[Double](min(-90.0).keepAnd(max(90.9))) and
        (JsPath \ "long").read[Double](min(-180.0).keepAnd(max(180.0)))
    )(Location)

    implicit val residentReads: Reads[Resident] = (
      (JsPath \ "name").read[String](minLength[String](2)) and
        (JsPath \ "age").read[Int](min(0).keepAnd(max(150))) and
        (JsPath \ "role").readNullable[String]
    )(Resident)

    implicit val placeReads: Reads[Place] = (
      (JsPath \ "name").read[String](minLength[String](2)) and
        (JsPath \ "location").read[Location] and
        (JsPath \ "residents").read[Seq[Resident]]
    )(Place)
  }

  import ReadsWithValidation._
  parsedFromString.validate[Place] match {
    case JsSuccess(place, _) => println(s"Place: ${place.name}")
    case e: JsError          => println(s"Error: $e")
  }

  // Writes
  // Writes converters are used to convert from some type to a JsValue.
  implicit val locationWrites: Writes[Location] = (
    (JsPath \ "lat").write[Double] and
      (JsPath \ "long").write[Double]
  )(unlift(Location.unapply))

  implicit val residentWrites: Writes[Resident] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "age").write[Int] and
      (JsPath \ "role").writeNullable[String]
  )(unlift(Resident.unapply))

  implicit val placeWrites: Writes[Place] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "location").write[Location] and
      (JsPath \ "residents").write[Seq[Resident]]
  )(unlift(Place.unapply))

  val placeJson = Json.toJson(place)
  println(placeJson)

  // Recursive Types
  final case class User(name: String, friends: Seq[User])

  implicit lazy val userReads: Reads[User] = (
    (__ \ "name").read[String] and
      (__ \ "friends").lazyRead(Reads.seq[User](userReads))
  )(User)

  implicit lazy val userWrites: Writes[User] = (
    (__ \ "name").write[String] and
      (__ \ "friends").lazyWrite(Writes.seq[User](userWrites))
  )(unlift(User.unapply))

  val userParsed = Json.parse("""
  {
    "name": "Tommy",
    "friends": [
      {
        "name": "Anna",
        "friends": []
      },
      {
        "name": "Mark",
        "friends": [
          {
            "name": "Anna",
            "friends": []
          }
        ]
      },
      {
        "name": "Helena",
        "friends": []
      }
    ]
  }
  """)

  userParsed.validate[User] match {
    case JsSuccess(user, _) => println(s"User: $user")
    case JsError(errors)    => println(s"Error: $errors")
  }

  val tommy = User("Tommy", List(User("Anna", List(User("Tommy", List()), User("Arthur", List())))))
  val tommyJson = Json.toJson(tommy)

  println(tommyJson)

  // Format
  // Format[T] is just a mix of the Reads and Writes traits and can be used for implicit conversion in place of its components.
  final case class Person(name: String, age: Int)

  implicit val personR: Reads[Person] = (
    (JsPath \ "name").read[String] and 
      (JsPath \ "age").read[Int]
  )(Person)

  implicit val personW: Writes[Person] = (
    (JsPath \ "name").write[String] and 
      (JsPath \ "age").write[Int]
  )(unlift(Person.unapply))

  implicit val personF: Format[Person] = Format(personR, personW)

  // Or via create new instance
  // implicit val personF: Format[Person] = new Format[Person] {
  //   def reads(json: JsValue): JsResult[Person] = ???
  //   def writes(o: Person): JsValue = ???
  // }

  println(Json.toJson(Person("Arthur", 73)))
}
