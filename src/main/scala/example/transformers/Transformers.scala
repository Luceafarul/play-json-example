package example.transformers

import play.api.libs.json._

object Transformers extends App {
  val json = """
  {
    "key1" : "value1",
    "key2" : {
      "key21" : 123,
      "key22" : true,
      "key23" : [ "alpha", "beta", "gamma"],
      "key24" : {
        "key241" : 234.123,
        "key242" : "value242"
      }
    },
    "key3" : 234
  }"""

  val parsedJson = Json.parse(json)

  // Case 1: Pick JSON value in JsPath
  // Pick value as JsValue
  // jsPath.json.pick gets ONLY the value inside the JsPath
  val jsonPick = (__ \ 'key2 \ 'key23).json.pick

  println(parsedJson.transform(jsonPick))

  // Pick values as Type
  // jsPath.json.pick[T <: JsValue] extracts ONLY the typed value inside the JsPath
  val jsonPickType = (__ \ 'key2 \ 'key23).json.pick[JsArray]

  println(parsedJson.transform(jsonPickType))

  // Case 2: Pick branch following JsPath
  // jsPath.json.pickBranch extracts the single branch down to JsPath + the value inside JsPath
  val pickBranch = (__ \ 'key2 \ 'key24 \ 'key241).json.pickBranch

  println(parsedJson.transform(pickBranch))

  // Case 3: Copy a value from input JsPath into a new JsPath
  // jsPath.json.copyFrom(Reads[A <: JsValue]) reads value from input JSON and creates a new branch with result as leaf
  val copyJsPath =
    (__ \ 'key2 \ 'key251).json.copyFrom((__ \ 'key2 \ 'key21).json.pick)

  println(parsedJson.transform(copyJsPath))

  // Case 4: Copy full input Json & update a branch
  // jsPath.json.update(Reads[A <: JsValue]) only works for JsObject,
  // copies full input JsObject and updates jsPath with provided Reads[A <: JsValue]
  val copyAndUpdate = (__ \ 'key2 \ 'key24).json.update(__.read[JsObject].map {
    o => o ++ Json.obj("field243" -> "coucou")
  })

  println(parsedJson.transform(copyAndUpdate))

  // Case 5: Put a given value in a new branch
  // jsPath.json.put( a: => Jsvalue ) creates a new branch with a given value without taking into account input JSON
  val putValueInNewBranch = (__ \ 'key24 \ 'key241).json.put(JsNumber(456))

  println(parsedJson.transform(putValueInNewBranch))

  // Case 6: Prune a branch from input JSON
  // jsPath.json.prune only works with JsObject and removes given JsPath form input JSON)
  // Please note that:
  //   - prune doesn’t work for recursive JsPath for the time being
  //   - if prune doesn’t find any branch to delete, it doesn’t generate any error and returns unchanged JSON.
  val pruneBranch = (__ \ 'key2 \ 'key22).json.prune

  println(parsedJson.transform(pruneBranch))

  // Case 7: Pick a branch and update it's content in 2 places
  // Please note the result is just the __ \ 'key2 branch since we picked only this branch
  import play.api.libs.json.Reads._

  val pickBranchAndUpdate = (__ \ 'key2).json.pickBranch(
    (__ \ 'key21).json.update(
      of[JsNumber].map { case JsNumber(n) => JsNumber(n + 10) }
    ) andThen
      (__ \ 'key23).json.update(
        of[JsArray].map { case JsArray(arr) =>
          JsArray(arr :+ JsString("delta"))
        }
      )
  )

  println(parsedJson.transform(pickBranchAndUpdate))

  // Case 8: Pick a branch and prune a sub-branch
  // Please remark the result is just the __ \ 'key2 branch without key23 field.
  val pickBranchAndPruneSubBranch = (__ \ 'key2).json.pickBranch(
    (__ \ 'key23).json.prune
  )

  println(parsedJson.transform(pickBranchAndPruneSubBranch))

  // Case 9: Combinators
  val gizmo = Json.obj(
    "name" -> "gizmo",
    "description" -> Json.obj(
      "features" -> Json.arr("hairy", "cute", "gentle"),
      "size" -> 10,
      "sex" -> "undefined",
      "life_expectancy" -> "very old",
      "danger" -> Json.obj(
        "wet" -> "multiplies",
        "feed after midnight" -> "becomes gremlin"
      )
    ),
    "loves" -> "all"
  )

  val gremlin = Json.obj(
    "name" -> "gremlin",
    "description" -> Json.obj(
      "features" -> Json.arr("skinny", "ugly", "evil"),
      "size" -> 30,
      "sex" -> "undefined",
      "life_expectancy" -> "very old",
      "danger" -> "always"
    ),
    "hates" -> "all"
  )

  // How to transform gizmo into gremlin?
  import play.api.libs.functional.syntax._

  val gizmo2gremlin = (__).json
    .update((__ \ 'name).json.put(JsString("gremlin")))
    .andThen(
      (__ \ 'description).json
        .update(
          (__).json.update(
            (__ \ 'features).json
              .put(Json.arr("skinny", "ugly", "evil"))
              .and((__ \ 'size).json.update(of[JsNumber].map {
                case JsNumber(size) => JsNumber(size * 3)
              }))
              .and((__ \ 'danger).json.put(JsString("always")))
              .reduce
          )
        )
        .and((__ \ 'hates).json.copyFrom((__ \ 'loves).json.pick))
        .reduce
    )

  println(gizmo.transform(gizmo2gremlin))
}
