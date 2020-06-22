package com.sirma.sep.instance.evaluators;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.ResourceService;

/**
 * Test class for user link evaluation.
 *
 * @author cdimitrov
 */
@RunWith(MockitoJUnitRunner.class)
public class UserLinkExpressionEvaluatorTest extends BaseEvaluatorTest {

	private static final String USER_LINK_PATTERN = "${userLink(USERNAME)}";
	private static final String EMPTY_LINK = "javascript:void(0)";
	private static final String USERNAME = "USERNAME";
	private static final String UI2_LINK = "ui2:8080/#/idoc";

	@InjectMocks
	private UserLinkExpressionEvaluator userEvaluator;

	@Mock
	private ResourceService resourceService;

	@Mock
	private ApplicationConfigurationProvider applicationConfigurations;

	@Mock
	private javax.enterprise.inject.Instance<ExpressionsManager> expressionManager;

	@Before
	public void setUp() {
		when(applicationConfigurations.getUi2EntityOpenUrl()).thenReturn(UI2_LINK);
	}

	@Test
	public void testUserLinkEvaluator_withNotExistingUser() {
		String link = userEvaluator.evaluate("${userLink(Unclaimed)}", USERNAME).toString();
		assertEquals(EMPTY_LINK, link);
	}

	@Test
	public void testUserLinkEvaluator_withNull() {
		String link = userEvaluator.evaluate("${userLink(null)}", "").toString();
		assertEquals(EMPTY_LINK, link);
	}

	@Test
	public void testUserLinkEvaluator_withValidUser() {
		String id = "emf:username";
		EmfUser user = createUser(id);

		when(resourceService.findResource(USERNAME)).thenReturn(user);

		String link = userEvaluator.evaluate(USER_LINK_PATTERN, USERNAME).toString();
		assertEquals(link, UI2_LINK + "/" + id);
	}

	@Test
	public void testUserLinkEvaluator_getUserIdFromRule() {
		String id = "emf:username";
		ExpressionsManager expressionsManager = mock(ExpressionsManager.class);

		when(expressionManager.get()).thenReturn(expressionsManager);
		when(expressionsManager.evaluateRule(Matchers.anyString(), Matchers.any(),
				Matchers.any(ExpressionContext.class),
				Matchers.anyVararg())).thenReturn(id);

		String link = userEvaluator.evaluate("${userLink(currentInstance)}", "").toString();
		assertEquals(link, UI2_LINK + "/" + id);
	}

	@Test
	public void testUserLinkEvaluator_getMultipleUserLinks() {
		String id1 = "emf:username1";
		String id2 = "emf:username2";

		when(resourceService.findResource(id1)).thenReturn(createUser(id1));
		when(resourceService.findResource(id2)).thenReturn(createUser(id2));

		String link = userEvaluator.evaluate("${userLink("+id1 + "," + id2 +")}", "").toString();
		assertEquals(link, UI2_LINK + "/" + id1);
	}

	private static EmfUser createUser(String id) {
		EmfUser user = new EmfUser();
		user.setId(id);
		return user;
	}

}
