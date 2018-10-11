package com.sirma.itt.seip.instance.save.expression.evaluation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.expressions.BaseEvaluatorTest;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;
import com.sirma.itt.seip.time.DateRange;
import com.sirma.itt.seip.util.ReflectionUtils;

@Test
public class DueDateExpressionEvaluatorTest extends BaseEvaluatorTest {

	private static final String DUEDATE_EXPRESSION = "${duedate(currentInstance)}";
	private DueDateExpressionEvaluator dueDateEvaluator;
	private TypeConverter converter;

	@BeforeTest
	public void init() {
		dueDateEvaluator = new DueDateExpressionEvaluator();

		converter = Mockito.mock(TypeConverter.class);
		ReflectionUtils.setFieldValue(dueDateEvaluator, "converter", converter);
		ReflectionUtils.setFieldValue(dueDateEvaluator, "contextService", contextService);

	}

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
		workflow.setId("2");
		workflow.setProperties(properties);

		EmfInstance caseInstance = new EmfInstance();
		caseInstance.setIdentifier(DefaultProperties.UNIQUE_IDENTIFIER);
		caseInstance.setId("1");
		contextService.bindContext(workflow, InstanceReferenceMock.createGeneric(caseInstance));

		// executing the test with populated context
		context = expressionsManager.createDefaultContext(workflow, null, null);
		Mockito.when(converter.convert(String.class, dateRange)).thenReturn(dateRange.toString());
		dateRangeResult = (String) dueDateEvaluator.evaluate(DUEDATE_EXPRESSION, context);
		Assert.assertNotNull(dateRangeResult);
		Assert.assertEquals(dateRangeResult, dateRange.toString());

	}
}
