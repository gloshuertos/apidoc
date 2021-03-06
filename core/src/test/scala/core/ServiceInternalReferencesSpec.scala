package core

import org.scalatest.{FunSpec, Matchers}

class ServiceInternalReferencesSpec extends FunSpec with Matchers {

  it("Validates circular reference") {
    val json = """
    {
      "base_url": "http://localhost:9000",
      "name": "svc-circular-reference",
      "models": {
        "foo": {
            "fields": [
                { "name": "bar", "type": "bar" }
            ]
        },
        "bar": {
            "fields": [
                { "name": "foo", "type": "foo" }
            ]
        }
      }
    }
    """

    val validator = TestHelper.serviceValidatorFromApiJson(json)
    validator.errors.mkString("") should be("")

    val barField = validator.service.models.find(_.name == "foo").get.fields.find(_.name == "bar").get
    barField.`type` should be("bar")

    val fooField = validator.service.models.find(_.name == "bar").get.fields.find(_.name == "foo").get
    fooField.`type` should be("foo")
  }

  it("Is able to parse self reference") {
    val json = """
    {
      "base_url": "http://localhost:9000",
      "name": "svc-reference",
      "models": {
        "user_variant": {
            "description": "variant set a user belongs to for a particular test.",
            "fields": [
                {
                    "name": "variant_key",
                    "type": "string"
                },
                {
                    "name": "parent",
                    "type": "user_variant",
                    "required": "false"
                }
            ]
        }
      }
    }
    """

    val validator = TestHelper.serviceValidatorFromApiJson(json)
    validator.errors.mkString("") should be("")
    val model = validator.service.models.find(_.name == "user_variant").get
    val parentField = model.fields.find(_.name == "parent").get
    parentField.`type` should be("user_variant")
  }

}
