package com.sirmaenterprise.sep.properties.expression.evaluation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceTypeResolver;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Tests for default methods in {@link PropertyValueEvaluator}.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 03/10/2017
 */
public class PropertyValueEvaluatorTest {

	private static final String SINGLE_VALUE_PROPERTY_NAME = "single-value";
	private static final String MULTI_VALUE_PROPERTY_NAME = "multi-value";

	@InjectMocks
	private PropertyValueEvaluator cut;
	@Mock
	private Instance instance;
	@Mock
	private InstanceTypeResolver typeResolver;

	@Before
	public void init() {
		cut = new PropertyValueEvaluatorMock();
		MockitoAnnotations.initMocks(this);
		when(instance.get(SINGLE_VALUE_PROPERTY_NAME)).thenReturn("some-string");
		when(instance.get(MULTI_VALUE_PROPERTY_NAME)).thenReturn((Serializable) Arrays.asList("value1", "value2"));
	}

	@Test
	public void testSingleValue() {
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		when(typeResolver.resolveInstances(captor.capture())).thenReturn(Collections.singletonList(mock(Instance
				.class)));
		cut.getInstances(instance, SINGLE_VALUE_PROPERTY_NAME, typeResolver);
		List<Object> asd = new ArrayList(captor.getValue());
		assertEquals(1, asd.size());
		verify(typeResolver).resolveInstances(any(Collection.class));
	}

	@Test
	public void testMultiValued() {
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		when(typeResolver.resolveInstances(captor.capture())).thenReturn(Arrays.asList(mock(Instance
				.class), mock(Instance.class)));
		cut.getInstances(instance, MULTI_VALUE_PROPERTY_NAME, typeResolver);
		assertEquals(2, captor.getValue().size());
		verify(typeResolver).resolveInstances(any(Collection.class));
	}

	class PropertyValueEvaluatorMock implements PropertyValueEvaluator {
		@Override
		public boolean canEvaluate(PropertyDefinition source, PropertyDefinition destination) {
			return false;
		}

		@Override
		public Serializable evaluate(Instance instance, String propertyName) {
			return null;
		}
	}
}