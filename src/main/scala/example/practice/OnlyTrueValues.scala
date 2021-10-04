package example.practice

import play.api.libs.json._

object OnlyTrueValues extends App {
  final case class Person(
      name: String,
      age: Int,
      personSettings: Person.PersonSettings
  )
  object Person {
    final case class PersonSettings(
        adult: Boolean = false,
        hasDriverLicense: Boolean = false
    )

    implicit val personSettingsF: OFormat[PersonSettings] =
      new OFormat[PersonSettings] {
        implicit val format: OFormat[PersonSettings] =
          Json.format[PersonSettings]

        def reads(json: JsValue): JsResult[PersonSettings] = format.reads(json)

        def writes(o: PersonSettings): JsObject =
          JsObject(format.writes(o).value.filter { case (_, value) =>
            value == JsBoolean(true)
          })
      }

    implicit val personF: OFormat[Person] = Json.format[Person]
  }

  import Person._

  val personOne = Person("Tom", 13, PersonSettings())
  val personTwo = Person("John", 21, PersonSettings(adult = true))
  val personThree =
    Person("Diana", 27, PersonSettings(adult = true, hasDriverLicense = true))

  println(Json.toJson(personOne))
  println(Json.toJson(personTwo))
  println(Json.toJson(personThree))
}
