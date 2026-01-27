package org.bahmni.module.elisFhirResultSupport;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;


public class ElisFhirResultSupportActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	
	@Override
	public void started() {
		log.info("Started ELIS FHIR Result Support Module");
	}
	
	
	@Override
	public void stopped() {
		log.info("Stopped ELIS FHIR Result Support Module");
	}
}
