package com.sirma.itt.pm.services.impl;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.services.mock.CommentServiceImplMock;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.operation.Operation;
import com.sirma.itt.pm.domain.definitions.ProjectDefinition;
import com.sirma.itt.pm.domain.model.ProjectInstance;
import com.sirma.itt.pm.event.ProjectCreateEvent;
import com.sirma.itt.pm.security.PmActionTypeConstants;
import com.sirma.itt.pm.services.ProjectService;
import com.sirma.itt.pm.testutil.BaseArquillianPmCITest;
import com.sirma.itt.pm.testutil.PmTestResourcePackager;

/**
 * @author bbanchev
 */
public class ProjectServiceImplCITest extends BaseArquillianPmCITest {
	@Inject
	private ProjectService projectService;
	@Inject
	private StateService stateService;

	private static ProjectInstance lastCreated;

	/**
	 * Creates a war deployment.
	 *
	 * @return the deployment
	 */
	@Deployment
	public static WebArchive createDeployment() {
		return BaseArquillianPmCITest.defaultBuilder(new PmTestResourcePackager())
				.addClasess(CommentServiceImplMock.class).packageWar();
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#createInstance(com.sirma.itt.pm.domain.definitions.ProjectDefinition, com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = true)
	public void testCreateInstanceProjectDefinitionInstance() throws Exception {
		ProjectDefinition definition = getDefinition(ProjectDefinition.class,
				DEFAULT_DEFINITION_ID_PROJECT);
		ProjectInstance createInstance = projectService.createInstance(definition, null);
		Assert.assertNotNull(createInstance);
		assertEquals(lastCreated, createInstance, "Event should be fired!");
		Assert.assertNotNull(createInstance.getId());
		Assert.assertNotNull(createInstance.getProperties());
		// still not in dms
		Assert.assertNull(createInstance.getDmsId());
		assertEquals(createInstance.getIdentifier(), DEFAULT_DEFINITION_ID_PROJECT,
				"Id should match");

		String primaryState = stateService.getPrimaryState(lastCreated);
		assertEquals(primaryState, PrimaryStates.INITIAL.getType(), "State should be initial");
	}

	/**
	 * Created project event.
	 *
	 * @param event
	 *            is the event
	 */
	public void onProjectCreated(@Observes ProjectCreateEvent event) {
		lastCreated = event.getInstance();
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#save(com.sirma.itt.pm.domain.model.ProjectInstance, com.sirma.itt.emf.state.operation.Operation)}
	 * .
	 */
	@Test(enabled = true, dependsOnMethods = { "testCreateInstanceProjectDefinitionInstance" })
	public void testSave() throws Exception {
		projectService.save(lastCreated, new Operation(PmActionTypeConstants.CREATE_PROJECT));
		Assert.assertNotNull(lastCreated.getDmsId());
		String primaryState = stateService.getPrimaryState(lastCreated);
		assertEquals(primaryState, PrimaryStates.SUBMITTED.getType(), "State should be changed");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#loadInstances(com.sirma.itt.emf.instance.model.Instance)}
	 * .
	 */
	@Test(enabled = false)
	public void testLoadInstances() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#loadByDbId(java.io.Serializable)}.
	 */
	@Test(enabled = false)
	public void testLoadByDbIdSerializable() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#load(java.io.Serializable)}.
	 */
	@Test(enabled = false)
	public void testLoadSerializable() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#getAllowedChildren(com.sirma.itt.pm.domain.model.ProjectInstance)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetAllowedChildrenProjectInstance() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#getAllowedChildren(com.sirma.itt.pm.domain.model.ProjectInstance, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testGetAllowedChildrenProjectInstanceString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#isChildAllowed(com.sirma.itt.pm.domain.model.ProjectInstance, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public void testIsChildAllowed() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#getInstanceDefinitionClass()}.
	 */
	@Test(enabled = false)
	public void testGetInstanceDefinitionClass() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#refresh(com.sirma.itt.pm.domain.model.ProjectInstance)}
	 * .
	 */
	@Test(enabled = false)
	public void testRefresh() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#cancel(com.sirma.itt.pm.domain.model.ProjectInstance)}
	 * .
	 */
	@Test(enabled = false)
	public void testCancel() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#clone(com.sirma.itt.pm.domain.model.ProjectInstance, com.sirma.itt.emf.state.operation.Operation)}
	 * .
	 */
	@Test(enabled = false)
	public void testClone() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#delete(com.sirma.itt.pm.domain.model.ProjectInstance, com.sirma.itt.emf.state.operation.Operation, boolean)}
	 * .
	 */
	@Test(enabled = false)
	public void testDelete() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#attach(com.sirma.itt.pm.domain.model.ProjectInstance, com.sirma.itt.emf.state.operation.Operation, com.sirma.itt.emf.instance.model.Instance[])}
	 * .
	 */
	@Test(enabled = false)
	public void testAttach() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.pm.services.impl.ProjectServiceImpl#detach(com.sirma.itt.pm.domain.model.ProjectInstance, com.sirma.itt.emf.state.operation.Operation, com.sirma.itt.emf.instance.model.Instance[])}
	 * .
	 */
	@Test(enabled = false)
	public void testDetach() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

}
