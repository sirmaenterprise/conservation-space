package com.sirma.cmf.web.evaluation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.time.DateRange;

/**
 * Test for due date evaluator.
 *
 * @author cdimitrov
 */
@Test
public class DueDateExpressionEvaluatorTest extends BaseEvaluatorTest {

	private static final String DUEDATE_EXPRESSION = "${duedate(currentInstance)}";
	private DueDateExpressionEvaluator dueDateEvaluator;
	private TypeConverter converter;

	/**
	 * Initialize required test components.
	 */
	@BeforeTest
	public void init() {
		dueDateEvaluator = new DueDateExpressionEvaluator();
		converter = Mockito.mock(TypeConverter.class);
		ReflectionUtils.setField(dueDateEvaluator, "converter", converter);
	}

	/**
	 * Test method for due date evaluator.
	 */
	public void evalaluateDueDateTest() {
		ExpressionsManager expressionsManager = createManager();

		// expecting empty result, because missing context
		ExpressionContext context = expressionsManager.createDefaultContext(null, null, null);
		String dateRangeResult = (String) dueDateEvaluator.evaluate(DUEDATE_EXPRESSION, context);
		Assert.assertNull(dateRangeResult);

		Map<String, Serializable> properties = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		DateRange dateRange = new DateRange(calendar.getTime(), calendar.getTime());
		properties.put(DefaultProperties.PLANNED_END_DATE, dateRange);

		EmfInstance workflow = new EmfInstance();
		workflow.setIdentifier(DefaultProperties.UNIQUE_IDENTIFIER);
		workflow.setId(2L);
		workflow.setProperties(properties);

		EmfInstance caseInstance = new EmfInstance();
		caseInstance.setIdentifier(DefaultProperties.UNIQUE_IDENTIFIER);
		caseInstance.setId(1L);
		workflow.setOwningInstance(caseInstance);

		// executing the test with populated context
		context = expressionsManager.createDefaultContext(workflow, null, null);
		Mockito.when(converter.convert(String.class, dateRange)).thenReturn(dateRange.toString());
		dateRangeResult = (String) dueDateEvaluator.evaluate(DUEDATE_EXPRESSION, context);
		Assert.assertNotNull(dateRangeResult);
		Assert.assertEquals(dateRangeResult, dateRange.toString());

	}
}
