package com.sirma.itt.cmf.services.impl;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.definitions.CaseDefinition;
import com.sirma.itt.cmf.services.ws.ApplicationAdministration;
import com.sirma.itt.cmf.services.ws.impl.ApplicationAdministrationWS;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;

/**
 * Test definition loading and reloading. TODO implement it
 */
public class CmfDefinitionsCITest extends BaseArquillianCITest {

	@Inject
	private ApplicationAdministration applicationAdministration;

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " +  CmfDefinitionsCITest.class);
		return defaultBuilder(new CmfTestResourcePackager()).addClasess(
				ApplicationAdministrationWS.class).packageWar();
	}

	/**
	 * Test reload definitions.
	 */
	@Test(groups = "initialization")
	public void testReload() {

		System.setProperty(DEFINITIONS_RELOAD_FORCE, Boolean.TRUE.toString());
		// triger loading
		getDefintions(CaseDefinition.class);
		// stop forced loading
		System.setProperty(DEFINITIONS_RELOAD_FORCE, Boolean.FALSE.toString());

		String definition = applicationAdministration.getDefinition("caseDefinition",
				DEFAULT_DEFINITION_ID_CASE);

		assertTrue(definition != null && !definition.isEmpty(),
				"Definition should be loaded and be valid!");

	}

}
