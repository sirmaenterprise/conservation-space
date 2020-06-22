package com.sirma.itt.seip.instance.save.expression.evaluation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionEvaluator;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.instance.save.expression.evaluation.InstanceLinkExpressionEvaluator;
import com.sirma.itt.seip.instance.util.LinkProviderService;

/**
 * Test class for instance link evaluator.
 *
 * @author cdimitrov
 */
@Test
public class InstanceLinkExpressionEvaluatorTest extends BaseEvaluatorTest {

	private static final String LINK_PATH = "LINK_PATH";
	private static final String EMPTY_LINK = "javascript:void(0)";
	private static final String LINK_PATTERN = "${link(currentInstance)}";
	@InjectMocks
	private InstanceLinkExpressionEvaluator instanceLinkEvaluator;
	@Mock
	private LinkProviderService linkProviderService;

	@Override
	@BeforeMethod
	public void beforeMethod() {
		super.beforeMethod();
		when(linkProviderService.buildLink(any(Instance.class))).thenReturn(LINK_PATH);
		when(linkProviderService.buildLink(any(String.class))).thenReturn(LINK_PATH);
	}

	@Override
	protected List<ExpressionEvaluator> initializeEvaluators(ExpressionsManager manager,
			TypeConverter converter) {
		List<ExpressionEvaluator> evaluators = super.initializeEvaluators(manager, converter);
		evaluators.add(initEvaluator(instanceLinkEvaluator, manager, converter));
		return evaluators;
	}

	/**
	 * Test method for instance link evaluator.
	 */
	public void instanceLinkEvaluatorTest() {
		ExpressionsManager expressionManager = createManager();
		ExpressionContext expressionContext = expressionManager.createDefaultContext(null, null, null);
		String link = expressionManager.evaluateRule(LINK_PATTERN, String.class, expressionContext);

		// empty context, should return empty link
		Assert.assertEquals(link, EMPTY_LINK);

		Instance instance = new EmfInstance();
		instance.setId("Id");
		instance.setIdentifier(DefaultProperties.UNIQUE_IDENTIFIER);
		expressionContext = expressionManager.createDefaultContext(instance, null, null);
		link = expressionManager.evaluateRule(LINK_PATTERN, String.class, expressionContext);

		// result based on populated result, should return generated link path
		Assert.assertNotNull(link);
		Assert.assertEquals(link, LINK_PATH);
	}

	@Test
	public void test_directUriLink() {
		ExpressionsManager expressionManager = createManager();
		ExpressionContext expressionContext = expressionManager.createDefaultContext(null, null, null);
		String link = expressionManager.evaluateRule("${link(emf:instance-id)}", String.class, expressionContext);
		assertNotNull(link);
		assertEquals(link, LINK_PATH);
	}
}
