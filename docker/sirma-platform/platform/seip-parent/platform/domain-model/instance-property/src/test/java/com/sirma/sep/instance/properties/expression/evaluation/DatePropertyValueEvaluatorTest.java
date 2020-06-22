package com.sirma.sep.instance.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Date;

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
import com.sirma.sep.instance.properties.expression.evaluation.DatePropertyValueEvaluator;
import com.sirma.sep.instance.properties.expression.evaluation.UserDateConverter;

/**
 * Test for {@link DatePropertyValueEvaluator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 17/07/2017
 */
public class DatePropertyValueEvaluatorTest {

	@InjectMocks
	private DatePropertyValueEvaluator cut;
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
		cut = new DatePropertyValueEvaluator();
		MockitoAnnotations.initMocks(this);

		when(property.getDataType()).thenReturn(dataTypeDefinition);
		when(dataTypeDefinition.getName()).thenReturn(DataTypeDefinition.DATE);
		when(instance.get("fieldName")).thenReturn(NOW);
		when(dateUtil.evaluateDateWithZoneOffset(any())).thenReturn("formatted-date");
	}

	@Test
	public void should_ReturnNull_When_PropertyIsNotSet() {
		Assert.assertNull(cut.evaluate(Mockito.mock(Instance.class), "fieldName"));
	}

	@Test
	public void test_evaluate() {
		Serializable fieldName = cut.evaluate(instance, "fieldName");
		assertEquals("formatted-date", fieldName);
		verify(dateUtil).evaluateDateWithZoneOffset(any());
	}

	@Test
	public void canEvaluateDate_success() {
		PropertyDefinition destination = Mockito.mock(PropertyDefinition.class);
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		when(dataType.getName()).thenReturn(DataTypeDefinition.TEXT);
		when(destination.getDataType()).thenReturn(dataType);
		assertTrue(cut.canEvaluate(property, destination));
	}

	@Test
	public void canEvaluate_fail() {
		verifyCanEvaluate(DataTypeDefinition.BOOLEAN, true);
	}

	@Test
	public void canEvaluateText_fail() {
		verifyCanEvaluate(DataTypeDefinition.TEXT, false);
	}

	@Test
	public void canEvaluateOtherType_fail() {
		verifyCanEvaluate(DataTypeDefinition.BOOLEAN, false);
	}

	private void verifyCanEvaluate(String type, boolean source) {
		PropertyDefinition destination = Mockito.mock(PropertyDefinition.class);
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		when(dataType.getName()).thenReturn(type);
		when(destination.getDataType()).thenReturn(dataType);
		if (source) {
			assertFalse(cut.canEvaluate(property, destination));
		} else {
			assertFalse(cut.canEvaluate(destination, destination));
		}
	}
}