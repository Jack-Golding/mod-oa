ackage mod_oa

import grails.rest.*
import grails.converters.*
import groovy.util.logging.Slf4j

/**
 * This class is a placeholder for OpenAPC OAI functionality and testing the ability for us to 
 * expose module endpoints without authentication
 */
class OpenAPCController {

  public index() {
    Map result = [
      'status':'OK'
    ]

    render result as JSON
  }

}
