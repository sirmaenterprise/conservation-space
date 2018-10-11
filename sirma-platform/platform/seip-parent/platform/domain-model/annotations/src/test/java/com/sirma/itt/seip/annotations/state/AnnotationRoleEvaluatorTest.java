package com.sirma.itt.seip.annotations.state;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.state.StateService;
import com.sirma.itt.seip.instance.state.StateTransitionManager;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.SecurityModel.BaseRoles;
import com.sirma.itt.seip.permissions.action.AuthorityService;
import com.sirma.itt.seip.permissions.action.EmfAction;
import com.sirma.itt.seip.permissions.role.Role;
import com.sirma.itt.seip.permissions.role.RoleEvaluatorManagerService;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.testutil.mocks.InstanceProxyMock;
import com.sirma.itt.semantic.model.vocabulary.EMF;

public class AnnotationRoleEvaluatorTest {

	@InjectMocks
	private AnnotationRoleEvaluator roleEvaluator;

	@Mock
	protected ResourceService resourceService;
	@Mock
	private StateService stateService;
	@Mock
	private StateTransitionManager transitionManager;
	@Mock
	AuthorityService authorityService;
	@Mock
	protected PermissionService permissionService;
	@Mock
	private RoleRegistry roleRegistry;
	private Role roleConsumer;
	private Role roleCollaborator;
	@Mock
	private RoleEvaluatorManagerService evaluatorManagerServiceMock;
	@Spy
	private InstanceProxyMock<RoleEvaluatorManagerService> roleEvaluatorManagerService = new InstanceProxyMock<>(null);
	@Mock
	private LabelProvider labelProvider;

	protected static final Action REPLY = new EmfAction(ActionTypeConstants.REPLY_COMMENT);
	protected static final Action SUSPEND = new EmfAction(ActionTypeConstants.SUSPEND_COMMENT);
	protected static final Action RESTART = new EmfAction(ActionTypeConstants.RESTART_COMMENT);
	protected static final Action EDIT = new EmfAction(ActionTypeConstants.EDIT_COMMENT);
	protected static final Action DELETE = new EmfAction(ActionTypeConstants.DELETE);

	private Set<Action> COLLABORATOR_ACTIVE_ACTIONS = new HashSet<>(Arrays.asList(REPLY, SUSPEND, EDIT, DELETE));

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		roleConsumer = new Role(BaseRoles.CONSUMER);
		roleCollaborator = new Role(BaseRoles.COLLABORATOR);
		when(roleRegistry.find(BaseRoles.CONSUMER)).thenReturn(roleConsumer);
		when(roleRegistry.find(BaseRoles.COLLABORATOR)).thenReturn(roleCollaborator);

		when(authorityService.isAdminOrSystemUser(any())).thenReturn(Boolean.FALSE);
		roleEvaluatorManagerService.set(evaluatorManagerServiceMock);

		userYoda();
		annotationCreatorYoda();
		annotationCreatorDarthVader();

	}

	private Resource userYoda() {
		Resource yoda = new EmfUser();
		yoda.setName("Yoda");
		return yoda;
	}

	private Annotation annotationCreatorYoda() {
		Annotation annotation = new Annotation();
		annotation.add(EMF.CREATED_BY.toString(), "Yoda");
		return annotation;
	}

	private Annotation annotationCreatorDarthVader() {
		Annotation annotation = new Annotation();
		annotation.add(EMF.CREATED_BY.toString(), "Darth Vader");
		return annotation;
	}

	@Test
	public void testSimpleAnnotation() {
		assertEquals(roleEvaluator.evaluateInternal(new Annotation(), null, null).getFirst(), roleConsumer);
	}

	@Test
	public void testEvaluateInternalWithDifferentUsers() {
		when(resourceService.areEqual(eq("Darth Vader"), eq(userYoda()))).thenReturn(Boolean.FALSE);
		assertEquals(roleEvaluator.evaluateInternal(annotationCreatorDarthVader(), userYoda(), null).getFirst(),
				roleConsumer);
	}

	@Test
	public void testEvaluateInternalWithEqualUsers() {
		when(resourceService.areEqual(eq("Yoda"), eq(userYoda()))).thenReturn(Boolean.TRUE);
		assertEquals(roleEvaluator.evaluateInternal(annotationCreatorYoda(), userYoda(), null).getFirst(),
				roleCollaborator);
	}

	@Test
	public void testEvaluateInternalWithDifferentUsersWithAnnotationTopic() {
		when(resourceService.areEqual(eq("Yoda"), eq(userYoda()))).thenReturn(Boolean.TRUE);

		Annotation annotation = new Annotation();
		annotation.setTopic(annotationCreatorYoda());

		assertEquals(annotation.getTopic(), annotationCreatorYoda());
		assertEquals(roleEvaluator.evaluateInternal(annotation, userYoda(), null).getFirst(), roleCollaborator);
	}

	@Test
	public void testFilterInternalCollaboratorActions() {
		Annotation annotation = new Annotation();
		roleEvaluator.filterInternal(annotation, null, roleCollaborator, COLLABORATOR_ACTIVE_ACTIONS);
		assertEquals(COLLABORATOR_ACTIVE_ACTIONS.size(), 4);
	}

	@Test
	public void testFilterInternalReplyCollaboratorActions() {
		Annotation annotation = new Annotation();
		annotation.add((EMF.PREFIX + ":" + EMF.REPLY_TO.getLocalName()), "emf:123");
		roleEvaluator.filterInternal(annotation, null, roleCollaborator, COLLABORATOR_ACTIVE_ACTIONS);
		assertEquals(COLLABORATOR_ACTIVE_ACTIONS.size(), 2);
	}

	@Test
	public void testFilterInternalReplyCollaboratorOverUserActions() {
		when(resourceService.areEqual(eq("Yoda"), eq(userYoda()))).thenReturn(Boolean.TRUE);
		Annotation annotation = new Annotation();
		annotation.setTopic(annotationCreatorYoda());
		annotation.add((EMF.PREFIX + ":" + EMF.REPLY_TO.getLocalName()), "emf:123");
		roleEvaluator.filterInternal(annotation, userYoda(), roleCollaborator, COLLABORATOR_ACTIVE_ACTIONS);
		assertEquals(COLLABORATOR_ACTIVE_ACTIONS.size(), 1);
	}

}
