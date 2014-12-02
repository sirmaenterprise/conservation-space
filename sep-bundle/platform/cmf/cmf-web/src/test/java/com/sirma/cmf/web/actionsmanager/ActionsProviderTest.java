package com.sirma.cmf.web.actionsmanager;

import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.cmf.CMFTest;
import com.sirma.itt.cmf.beans.model.CaseInstance;
import com.sirma.itt.cmf.constants.allowed_action.ActionTypeConstants;
import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.security.AuthorityService;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.web.config.InstanceProvider;

/**
 * Test for ActionsProvider class.
 * 
 * @author svelikov
 */
@Test
public class ActionsProviderTest extends CMFTest {

	private static final String WRONG_ID = "wrongId";
	private final ActionsProvider provider;
	private final CaseInstance caseInstance;
	private final AuthorityService authorityService;
	private final InstanceProvider instanceProvider;

	/**
	 * Instantiates a new actions provider test.
	 */
	public ActionsProviderTest() {
		provider = new ActionsProvider();

		caseInstance = createCaseInstance(Long.valueOf(1L));
		caseInstance.setId("1");

		ReflectionUtils.setField(provider, "log", SLF4J_LOG);
		ReflectionUtils.setField(provider, "timeTracker", new TimeTracker());

		instanceProvider = Mockito.mock(InstanceProvider.class);
		ReflectionUtils.setField(provider, "instanceProvider", instanceProvider);
		Mockito.when(instanceProvider.fetchInstance(WRONG_ID, null)).thenReturn(null);
		Mockito.when(instanceProvider.fetchInstance(Mockito.anyString(), Mockito.anyString()))
				.thenReturn(caseInstance);

		authorityService = Mockito.mock(AuthorityService.class);
		ReflectionUtils.setField(provider, "authorityService", authorityService);

		ActionContext actionContext = Mockito.mock(ActionContext.class);
		ReflectionUtils.setField(provider, "actionContext", actionContext);
	}

	/**
	 * Test for evaluateActionsByInstance method.
	 */
	public void evaluateActionsByInstanceTest() {
		// expect empty list when all arguments are null
		List<Action> actionsByInstance = provider.evaluateActionsByInstance(null, null, null);
		Assert.assertTrue(actionsByInstance.isEmpty());

		// expect empty list when placeholder is null
		actionsByInstance = provider.evaluateActionsByInstance(caseInstance, null, null);
		Assert.assertTrue(actionsByInstance.isEmpty());

		// expect empty list when placeholder is empty string
		actionsByInstance = provider.evaluateActionsByInstance(caseInstance, "", null);
		Assert.assertTrue(actionsByInstance.isEmpty());

		// all is ok - for the test the service just returns empty collection
		actionsByInstance = provider.evaluateActionsByInstance(caseInstance, "actions-placeholder",
				null);
		Assert.assertEquals(actionsByInstance.get(0).getActionId(),
				ActionTypeConstants.NO_PERMISSIONS);

		// we expect no permission message when backend service can't find instance
		caseInstance.setId(WRONG_ID);
		actionsByInstance = provider.evaluateActionsByInstance(caseInstance, "actions-placeholder",
				null);
		Assert.assertEquals(actionsByInstance.get(0).getActionId(),
				ActionTypeConstants.NO_PERMISSIONS);
	}

	/**
	 * Test for loadDashletActions method.
	 */
	public void loadDashletActionsTest() {
		List<Action> dashletActions = provider.loadDashletActions(null);
		Assert.assertTrue(dashletActions.isEmpty());
	}
}
