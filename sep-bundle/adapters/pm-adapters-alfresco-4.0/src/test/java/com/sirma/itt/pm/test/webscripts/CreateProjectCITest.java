package com.sirma.itt.pm.test.webscripts;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.test.PmBaseAlfrescoCITest;
import com.sirma.itt.emf.adapter.DMSDefintionAdapterService;
import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.pm.alfresco4.services.ProjectInstanceAlfresco4Service;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * The Class CreateProjectTest.
 */
public class CreateProjectCITest extends PmBaseAlfrescoCITest {

	/** The instance adapter. */
	private ProjectInstanceAlfresco4Service instanceAdapter;

	@Override
	@BeforeClass
	protected void setUp() {
		super.setUp();
		instanceAdapter = getMockupProvider().mockupProjectAdapter();
	}

	/**
	 * Test create.
	 */
	@Test
	public void testCreate() {
		ProjectInstance projectInstance = new ProjectInstance();
		DMSDefintionAdapterService mockupDefinitonAdapter = mockupProvider.mockupDefinitonAdapter();
		try {
			List<FileDescriptor> allProjectDefinitions = mockupDefinitonAdapter
					.getDefinitions(ProjectDefinition.class);
			projectInstance.setIdentifier(allProjectDefinitions.get(0).getId());
			projectInstance.setProperties(new HashMap<String, Serializable>());
			projectInstance.getProperties().put("identifier", UUID.randomUUID().toString());
			projectInstance.getProperties().put("type", "GEP10001");
			instanceAdapter.createProjectInstance(projectInstance);
		} catch (DMSException e) {
			fail(e);
		}
	}
}