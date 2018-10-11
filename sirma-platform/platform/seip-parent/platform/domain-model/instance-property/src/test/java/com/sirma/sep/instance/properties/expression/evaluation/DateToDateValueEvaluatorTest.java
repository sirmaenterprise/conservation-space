package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.sep.instance.properties.expression.evaluation.DateToDateValueEvaluator;
import com.sirma.sep.instance.properties.expression.evaluation.UserDateConverter;

/**
 * Test for {@link DateToDateValueEvaluator}.
 *
 * @author S.Djulgerova
 */
public class DateToDateValueEvaluatorTest {

	@InjectMocks
	private DateToDateValueEvaluator evaluator;

	@Mock
	private PropertyDefinition property;

	@Mock
	private DataTypeDefinition dataTypeDefinition;

	@Mock
	private Instance instance;

	@Mock
	private UserDateConverter dateUtil;

	private static final Date NOW = new Date();

	@Before
	public void init() {
		evaluator = new DateToDateValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATETIME);

		when(instance.get("fieldName")).thenReturn(NOW);
		when(dateUtil.evaluateDateWithZoneOffset(any())).thenReturn("formatted-date-time");
	}

	@Test
	public void should_ReturnNull_When_PropertyIsNotSet() {
		Assert.assertNull(evaluator.evaluate(Mockito.mock(Instance.class), "fieldName"));
	}

	@Test
	public void test_evaluate() {
		Serializable fieldName = evaluator.evaluate(instance, "fieldName");
		assertEquals(new DateTime(NOW).withZone(DateTimeZone.UTC).toString(), fieldName);
	}

	@Test
	public void canEvaluateDate_success() {
		PropertyDefinition source = Mockito.mock(PropertyDefinition.class);
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		when(dataType.getName()).thenReturn(DataTypeDefinition.DATE);
		when(source.getDataType()).thenReturn(dataType);
		assertTrue(evaluator.canEvaluate(source, source));
	}

	@Test
	public void canEvaluateDatetime_success() {
		assertTrue(evaluator.canEvaluate(property, property));
	}

	@Test
	public void canEvaluateDate_fail() {
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.BOOLEAN);
		assertFalse(evaluator.canEvaluate(property, null));
	}

	@Test
	public void canEvaluateDatetime_fail() {
		PropertyDefinition source = Mockito.mock(PropertyDefinition.class);
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		when(dataType.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(source.getDataType()).thenReturn(dataType);
		assertFalse(evaluator.canEvaluate(property, source));
	}

}