package example.mapping

import play.api.libs.json._
import play.api.libs.functional.syntax._
import example.Api._

object AutoMapping extends App {
  implicit val residentReads = Json.reads[Resident]
  implicit val residentWrites = Json.writes[Resident]
  implicit val residentFormat = Json.format[Resident]

  val resident = Resident("Fiver", 4, role = None)

  val residentJson = Json.toJson(resident)

  println(residentJson)

  val jsonString: JsValue = Json.parse("""{
    "name" : "Fiver",
    "age" : 4
  }""")

  val residentFromJson = Json.fromJson(jsonString)

  residentFromJson match {
    case JsSuccess(value, _) => println(s"Resident name: ${value.name}")
    case JsError(errors)     => println(s"Errors: $errors")
  }

  // Value classes
  final class IdText(val id: String) extends AnyVal

  implicit val idTextReads = Json.valueReads[IdText]
  implicit val idTextWrites = Json.valueWrites[IdText]
  implicit val idTextFormaat = Json.valueFormat[IdText]

  val idTextJson = Json.toJson(new IdText("some-unique-id"))

  println(idTextJson)

  // Requirements
  // The macros work for classes and traits meeting the following requirements:
  // 1. It must have a companion object having apply and unapply methods.
  // 2. The return types of the unapply must match the argument types of the apply method.
  // 3. The parameter names of the apply method must be the same as the property names desired in the JSON.

  sealed trait Role
  case object Admin extends Role
  class Contributor(val organization: String) extends Role {
    override def equals(other: Any): Boolean = other match {
      case that: Contributor if other != null =>
        this.organization == that.organization
      case _ => false
    }

    override def toString(): String = s"Contributor(organization=$organization)"
  }

  object Contributor {
    def apply(organization: String): Contributor = new Contributor(organization)
    def unapply(contributor: Contributor): Option[(String)] = Some(
      contributor.organization
    )
  }

  // First provide instance for each sub-types 'Admin' and 'Contributor':
  implicit val adminF = OFormat[Admin.type](
    Reads[Admin.type] {
      case JsObject(_) => JsSuccess(Admin)
      case _           => JsError("Empty object expeted")
    },
    OWrites[Admin.type] { _ => Json.obj() }
  )

  implicit val contributorF = Json.format[Contributor]

  // Finally able to generate format for the sealed family 'Role'
  implicit val roleF = Json.format[Role]

  println(Json.toJson(Admin))
  println(Json.toJson(Contributor("github")))

  // Custom Naming Strategies
  // implicit val config = JsonConfiguration(JsonNaming.SnakeCase)

  val adminJson = Json.parse("""{ "roleType": "admin" }""")

  val contributorJson = Json.parse("""
  {
    "roleType":"contributor",
    "organization":"Foo"
  }""")

  // implicit val roleConfig = JsonConfiguration(
  // discriminator = "roleType"
  // )

  println(adminJson.validate[Admin.type])
  println(contributorJson.validate[Contributor])

  // Implementing your own Naming Strategy
  // To implement your own Naming Strategy you just need to implement the JsonNaming trait:
  object CustomNamingStrategy extends JsonNaming {
    def apply(property: String): String = s"custom_$property"
  }

  // implicit val customNamingConfig =
  //   JsonConfiguration(discriminator = "role", naming = CustomNamingStrategy)

  final case class PlayUser(name: String, role: Role)

  implicit val playUserF = Json.format[PlayUser]

  // TODO: how to resolve it?
  val playUserOneJson = Json.parse("""
  {
    "custom_name": "John",
    "custom_role": {
      "custom_role":"contributor",
      "organization":"Foo"
    }
  }""")

  println(playUserOneJson.validate[PlayUser])

  // Customize the macro to output null
  // Place before writes for get {"name":"Fiver","age":4,"role":null} other {"name":"Fiver","age":4}
  implicit val config =
    JsonConfiguration(optionHandlers = OptionHandlers.WritesNull)
  println(Json.toJson(resident))
}
