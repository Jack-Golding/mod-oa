package mod_oa

import java.sql.ResultSet

import javax.sql.DataSource

import org.grails.datastore.mapping.core.exceptions.ConfigurationException
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.plugins.databasemigration.liquibase.GrailsLiquibase

import grails.core.GrailsApplication
import grails.events.annotation.Subscriber
import grails.gorm.multitenancy.Tenants
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import com.k_int.okapi.OkapiTenantAdminService
import com.k_int.web.toolkit.settings.AppSetting
import com.k_int.web.toolkit.refdata.*
import com.k_int.okapi.OkapiTenantResolver
import org.olf.oa.Party

/**
 * This service works at the module level, it's often called without a tenant context.
 */
public class HousekeepingService {

  BibReferenceService bibReferenceService

  /**
   * This is called by the eventing mechanism - There is no web request context
   * this method is called after the schema for a tenant is updated.
   */
  @Subscriber('okapi:schema_update')
  public void onSchemaUpdate(tn, tid) {
    log.debug("HousekeepingService::onSchemaUpdate(${tn},${tid})")
    setupData(tn, tid);
  }

  /**
   * Put calls to estabish any required reference data in here. This method MUST be communtative - IE repeated calls must leave the 
   * system in the same state. It will be called regularly throughout the lifecycle of a project. It is common to see calls to
   * lookupOrCreate, or "upsert" type functions in here."
   */
  private void setupData(tenantName, tenantId) {
    log.info("HousekeepingService::setupData(${tenantName},${tenantId})");
    // Establish a database session in the context of the activated tenant. You can use GORM domain classes inside the closure
    Tenants.withId(tenantId) {
      AppSetting.withNewTransaction { status ->
        AppSetting pubreq_id_prefix = AppSetting.findByKey('hrid_prefix') ?: new AppSetting(
                                  section:'PublicationRequests',
                                  settingType:'String',
                                  key: 'hrid_prefix',).save(flush:true, failOnError: true);

      }
    }
  }

  @Subscriber('okapi:tenant_load_sample')
  public void onTenantLoadSample(final String tenantId, 
                                 final String value, 
                                 final boolean existing_tenant, 
                                 final boolean upgrading, 
                                 final String toVersion, 
                                 final String fromVersion) {
    log.debug("HousekeepingService::onTenantLoadSample(${tenantId},${value},${existing_tenant},${upgrading},${toVersion},${fromVersion}");
    final String schemaName = OkapiTenantResolver.getTenantSchemaName(tenantId)
    Tenants.withId(schemaName) {
      try {
        def sample_journal_data_stream = this.class.classLoader.getResourceAsStream("dummy_journal_data.json")
        List<Map> sample_journal_data = new groovy.json.JsonSlurper().parse(sample_journal_data_stream)

        int num_sample_jounals = sample_journal_data.size();
        int ctr=0;
        AppSetting.withNewSession {
          sample_journal_data.each { desc ->
            log.debug("Import sample journal ${ctr++} of ${num_sample_jounals}");
            AppSetting.withNewTransaction { status ->
              bibReferenceService.importWorkAndInstances(desc)
            }
          }
        }

        def sample_party_data_stream = this.class.classLoader.getResourceAsStream("dummy_party_data.json")
        List<Map> sample_party_data = new groovy.json.JsonSlurper().parse(sample_party_data_stream)
        int num_sample_party_records = sample_party_data.size();
        ctr = 0;
        AppSetting.withNewSession {
          sample_party_data.each { pty ->
            log.debug("Import sample party ${ctr++} of ${num_sample_party_records}");
            AppSetting.withNewTransaction { status ->
              log.debug("Import party ${pty}");
              // Key the sample data off main email - so that repeated calls to loadReference will not repeat the data
              Party p = Party.findByMainEmail(pty.mainEmail) ?: new Party(
                                  'title': pty.title,
                                  'familyName': pty.familyName,
                                  'givenNames': pty.givenNames,
                                  'mainEmail': pty.mainEmail).save(flush:true, failOnError:true);
            }
          }
        }

      }
      catch ( Exception e) {
        log.error("Error in loadSample",e);
      }
      finally {
        log.debug("Complete onTenantLoadSample transaction");
      }
    }
  }

}
