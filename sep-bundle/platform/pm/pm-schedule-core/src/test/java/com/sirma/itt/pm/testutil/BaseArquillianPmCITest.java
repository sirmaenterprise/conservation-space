package com.sirma.itt.pm.testutil;

import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.services.adapters.CodelistServerAccessorMock;
import com.sirma.itt.cmf.services.mock.CodelistServiceProviderMock;
import com.sirma.itt.cmf.services.mock.GroupServiceImplMock;
import com.sirma.itt.cmf.services.mock.LinkProviderServiceMock;
import com.sirma.itt.cmf.services.mock.MailNotificationServiceMock;
import com.sirma.itt.cmf.services.mock.PeopleServiceImplMock;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.cmf.testutil.JarPackages;
import com.sirma.itt.cmf.testutil.ModulesInfo;
import com.sirma.itt.cmf.testutil.ResourceImporter;
import com.sirma.itt.cmf.testutil.TestPackageBuilder;
import com.sirma.itt.cmf.testutil.TestResouceBuilderWrapper;
import com.sirma.itt.cmf.testutil.TestableJarModules;
import com.sirma.itt.cmf.testutil.TestableWarModules;
import com.sirma.itt.cmf.testutil.WarPackages;
import com.sirma.itt.emf.security.AuthenticationServiceMock;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;

/**
 * The BaseArquillianPmCITest is helper class that capsulate pm related testing
 *
 * @author bbanchev
 */
public class BaseArquillianPmCITest extends BaseArquillianCITest {
	protected static final String DEFAULT_DEFINITION_ID_PROJECT = "GEP10001";

	/**
	 * Inits the builder.
	 *
	 * @param builder
	 *            the builder
	 * @return the test resouce builder wrapper
	 */
	protected static TestResouceBuilderWrapper defaultBuilder(TestPackageBuilder builder) {
		return new TestResouceBuilderWrapper(builder)
				.init(JarPackages.BASIC, JarPackages.DOZER, JarPackages.SECURITY,
						JarPackages.CASE_CREATION, JarPackages.ADAPTERS_MOCK,
						PmJarPackages.RESOURCES)
				.addClasess(BaseArquillianCITest.class, PmModulesInfo.class, PmJarPackages.class,
						ModulesInfo.class, BaseArquillianPmCITest.class,
						PmTestResourcePackager.class, PmResourceImporter.class,
						CmfTestResourcePackager.class, JarPackages.class, TestableJarModules.class,
						TestableWarModules.class, WarPackages.class, TestPackageBuilder.class,
						TestResouceBuilderWrapper.class, ResourceImporter.class)
				.addClasess(MailNotificationServiceMock.class, CodelistServerAccessorMock.class,
						CodelistServiceProviderMock.class, LinkProviderServiceMock.class,
						GroupServiceImplMock.class, PeopleServiceImplMock.class,
						AuthenticationServiceMock.class);
	}

	/**
	 * Creates the project instance as root instance
	 *
	 * @param definitionId
	 *            is the test definition for project
	 * @return the project instance created
	 */
	protected ProjectInstance createProjectInstance(String definitionId) {
		ProjectDefinition definition = getDefinition(ProjectDefinition.class, definitionId);
		ProjectInstance createInstance = getProjectService().createInstance(definition, null);
		createInstance.getProperties().put(DocumentProperties.TITLE, definitionId);
		createInstance.getProperties().put(ProjectProperties.OWNER,
				authenticationService.getCurrentUserId());
		ProjectInstance saved = getProjectService().save(createInstance,
				new Operation(PmActionTypeConstants.CREATE_PROJECT));
		return saved;
	}

	/**
	 * Factory method. Should be implemented in subclasses if creation of project is needed
	 *
	 * @return the project service
	 */
	protected ProjectService getProjectService() {
		return null;
	}

}
