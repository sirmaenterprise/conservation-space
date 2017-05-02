package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.exception.EmfRuntimeException;

/**
 * Test for {@link EmfInstance}
 *
 * @author BBonev
 */
public class EmfInstanceTest {

	@Mock
	private TypeConverter typeConverter;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(typeConverter.convert(eq(InstanceReference.class), any(Instance.class)))
				.thenReturn(mock(InstanceReference.class));
	}

	@Test(expected = EmfRuntimeException.class)
	public void testSetOwningReference_rootReference() throws Exception {
		new EmfInstance().setOwningReference(InstanceReference.ROOT_REFERENCE);
	}

	@Test(expected = EmfRuntimeException.class)
	public void testSetOwningReference_cycleReference() throws Exception {
		InstanceReference reference = mock(InstanceReference.class);
		EmfInstance instance = new EmfInstance();
		instance.setReference(reference);
		instance.setOwningReference(reference);
	}

	@Test
	public void testSetOwningReference() throws Exception {
		InstanceReference reference = mock(InstanceReference.class);
		EmfInstance instance = new EmfInstance();
		instance.setOwningReference(reference);
		assertNotNull(instance.getOwningReference());
		assertEquals(reference, instance.getOwningReference());
	}

	@Test(expected = EmfRuntimeException.class)
	public void setOwningInstance_parentCycle() throws Exception {
		new EmfInstance().setOwningInstance(new EmfInstance());
	}

	@Test
	public void setOwningInstance_andReferenceParent() throws Exception {

		EmfInstance instance = new EmfInstance();
		instance.setId("emf:instance");
		InstanceReference instanceReference = mock(InstanceReference.class);
		instance.setReference(instanceReference);
		EmfInstance parent = new EmfInstance();
		parent.setId("emf:parent");
		InstanceReference parentReference = mock(InstanceReference.class);
		parent.setReference(parentReference);

		// should handle null values
		instance.setOwningInstance(null);

		instance.setOwningInstance(parent);

		verify(instanceReference).setParent(parentReference);
	}
}
