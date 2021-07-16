package mod_oa

class UrlMappings {

  static mappings = {
    "/"(controller: 'application', action:'index');

    '/remotesync/refdata'(resources: 'refdata') {
      collection {
        "/$domain/$property" (controller: 'refdata', action: 'lookup', method: 'GET')
      }
    }
    "/oa/scholarlyWork" (resources: "scholarlyWork")

  }
}
