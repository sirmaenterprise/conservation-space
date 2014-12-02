package com.sirma.itt.cmf.security.evaluator;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.beans.model.DocumentInstance;
import com.sirma.itt.cmf.beans.model.SectionInstance;
import com.sirma.itt.cmf.constants.DocumentProperties;
import com.sirma.itt.cmf.domain.ObjectTypesCmf;
import com.sirma.itt.cmf.services.DocumentService;
import com.sirma.itt.cmf.states.PrimaryStates;
import com.sirma.itt.cmf.testutil.CmfTest;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.dao.InstanceService;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.resources.model.Resource;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.RoleEvaluator;
import com.sirma.itt.emf.security.RoleEvaluatorManagerService;
import com.sirma.itt.emf.security.SecurityModel;
import com.sirma.itt.emf.security.SecurityModel.BaseRoles;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.security.model.EmfPermission;
import com.sirma.itt.emf.security.model.EmfUser;
import com.sirma.itt.emf.security.model.Permission;
import com.sirma.itt.emf.security.model.Role;
import com.sirma.itt.emf.security.model.RoleIdentifier;
import com.sirma.itt.emf.security.model.RoleImpl;
import com.sirma.itt.emf.security.model.RoleRegistry;
import com.sirma.itt.emf.state.StateService;
import com.sirma.itt.emf.state.transition.StateTransitionManager;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * The Class DocumentRoleEvaluatorTest.
 *
 * @author BBonev
 */
@Test
public class DocumentRoleEvaluatorTest extends CmfTest {

	/** The state service. */
	private StateService stateService;

	/** The transition manager. */
	private StateTransitionManager transitionManager;

	/** The resource service. */
	private ResourceService resourceService;

	/** The instance service. */
	@SuppressWarnings("rawtypes")
	private InstanceService instanceService;

	/** The authority service. */
	private AuthorityService authorityService;

	/** The role evaluator manager service. */
	private RoleEvaluatorManagerService roleEvaluatorManagerService;

	/** The role registry. */
	private RoleRegistry roleRegistry;

	private DocumentService documentService;

	/**
	 * Initializes the.
	 */
	@BeforeMethod
	public void init() {
		stateService = Mockito.mock(StateService.class);
		Mockito.when(stateService.getPrimaryState(Mockito.any(Instance.class))).then(
				new Answer<String>() {

					@Override
					public String answer(InvocationOnMock invocation) throws Throwable {
						Instance instance = (Instance) invocation.getArguments()[0];
						return (String) instance.getProperties().get(DefaultProperties.STATUS);
					}
				});
		transitionManager = Mockito.mock(StateTransitionManager.class);
		resourceService = Mockito.mock(ResourceService.class);
		instanceService = Mockito.mock(InstanceService.class);
		authorityService = Mockito.mock(AuthorityService.class);
		roleEvaluatorManagerService = Mockito.mock(RoleEvaluatorManagerService.class);
		roleRegistry = Mockito.mock(RoleRegistry.class);
		documentService = Mockito.mock(DocumentService.class);
	}

	/**
	 * Test workflow action.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_workflowAction() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		DocumentInstance target = createDocument();

		Resource resource = createResource();
		Role role = createAdminRole("create", DocumentInstance.class,
				DocumentRoleEvaluator.START_WORKFLOW);

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays.asList(DocumentRoleEvaluator.START_WORKFLOW)));

		Set<Action> actions = evaluator.filterActions(target, resource, role);
		Assert.assertFalse(actions.contains(DocumentRoleEvaluator.START_WORKFLOW));

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays.asList(DocumentRoleEvaluator.START_WORKFLOW)));

		Mockito.when(
				instanceService.isChildAllowed(Mockito.any(DocumentInstance.class),
						Mockito.eq(ObjectTypesCmf.WORKFLOW))).thenReturn(true);

		target.getProperties().put(DefaultProperties.STATUS, "status2");

		actions = evaluator.filterActions(target, resource, role);

		Assert.assertTrue(actions.contains(DocumentRoleEvaluator.START_WORKFLOW));
	}

	/**
	 * Test filter_deleted context.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_deletedContext() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		DocumentInstance target = new DocumentInstance();
		ReflectionUtils.setField(target, "owningInstance", caseInstance);
		target.setId("doc");
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put(DefaultProperties.STATUS, "status1");

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(
				new HashSet<Action>(Arrays.asList(DocumentRoleEvaluator.DOWNLOAD)));

		Mockito.when(stateService.isInStates(caseInstance, PrimaryStates.DELETED)).thenReturn(true);

		Set<Action> set = evaluator.filterActions(target, createResource(),
				createAdminRole("create", DocumentInstance.class, DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertTrue(set.isEmpty());
	}

	/**
	 * Test filter_ canceled context.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_CanceledContext() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		DocumentInstance target = new DocumentInstance();
		ReflectionUtils.setField(target, "owningInstance", caseInstance);
		target.setId("doc");
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put(DefaultProperties.STATUS, "status1");

		Mockito.when(stateService.isInStates(caseInstance, PrimaryStates.CANCELED))
				.thenReturn(true);

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOWNLOAD,
				DocumentRoleEvaluator.PRINT, DocumentRoleEvaluator.COPY_CONTENT,
				DocumentRoleEvaluator.LINK_DOCUMENT, DocumentRoleEvaluator.START_WORKFLOW);
		Set<Action> testedActions = new HashSet<Action>(asList);

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.PRINT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.COPY_CONTENT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.LINK_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.START_WORKFLOW));
	}

	/**
	 * Test filter_history_no history.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_history_noHistory() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOWNLOAD,
				DocumentRoleEvaluator.PRINT, DocumentRoleEvaluator.COPY_CONTENT,
				DocumentRoleEvaluator.LINK_DOCUMENT, DocumentRoleEvaluator.START_WORKFLOW,
				DocumentRoleEvaluator.REVERT, DocumentRoleEvaluator.HISTORY_PREVIEW);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));
		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.COPY_CONTENT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.LINK_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.REVERT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.HISTORY_PREVIEW));
	}

	/**
	 * Test filter_history.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_history() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOWNLOAD,
				DocumentRoleEvaluator.PRINT, DocumentRoleEvaluator.COPY_CONTENT,
				DocumentRoleEvaluator.LINK_DOCUMENT, DocumentRoleEvaluator.START_WORKFLOW,
				DocumentRoleEvaluator.REVERT, DocumentRoleEvaluator.HISTORY_PREVIEW);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		target.getProperties().put(DocumentProperties.DOCUMENT_CURRENT_VERSION_INSTANCE, target);

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.PRINT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.REVERT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.HISTORY_PREVIEW));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.COPY_CONTENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LINK_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.START_WORKFLOW));
	}

	/**
	 * Test filter_history_locked.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_history_locked() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOWNLOAD,
				DocumentRoleEvaluator.PRINT, DocumentRoleEvaluator.COPY_CONTENT,
				DocumentRoleEvaluator.LINK_DOCUMENT, DocumentRoleEvaluator.START_WORKFLOW,
				DocumentRoleEvaluator.REVERT, DocumentRoleEvaluator.HISTORY_PREVIEW);
		Set<Action> testedActions = new HashSet<Action>(asList);

		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		DocumentInstance target = new DocumentInstance();
		ReflectionUtils.setField(target, "owningInstance", caseInstance);
		target.setId("doc");
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put(DefaultProperties.STATUS, "status1");
		target.getProperties().put(DefaultProperties.LOCKED_BY, "emf:admin");

		Mockito.when(stateService.isInStates(caseInstance, PrimaryStates.DELETED))
				.thenReturn(false);
		Mockito.when(stateService.isInStates(caseInstance, PrimaryStates.CANCELED)).thenReturn(
				false);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		target.getProperties().put(DocumentProperties.DOCUMENT_CURRENT_VERSION_INSTANCE, target);

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.PRINT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.REVERT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.HISTORY_PREVIEW));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.COPY_CONTENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LINK_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.START_WORKFLOW));
	}

	/**
	 * Test filter_locked.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_locked() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.EDIT_PROPERTIES,
				DocumentRoleEvaluator.LINK_DOCUMENT, DocumentRoleEvaluator.COPY_CONTENT,
				DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE, DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE,
				DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT, DocumentRoleEvaluator.DELETE,
				DocumentRoleEvaluator.DOCUMENT_SIGN, DocumentRoleEvaluator.UNLOCK,
				DocumentRoleEvaluator.DOWNLOAD);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DefaultProperties.LOCKED_BY, "emf:admin");
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UNLOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.EDIT_PROPERTIES));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.COPY_CONTENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LINK_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DELETE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));
	}

	/**
	 * Test filter_locked.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_Idoc() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.EXPORT,
				DocumentRoleEvaluator.SAVE_AS_PUBLIC_TEMPLATE, DocumentRoleEvaluator.DOWNLOAD,
				DocumentRoleEvaluator.UPLOAD, DocumentRoleEvaluator.UPLOAD_NEW_VERSION,
				DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT,
				DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE,
				DocumentRoleEvaluator.CREATE_SUB_DOCUMENT, DocumentRoleEvaluator.CLONE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.setPurpose(DocumentProperties.PURPOSE_IDOC);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.EXPORT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.CLONE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.SAVE_AS_PUBLIC_TEMPLATE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.CREATE_SUB_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UPLOAD));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOWNLOAD));
	}

	/**
	 * Test filter_not manager.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_notManager() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.EDIT_PROPERTIES,
				DocumentRoleEvaluator.LINK_DOCUMENT, DocumentRoleEvaluator.COPY_CONTENT,
				DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE,
				DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE,
				DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT, DocumentRoleEvaluator.DELETE,
				DocumentRoleEvaluator.DOCUMENT_SIGN, DocumentRoleEvaluator.UNLOCK,
				DocumentRoleEvaluator.DOWNLOAD);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createRole(BaseRoles.CONTRIBUTOR, "create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DELETE));
	}

	/**
	 * Test filter_structured.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_structured() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.EDIT_PROPERTIES,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION,
				DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE,
				DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT, DocumentRoleEvaluator.PRINT);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.setStructured(Boolean.TRUE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.PRINT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.EDIT_PROPERTIES));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
	}

	/**
	 * Test filter_not structured.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_notStructured() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.EDIT_PROPERTIES,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION,
				DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE,
				DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT, DocumentRoleEvaluator.PRINT);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.setStructured(Boolean.FALSE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.EDIT_STRUCTURED_DOCUMENT));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.PRINT));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.EDIT_PROPERTIES));

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
	}

	/**
	 * Test filter_signed.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_signed() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.EDIT_PROPERTIES);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.DOCUMENT_SIGNED_DATE, new Date());
		target.setStructured(Boolean.FALSE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));
	}

	/**
	 * Test filter_not signed_pdf.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_notSigned_pdf() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_SIGN);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.setStructured(Boolean.FALSE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));
	}

	/**
	 * Test filter_signed_html.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_signed_html() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_SIGN);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.MIMETYPE, "text/html");
		target.setStructured(Boolean.FALSE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertTrue(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));
	}

	/**
	 * Test filter_attached document in section instance.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_attachedDocumentInSectionInstance() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE,
				DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		SectionInstance section = new SectionInstance();
		section.setId("emf:section");
		target.setOwningInstance(section);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE));
	}

	/**
	 * Test filter_attached document in section reference.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_attachedDocumentInSectionReference() {
		createTypeConverter();
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE,
				DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		SectionInstance section = new SectionInstance();
		section.setId("emf:section");
		target.setOwningReference(section.toReference());
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE));
	}

	/**
	 * Test filter_not attached document in section.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_notAttachedDocumentInSection() {
		createTypeConverter();
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE,
				DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		// note that actual case is if the document is attached in object not in case but the
		// algorithm is the same
		CaseInstance section = new CaseInstance();
		section.setId("emf:case");
		target.setOwningInstance(section);
		target.setOwningReference(section.toReference());
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertTrue(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_OTHER_CASE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_MOVE_SAME_CASE));
	}

	/**
	 * Test filter_attached document.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_attachedDocument() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DETACH_DOCUMENT);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		SectionInstance section = new SectionInstance();
		section.setId("emf:section");
		target.setOwningInstance(section);
		Mockito.when(documentService.isAttached(section, target)).thenReturn(true);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DETACH_DOCUMENT));
	}

	/**
	 * Test filter_non attached document.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_nonAttachedDocument() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DETACH_DOCUMENT);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		SectionInstance section = new SectionInstance();
		section.setId("emf:section");
		target.setOwningInstance(section);
		Mockito.when(documentService.isAttached(section, target)).thenReturn(false);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole("create", DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertTrue(set.isEmpty());
	}

	/**
	 * Test filter_permission locked_not locked.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_notLocked() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole(SecurityModel.PERMISSION_LOCK.getPermissionId(),
				DocumentInstance.class, asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UNLOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test filter_permission locked_not locked_inline edit_html.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_notLocked_inlineEdit_html() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties()
				.put(DocumentProperties.MIMETYPE, DocumentRoleEvaluator.HTML_MIMETYPE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole(SecurityModel.PERMISSION_LOCK.getPermissionId(),
				DocumentInstance.class, asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UNLOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test filter_permission locked_not locked_inline edit_xhtml.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_notLocked_inlineEdit_xhtml() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.MIMETYPE,
				DocumentRoleEvaluator.XHTML_MIMETYPE);
		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole(SecurityModel.PERMISSION_LOCK.getPermissionId(),
				DocumentInstance.class, asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, createResource(), role);
		Assert.assertFalse(set.isEmpty());
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UNLOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test filter_permission locked_locked_not same user_admin role.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_locked_notSameUser_adminRole() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.LOCKED_BY, "testUserName");
		Resource resource = createResource();

		Mockito.when(
				resourceService.areEqual(Mockito.any(Resource.class), Mockito.eq("testUserName")))
				.thenReturn(false);

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole(SecurityModel.PERMISSION_LOCK.getPermissionId(),
				DocumentInstance.class, asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, resource, role);
		Assert.assertFalse(set.isEmpty());

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UNLOCK));
		// the check for working copy removes the operations
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test filter_permission locked_locked_not same user_not admin role.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_locked_notSameUser_notAdminRole() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE,
				DocumentRoleEvaluator.DOWNLOAD);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.LOCKED_BY, "testUserName");
		Resource resource = createResource();

		Mockito.when(
				resourceService.areEqual(Mockito.any(Resource.class), Mockito.eq("testUserName")))
				.thenReturn(false);

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createRole(BaseRoles.CONTRIBUTOR,
				SecurityModel.PERMISSION_LOCK.getPermissionId(),
				DocumentInstance.class, asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, resource, role);
		Assert.assertFalse(set.isEmpty());

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UNLOCK));
		// the check for working copy removes the operations
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test filter_permission locked_locked_same user.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_locked_sameUser() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE,
				DocumentRoleEvaluator.DOWNLOAD);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.LOCKED_BY, "emf:user");
		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(Mockito.any(Resource.class), Mockito.eq("emf:user")))
				.thenReturn(true);

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole(
				SecurityModel.PERMISSION_LOCK.getPermissionId(), DocumentInstance.class,
				asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, resource, role);
		Assert.assertFalse(set.isEmpty());

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UNLOCK));
		// the check for working copy removes the operations
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test filter_permission locked_locked_working copy.
	 */
	@SuppressWarnings("unchecked")
	public void testFilter_permissionLocked_locked_workingCopy() {
		DocumentRoleEvaluator evaluator = createEvaluator();

		List<Action> asList = Arrays.asList(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE,
				DocumentRoleEvaluator.UNLOCK, DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE,
				DocumentRoleEvaluator.UPLOAD_NEW_VERSION, DocumentRoleEvaluator.DOCUMENT_SIGN,
				DocumentRoleEvaluator.LOCK, DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE,
				DocumentRoleEvaluator.DOWNLOAD);
		Set<Action> testedActions = new HashSet<Action>(asList);

		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.LOCKED_BY, "emf:user");
		target.getProperties().put(DocumentProperties.WORKING_COPY_LOCATION, "some location");
		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(Mockito.any(Resource.class), Mockito.eq("emf:user")))
				.thenReturn(true);

		Mockito.when(
				transitionManager.getAllowedActions(Mockito.any(DocumentInstance.class),
						Mockito.anyString(), Mockito.any(Set.class))).thenReturn(testedActions);

		Role role = createAdminRole(SecurityModel.PERMISSION_LOCK.getPermissionId(),
				DocumentInstance.class, asList.toArray(new Action[asList.size()]));

		Set<Action> set = evaluator.filterActions(target, resource, role);
		Assert.assertFalse(set.isEmpty());

		Assert.assertTrue(set.contains(DocumentRoleEvaluator.DOWNLOAD));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.CANCEL_EDIT_OFFLINE));
		Assert.assertTrue(set.contains(DocumentRoleEvaluator.UPLOAD_NEW_VERSION));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_SIGN));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.UNLOCK));

		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_ONLINE));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.LOCK));
		Assert.assertFalse(set.contains(DocumentRoleEvaluator.DOCUMENT_EDIT_OFFLINE));
	}

	/**
	 * Test role evaluation_admin user.
	 */
	public void testRoleEvaluation_adminUser() {
		DocumentRoleEvaluator evaluator = createEvaluator();
		DocumentInstance target = createDocument();
		Resource resource = createResource();

		Mockito.when(authorityService.isAdminOrSystemUser(resource)).thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.ADMINISTRATOR,
				Collections.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(roleRegistry.find(BaseRoles.ADMINISTRATOR)).thenReturn(role);

		Pair<Role, RoleEvaluator<DocumentInstance>> evaluate = evaluator.evaluate(target, resource,
				null);
		Assert.assertNotNull(evaluate);
		Assert.assertNotNull(evaluate.getFirst());

		Assert.assertEquals(evaluate.getFirst(), role);
	}

	/**
	 * Test role evaluation_creator.
	 */
	public void testRoleEvaluation_creator() {
		DocumentRoleEvaluator evaluator = createEvaluator();
		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.CREATED_BY, "emf:user");
		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(resource, "emf:user")).thenReturn(true);

		RoleImpl role = new RoleImpl(BaseRoles.CREATOR,
				Collections.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(roleRegistry.find(BaseRoles.CREATOR)).thenReturn(role);

		Pair<Role, RoleEvaluator<DocumentInstance>> evaluate = evaluator.evaluate(target, resource,
				null);
		Assert.assertNotNull(evaluate);
		Assert.assertNotNull(evaluate.getFirst());

		Assert.assertEquals(evaluate.getFirst(), role);
	}

	/**
	 * Test role evaluation_not creator.
	 */
	public void testRoleEvaluation_notCreator() {
		DocumentRoleEvaluator evaluator = createEvaluator();
		DocumentInstance target = createDocument();
		target.getProperties().put(DocumentProperties.CREATED_BY, "emf:admin");
		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(resource, "emf:admin")).thenReturn(false);

		RoleImpl role = new RoleImpl(BaseRoles.VIEWER,
				Collections.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(roleRegistry.find(BaseRoles.VIEWER)).thenReturn(role);

		Pair<Role, RoleEvaluator<DocumentInstance>> evaluate = evaluator.evaluate(target, resource,
				null);
		Assert.assertNotNull(evaluate);
		Assert.assertNotNull(evaluate.getFirst());

		Assert.assertEquals(evaluate.getFirst(), role);
	}

	/**
	 * Test role evaluation_not attached.
	 */
	public void testRoleEvaluation_notAttached() {
		DocumentRoleEvaluator evaluator = createEvaluator();
		DocumentInstance target = createDocument();
		// no parant context
		target.setOwningInstance(null);
		target.setOwningReference(null);
		target.getProperties().put(DocumentProperties.CREATED_BY, "emf:admin");
		Resource resource = createResource();

		Mockito.when(resourceService.areEqual(resource, "emf:admin")).thenReturn(false);

		RoleImpl role = new RoleImpl(BaseRoles.CONSUMER,
				Collections.<Permission, List<Pair<Class<?>, Action>>> emptyMap());
		Mockito.when(roleRegistry.find(BaseRoles.CONSUMER)).thenReturn(role);

		Pair<Role, RoleEvaluator<DocumentInstance>> evaluate = evaluator.evaluate(target, resource,
				null);
		Assert.assertNotNull(evaluate);
		Assert.assertNotNull(evaluate.getFirst());

		Assert.assertEquals(evaluate.getFirst(), role);
	}

	/**
	 * Creates the document.
	 *
	 * @return the document instance
	 */
	private DocumentInstance createDocument() {
		CaseInstance caseInstance = new CaseInstance();
		caseInstance.setId("case");
		caseInstance.setProperties(new HashMap<String, Serializable>());
		DocumentInstance target = new DocumentInstance();
		ReflectionUtils.setField(target, "owningInstance", caseInstance);
		target.setId("doc");
		target.setProperties(new HashMap<String, Serializable>());
		target.getProperties().put(DefaultProperties.STATUS, "status1");
		target.getProperties().put(DocumentProperties.MIMETYPE, "application/pdf");

		Mockito.when(stateService.isInStates(caseInstance, PrimaryStates.DELETED))
				.thenReturn(false);
		Mockito.when(stateService.isInStates(caseInstance, PrimaryStates.CANCELED)).thenReturn(
				false);
		return target;
	}

	/**
	 * Creates the role.
	 *
	 * @param roleId
	 *            the role id
	 * @param permission
	 *            the permission
	 * @param target
	 *            the target
	 * @param actions
	 *            the actions
	 * @return the role
	 */
	private Role createRole(RoleIdentifier roleId, String permission, Class<?> target,
			Action... actions) {
		Map<Permission, List<Pair<Class<?>, Action>>> permissions = new HashMap<Permission, List<Pair<Class<?>, Action>>>();
		EmfPermission key = new EmfPermission(permission);
		for (Action action : actions) {
			CollectionUtils.addValueToMap(permissions, key, new Pair<Class<?>, Action>(target,
					action));
		}
		Role role = new RoleImpl(roleId, permissions);
		return role;
	}

	/**
	 * Creates the role.
	 *
	 * @param permission
	 *            the permission
	 * @param target
	 *            the target
	 * @param actions
	 *            the actions
	 * @return the role
	 */
	private Role createAdminRole(String permission, Class<?> target, Action... actions) {
		return createRole(BaseRoles.ADMINISTRATOR, permission, target, actions);
	}

	/**
	 * Creates the resource.
	 *
	 * @return the resource
	 */
	private Resource createResource() {
		Resource resource = new EmfUser();
		resource.setId("emf:user");
		resource.setIdentifier("user");
		return resource;
	}

	/**
	 * Creates the evaluator.
	 *
	 * @return the document role evaluator
	 */
	private DocumentRoleEvaluator createEvaluator() {
		DocumentRoleEvaluator evaluator = new DocumentRoleEvaluator();
		ReflectionUtils.setField(evaluator, "stateService", stateService);
		ReflectionUtils.setField(evaluator, "documentService", documentService);
		ReflectionUtils.setField(evaluator, "transitionManager", transitionManager);
		ReflectionUtils.setField(evaluator, "resourceService", resourceService);
		ReflectionUtils.setField(evaluator, "instanceService", instanceService);
		ReflectionUtils.setField(evaluator, "authorityService", authorityService);
		ReflectionUtils.setField(evaluator, "registry", roleRegistry);
		ReflectionUtils.setField(evaluator, "roleEvaluatorManagerService",
				new InstanceProxyMock<RoleEvaluatorManagerService>(roleEvaluatorManagerService));
		evaluator.initialize();
		return evaluator;
	}

}
