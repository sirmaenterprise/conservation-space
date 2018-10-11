package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.mock.InstanceReferenceMock;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Test for {@link EmfInstance}
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class EmfInstanceTest {

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.thenReturn(mock(InstanceReference.class));
	}

	@Test
	public void defaultConstructor_createsEmptyObject() {
		EmfInstance instance = new EmfInstance();
		assertNotNull(instance);
		assertNull(instance.getId());
	}

	@Test
	public void constructorWithId_createsObjectWithId() {
		EmfInstance instance = new EmfInstance("instance-id");
		assertNotNull(instance);
		assertEquals("instance-id", instance.getId());
	}

	private InstanceReference createInstanceReference(Instance emfInstance) {
		return new InstanceReferenceMock((String) emfInstance.getId(), null, emfInstance.type(), emfInstance);
	}

}
