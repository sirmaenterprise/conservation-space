package com.sirma.itt.cmf.services.impl;

import static com.sirma.itt.emf.security.SecurityModel.ActivitiRoles.ASSIGNEE;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.ADMINISTRATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.COLLABORATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CONSUMER;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.CREATOR;
import static com.sirma.itt.emf.security.SecurityModel.BaseRoles.VIEWER;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.ByteArrayAndPropertiesDescriptor;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.TaskInstance;
import com.sirma.itt.cmf.beans.model.TaskState;
import com.sirma.itt.cmf.beans.model.WorkflowInstanceContext;
import com.sirma.itt.cmf.constants.CaseProperties;
import com.sirma.itt.cmf.constants.TaskProperties;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.cmf.security.evaluator.CommentRoleEvaluator;
import com.sirma.itt.cmf.security.evaluator.TopicRoleEvaluator;
import com.sirma.itt.cmf.services.CaseService;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.services.TaskService;
import com.sirma.itt.cmf.services.WorkflowService;
import com.sirma.itt.cmf.testutil.BaseArquillianCITest;
import com.sirma.itt.cmf.testutil.CmfTestResourcePackager;
import com.sirma.itt.emf.forum.CommentService;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.ResourceType;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.User;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * Tests AuthorityServiceImpl functionality.
 *
 * @author bbanchev
 */
public class AuthorityServiceImplCITest extends BaseArquillianCITest {

	/** The service. */
	@Inject
	protected AuthorityService authService;
	@Inject
	protected ResourceService resourceService;
	@Inject
	protected CaseService caseService;
	@Inject
	protected DocumentService documentService;
	@Inject
	protected WorkflowService workflowService;
	@Inject
	protected TaskService taskService;
	@Inject
	protected CommentService commentService;

	private static CaseInstance createdCase;
	private static DocumentInstance uploadDocument;

	/**
	 * Creates the deployment.
	 *
	 * @return the web archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		System.out.println("Starting test: " + AuthorityServiceImplCITest.class);
		return defaultBuilder(new CmfTestResourcePackager()).addClasess(TopicRoleEvaluator.class,
				CommentRoleEvaluator.class).packageWar();
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getAllowedActions(com.sirma.itt.emf.instance.model.Instance, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public final void testGetAllowedActionsInstanceString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getAllowedAction(com.sirma.itt.emf.instance.model.Instance, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public final void testGetAllowedAction() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getAllowedActions(java.lang.String, com.sirma.itt.emf.instance.model.Instance, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public final void testGetAllowedActionsStringInstanceString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getUserRole(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.resources.model.Resource)}
	 * . Case related subtests.
	 */
	@Test
	public final void testGetUserRoleCaseResource() throws Exception {
		List<User> allResources = resourceService.getAllResources(ResourceType.USER, null);
		assertTrue(allResources.size() > 5, "Users shoould be more than 5");
		User caseCreator = chooseNewUser(true, "admin", "system", "bbanchev", "banchev");
		Map<String, Serializable> caseProperties = new HashMap<>();
		caseProperties.put(DefaultProperties.TITLE, "task holder");
		caseProperties.put(DefaultProperties.DESCRIPTION, "created for holder of tasks");
		createdCase = createCase(null, DEFAULT_DEFINITION_ID_CASE, caseProperties);
		// depends on admin.groupname=GROUP_ALFRESCO_ADMINISTRATORS
		Role userRole = authService.getUserRole(createdCase,
				resourceService.getResource("admin", ResourceType.USER));
		assertEquals(userRole.getRoleId(), ADMINISTRATOR, "admin should be " + ADMINISTRATOR);
		userRole = authService.getUserRole(createdCase, authenticationService.getCurrentUser());
		assertEquals(userRole.getRoleId(), CREATOR, authenticationService.getCurrentUserId()
				+ " should be " + CREATOR);
		String userId = "banchev";
		userRole = authService.getUserRole(createdCase,
				resourceService.getResource(userId, ResourceType.USER));
		assertEquals(userRole.getRoleId(), VIEWER, userId + " should be " + VIEWER);
		caseService.delete(createdCase, new Operation(ActionTypeConstants.DELETE), true);

		userRole = authService.getUserRole(createdCase, caseCreator);
		assertEquals(userRole.getRoleId(), VIEWER, caseCreator.getIdentifier() + " should be "
				+ VIEWER);
		// create new case now
		createdCase = createCase(null, DEFAULT_DEFINITION_ID_CASE, caseProperties);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getUserRole(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.resources.model.Resource)}
	 * . Document related subtests.
	 */
	@Test(enabled = true, dependsOnMethods = { "testGetUserRoleCaseResource" })
	public final void testGetUserRoleDocumentResource() throws Exception {
		User authenticated = resourceService.getResource(
				(String) createdCase.getProperties().get(CaseProperties.CREATED_BY),
				ResourceType.USER);
		Map<String, Serializable> props = new HashMap<>();
		User authenticated2 = chooseNewUser(true, "admin", "system", "bbanchev",
				authenticated.getIdentifier());
		uploadDocument = uploadDocument(
				createdCase.getSections().get(0),
				new ByteArrayAndPropertiesDescriptor("testfile.txt", "this is my txt file content"
						.getBytes(), null, props));
		// this actually should not be allowed
		Role userRole = authService.getUserRole(uploadDocument, authenticated);
		assertEquals(userRole.getRoleId(), VIEWER, authenticated.getIdentifier() + " should be "
				+ VIEWER);
		userRole = authService.getUserRole(uploadDocument, authenticated2);
		assertEquals(userRole.getRoleId(), CREATOR, authenticated2.getIdentifier() + " should be "
				+ CREATOR);

	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getUserRole(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.resources.model.Resource)}
	 * . Task and workflow related subtests.
	 */
	@Test(enabled = true, dependsOnMethods = { "testGetUserRoleCaseResource" })
	public final void testGetUserRoleProcessResource() throws Exception {
		User authenticated = resourceService.getResource(
				(String) createdCase.getProperties().get(CaseProperties.CREATED_BY),
				ResourceType.USER);

		User assignee = chooseNewUser(false, "admin", "system", "bbanchev",
				authenticated.getIdentifier());
		Map<String, Serializable> props = new HashMap<>();
		props.put(TaskProperties.TASK_ASSIGNEE, assignee.getIdentifier());
		// simulate activiti set the owner
		props.put(TaskProperties.TASK_OWNER, assignee.getIdentifier());
		assertEquals(authService.getUserRole(createdCase, assignee).getRoleId(), VIEWER,
				assignee.getIdentifier() + " should be " + VIEWER);
		User choosedNewUser = chooseNewUser(true, "admin", "system", "bbanchev",
				authenticated.getIdentifier());
		WorkflowInstanceContext startedWorkflow = startWorkflow(createdCase,
				DEFAULT_DEFINITION_ID_WORKFLOW, props);
		assertEquals(authService.getUserRole(startedWorkflow, choosedNewUser).getRoleId(), CREATOR,
				choosedNewUser.getIdentifier() + " should be " + CREATOR);
		List<TaskInstance> workflowTasks = getWorkflowService().getWorkflowTasks(startedWorkflow,
				TaskState.IN_PROGRESS);
		for (TaskInstance taskInstance : workflowTasks) {
			assertEquals(authService.getUserRole(taskInstance, assignee).getRoleId(), ASSIGNEE,
					assignee.getIdentifier() + " should be " + ASSIGNEE);
		}
		assertEquals(authService.getUserRole(createdCase, assignee).getRoleId(), COLLABORATOR,
				assignee.getIdentifier() + " should be " + COLLABORATOR);

		workflowService.cancel(startedWorkflow);
		// now should be consumer
		assertEquals(authService.getUserRole(createdCase, assignee).getRoleId(), CONSUMER,
				assignee.getIdentifier() + " should be " + CONSUMER);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#getUserRole(com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.resources.model.Resource)}
	 * . Comment related subtests.
	 */
	@Test(enabled = true, dependsOnMethods = { "testGetUserRoleCaseResource",
			"testGetUserRoleDocumentResource" })
	public final void testGetUserRoleCommentResource() throws Exception {
		User authenticated = resourceService.getResource(
				(String) createdCase.getProperties().get(CaseProperties.CREATED_BY),
				ResourceType.USER);
		User authenticated2 = resourceService.getResource((String) uploadDocument.getProperties()
				.get(CaseProperties.CREATED_BY), ResourceType.USER);
		TopicInstance createdTopic = createTopic(uploadDocument, authenticated2);
		assertEquals(authService.getUserRole(createdTopic, authenticated2).getRoleId(), CREATOR,
				authenticated2.getIdentifier() + " should be " + CREATOR);
		User choosedNewUser = chooseNewUser(false, "admin", "system", "bbanchev");
		assertEquals(authService.getUserRole(createdTopic, authenticated).getRoleId(), VIEWER,
				authenticated.getIdentifier() + " should be " + VIEWER);
		assertEquals(authService.getUserRole(createdTopic, choosedNewUser).getRoleId(), VIEWER,
				choosedNewUser.getIdentifier() + " should be " + VIEWER);

		CommentInstance createdComment = createComment(createdTopic, choosedNewUser);
		assertEquals(authService.getUserRole(createdTopic, choosedNewUser).getRoleId(), VIEWER,
				choosedNewUser.getIdentifier() + " should be " + VIEWER);
		assertEquals(authService.getUserRole(createdComment, choosedNewUser).getRoleId(), CREATOR,
				choosedNewUser.getIdentifier() + " should be " + CREATOR);
		// topic creator is owner of comments as well
		assertEquals(authService.getUserRole(createdComment, authenticated2).getRoleId(), CREATOR,
				authenticated2.getIdentifier() + " should be " + CREATOR);
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#isActionAllowed(com.sirma.itt.emf.instance.model.Instance, java.lang.String, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public final void testIsActionAllowedInstanceStringString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#isActionAllowed(com.sirma.itt.emf.instance.model.Instance, java.lang.String, com.sirma.itt.emf.resources.model.Resource, java.lang.String)}
	 * .
	 */
	@Test(enabled = false)
	public final void testIsActionAllowedInstanceStringResourceString() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#filterAllowedActions(com.sirma.itt.emf.instance.model.Instance, java.lang.String, java.lang.String[])}
	 * .
	 */
	@Test(enabled = false)
	public final void testFilterAllowedActions() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * Test method for
	 * {@link com.sirma.itt.emf.security.AuthorityService#hasPermission(com.sirma.itt.emf.security.model.Permission, com.sirma.itt.emf.instance.model.Instance, com.sirma.itt.emf.resources.model.Resource)}
	 * .
	 */
	@Test(enabled = false)
	public final void testHasPermissionPermissionInstanceResource() throws Exception {
		throw new RuntimeException("not yet implemented");
	}

	@Override
	protected DocumentService getDocumentService() {
		return documentService;
	}

	@Override
	protected CaseService getCaseService() {
		return caseService;
	}

	@Override
	protected WorkflowService getWorkflowService() {
		return workflowService;
	}

	@Override
	protected TaskService getTaskService() {
		return taskService;
	}

	@Override
	protected CommentService getCommentService() {
		return commentService;
	}

}
