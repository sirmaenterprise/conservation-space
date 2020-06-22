package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.mock.InstanceReferenceMock;

/**
 * Test for {@link InstanceReference}
 *
 * @author BBonev
 */
public class InstanceReferencetTest {

	@Test
	public void testInstantiate() throws Exception {
		InstanceReferenceMock ref = new InstanceReferenceMock("ref");

		assertNull(ref.instantiate());

		DataTypeDefinition typeDefinition = mock(DataTypeDefinition.class);
		ref.setReferenceType(typeDefinition);

		assertNull(ref.instantiate());

		when(typeDefinition.getJavaClass()).then(a -> EmfInstance.class);

		ref.setType(InstanceType.create("emf:Case"));

		Instance instance = ref.instantiate();

		assertTrue(instance instanceof EmfInstance);
		assertNotNull(instance);
		assertNotNull(instance.type());
		assertEquals("emf:Case", instance.type().getId());

		assertNull(InstanceReference.instantiate(null));
		assertNull(InstanceReference.instantiate(new InstanceReferenceMock("ref")));
	}

	@Test
	public void testIsValid() throws Exception {
		assertFalse(InstanceReference.isValid(null));
		assertFalse(InstanceReference.isValid(new InstanceReferenceMock(null)));
		InstanceReferenceMock reference = new InstanceReferenceMock("ref");
		assertFalse(InstanceReference.isValid(reference));
		reference.setReferenceType(mock(DataTypeDefinition.class));
		assertTrue(InstanceReference.isValid(reference));
	}

}
