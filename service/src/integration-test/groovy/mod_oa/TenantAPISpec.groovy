package mod_oa

import com.k_int.okapi.OkapiHeaders
import com.k_int.web.toolkit.testing.HttpSpec

import grails.testing.mixin.integration.Integration
import groovyx.net.http.FromServer
import spock.lang.*
import spock.util.concurrent.PollingConditions
import groovy.json.JsonOutput;

@Integration
@Stepwise
class TenantAPISpec extends HttpSpec {

  static final String tenantName = 'tenant_api_tests'
  static final Closure booleanResponder = {
    response.success { FromServer fs, Object body ->
      true
    }
    response.failure { FromServer fs, Object body ->
      false
    }
  }

  def setupSpec() {
    httpClientConfig = {
      client.clientCustomizer { HttpURLConnection conn ->
        conn.connectTimeout = 2000
        conn.readTimeout = 10000 // Need this for activating tenants
      }
    }
  }

  def setup() {
    setHeaders((OkapiHeaders.TENANT): tenantName)
  }

  void "Create Tenant" () {
    // Max time to wait is 10 seconds
    def conditions = new PollingConditions(timeout: 10)

    when: 'Create the tenant'
      boolean resp = doPost('/_/tenant', {
        parameters ([["key": "loadReference", "value": true]])
      }, null, booleanResponder)

    then: 'Response obtained'
      resp == true

    and: 'Refdata added'

      List list
      // Wait for the refdata to be loaded.
      conditions.eventually {
        (list = doGet('/oa/refdata')).size() > 0
      }
  }

  void "Disable Tenant" () {

    when: 'Purge the tenant'
      boolean resp = doPost('/_/tenant/disable', null, null, booleanResponder)

    then: 'Response obtained'
      resp == true
  }

  void "Re-enable tenant" () {
    // Max time to wait is 10 seconds
    def conditions = new PollingConditions(timeout: 10)

    when: 'Create the tenant'
      boolean resp = doPost('/_/tenant', {
        parameters ([["key": "loadReference", "value": true]])
      }, null, booleanResponder)

    then: 'Response obtained'
      resp == true

    and: 'Refdata added'

      List list
      // Wait for the refdata to be loaded.
      conditions.eventually {
        (list = doGet('/oa/refdata')).size() > 0
      }
  }

  void "Purge Tenant" () {

    when: 'Purge the tenant'
      boolean resp = doDelete('/_/tenant', null, booleanResponder)

    then: 'Response obtained'
      resp == true
  }

  void "Recreate tenant" () {
    // Max time to wait is 10 seconds
    def conditions = new PollingConditions(timeout: 10)

    when: 'Create the tenant'
      boolean resp = doPost('/_/tenant', {
        parameters ([["key": "loadReference", "value": true]])
      }, null, booleanResponder)

    then: 'Response obtained'
      resp == true

    and: 'Refdata added'

      List list
      // Wait for the refdata to be loaded.
      conditions.eventually {
        (list = doGet('/oa/refdata')).size() > 0
      }
  }

  void "Check OpenAPI endpoint"() {
    when:"We call the swagger api endpoint"
      def api_response = doGet("/oa/swagger/api");

    then:"Check the docs"
      log.debug("Got response to API GET message: ${api_response}");
      api_response != null
      File swagger_output = new File('mod_oa_openapi.json')
      if ( swagger_output.exists() )
        swagger_output.delete();
      swagger_output << JsonOutput.prettyPrint(JsonOutput.toJson(api_response))
  }

}

