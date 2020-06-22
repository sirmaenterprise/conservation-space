package com.sirma.sep.instance.evaluators;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.testng.annotations.Test;

import com.sirma.itt.emf.web.application.ApplicationConfigurationProvider;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;

/**
 * Test the {@link SearchLinkExpressionEvaluator}.
 *
 * @author cdimitrov
 */
@Test
public class SearchLinkExpressionEvaluatorTest extends BaseEvaluatorTest {

	private static final String RESULT_EXPECTED = "PATH_TO_SEARCH/ID";
	private static final String RESULT_BLANK = "javascript:void(0)";
	private static final String PATH_TO_SEARCH = "PATH_TO_SEARCH";
	private static final String ID = "ID";

	@Mock
	private ApplicationConfigurationProvider applicationConfig;

	@InjectMocks
	private SearchLinkExpressionEvaluator searchEvaluator;

	@Test
	public void testSearchEvaluator_withProperParams() {
		when(applicationConfig.getUi2SearchOpenUrl()).thenReturn(PATH_TO_SEARCH);

		Instance instance = SearchLinkExpressionEvaluatorTest.getInstance();
		String script = SearchLinkExpressionEvaluatorTest.buildScript("currentInstance");
		ExpressionContext context = createManager().createDefaultContext(instance, null, null);

		assertEquals(RESULT_EXPECTED, searchEvaluator.evaluate(script, context).toString());
	}

	@Test
	public void testSearchEvaluator_nullable() {
		when(applicationConfig.getUi2SearchOpenUrl()).thenReturn(PATH_TO_SEARCH);

		Instance instance = SearchLinkExpressionEvaluatorTest.getInstance();
		String script = SearchLinkExpressionEvaluatorTest.buildScript(null);
		ExpressionContext context = createManager().createDefaultContext(instance, null, null);

		assertEquals(RESULT_BLANK, searchEvaluator.evaluate(script, context).toString());
	}

	@Test
	public void testSearchEvaluator_emptyContext() {
		when(applicationConfig.getUi2SearchOpenUrl()).thenReturn(PATH_TO_SEARCH);

		String script = SearchLinkExpressionEvaluatorTest.buildScript(null);
		ExpressionContext context = createManager().createDefaultContext(null, null, null);

		assertEquals(RESULT_BLANK, searchEvaluator.evaluate(script, context).toString());
	}

	private static Instance getInstance() {
		Instance instance = new EmfInstance();
		instance.setId(ID);
		return instance;
	}

	private static String buildScript(String currentInstance) {
		StringBuilder element = new StringBuilder("${searchLink(");
		element.append(currentInstance).append(")}");
		return element.toString();
	}
}
