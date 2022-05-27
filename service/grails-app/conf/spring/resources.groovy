// Place your Spring DSL code here
import io.swagger.models.Swagger
import io.swagger.models.Info

beans = {

  apiInfo(Info) {
    title="MOD-OA FOLIO Open Access API"
    description="Manage OA Publication workflow."
    termsOfService="tbc"
    version="${grailsApplication.metadata.getApplicationVersion()}"
  }

  swagger(Swagger) {
    // see https://github.com/OAI/OpenAPI-Specification/blob/main/versions/2.0.md#schema
    // securityDefinitions = ["apiKey": new ApiKeyAuthDefinition("apiKey", In.HEADER)]
    // security = [new SecurityRequirement().requirement("apiKey")]
    info = apiInfo
  }

}
