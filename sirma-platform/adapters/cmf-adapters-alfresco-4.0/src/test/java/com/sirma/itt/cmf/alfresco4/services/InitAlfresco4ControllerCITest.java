package com.sirma.itt.cmf.alfresco4.services;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller.DefinitionType;
import com.sirma.itt.cmf.alfresco4.services.InitAlfresco4Controller.InitConfiguration;
import com.sirma.itt.cmf.test.BaseAlfrescoTest;
import com.sirma.itt.seip.util.file.ArchiveUtil;

/**
 * Tests the init controller for alfresco
 *
 * @author bbanchev
 */
public class InitAlfresco4ControllerCITest extends BaseAlfrescoTest {
	private InitAlfresco4Controller initController;

	@BeforeClass
	@Override
	protected void setUp() {
		super.setUp();
		initController = mockupProvider.mockupInitAlfresco4Controller();
	}

	/**
	 * Tests the initialization process
	 */
	@Test
	public void initialize() {
		InitConfiguration configuration = new InitConfiguration();
		try {
			configuration.setSiteId("test");
			configuration.setAdminUser("admin");
			File createTempFolder = createTempFolder();
			ArchiveUtil.unZip(InitAlfresco4Controller.class.getResourceAsStream("base-definitions.zip"),
					createTempFolder);
			// add all definitions
			configuration.setDefinitionsLocation(createTempFolder.getAbsolutePath());
			configuration.addDefinitionType(DefinitionType.values());
			initController.initialize(configuration, progreess -> {
				// add progress handling
			});
		} catch (Exception e) {
			fail(e);
		}
	}

	private File createTempFolder() throws IOException {
		File createTempFile = File.createTempFile("InitAlfresco4Controller", "");
		createTempFile.mkdirs();
		createTempFile.delete();
		createTempFile.mkdirs();
		return createTempFile;
	}
}
