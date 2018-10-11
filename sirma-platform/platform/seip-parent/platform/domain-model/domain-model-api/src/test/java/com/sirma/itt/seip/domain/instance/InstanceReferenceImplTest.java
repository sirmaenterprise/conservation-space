package com.sirma.itt.seip.domain.instance;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertNull;

import org.junit.Test;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.mock.InstanceReferenceMock;

/**
 * @author bbanchev
 */
public class InstanceReferenceImplTest {

	@Test
	public void testHashCode() throws Exception {
		InstanceReferenceMock ref1 = new InstanceReferenceMock("id1", null);
		InstanceReferenceMock ref2 = new InstanceReferenceMock("id1", null);
		assertEquals(ref1.hashCode(), ref2.hashCode());
		DataTypeDefinition ref1Type = mock(DataTypeDefinition.class);
		DataTypeDefinition ref2Type = ref1Type;
		ref1 = new InstanceReferenceMock("id1", ref1Type);
		ref2 = new InstanceReferenceMock("id1", ref2Type);
		assertEquals(ref1.hashCode(), ref2.hashCode());
	}

	@Test
	public void testEquals() throws Exception {
		InstanceReferenceMock ref1 = new InstanceReferenceMock("id1", null);
		InstanceReferenceMock ref2 = new InstanceReferenceMock("id1", null);
		assertEquals(ref1, ref2);
		DataTypeDefinition ref1Type = mock(DataTypeDefinition.class);
		DataTypeDefinition ref2Type = ref1Type;
		ref1 = new InstanceReferenceMock("id1", ref1Type);
		ref1.setType(mock(InstanceType.class));
		ref2 = new InstanceReferenceMock("id1", ref2Type);
		assertEquals(ref1, ref2);
	}

	@Test
	public void testGetId() throws Exception {
		InstanceReferenceMock ref1 = new InstanceReferenceMock("id1", null);
		assertEquals("id1", ref1.getId());
		// proxy id
		EmfInstance emfInstance = new EmfInstance("id");
		ref1 = new InstanceReferenceMock(null, null, null, emfInstance);
		assertEquals("id", ref1.getId());

		ref1 = new InstanceReferenceMock(null, null, null, null);
		assertNull(ref1.getId());
	}

	@Test
	public void testSetId() throws Exception {
		EmfInstance emfInstance = new EmfInstance();
		InstanceReferenceMock ref1 = new InstanceReferenceMock(null, null, null, emfInstance);
		assertNull(ref1.getId());
		// set diff id at first
		emfInstance.setId("id");
		// now ref and instance should be updated
		ref1.setId("id1");
		assertEquals("id1", ref1.getId());
		assertEquals("id1", emfInstance.getId());
	}

	@Test
	public void testReferenceType() throws Exception {
		DataTypeDefinition ref1Type = mock(DataTypeDefinition.class);
		InstanceReferenceMock ref1 = new InstanceReferenceMock("id1", ref1Type);
		assertEquals(ref1.getReferenceType(), ref1Type);
		DataTypeDefinition ref2Type = mock(DataTypeDefinition.class);
		ref1.setReferenceType(ref2Type);
		assertEquals(ref1.getReferenceType(), ref2Type);
	}

	@Test
	public void testType() throws Exception {
		EmfInstance emfInstance = new EmfInstance();
		InstanceType type = mock(InstanceType.class);
		InstanceReferenceMock ref1 = new InstanceReferenceMock(null, null, type, emfInstance);
		assertEquals(type, ref1.getType());

		ref1 = new InstanceReferenceMock(null, null, null, emfInstance);

		ref1.setType(type);
		// now ref and instance should be updated
		assertEquals(type, emfInstance.type());
		assertEquals(type, ref1.getType());
		ref1.setType(null);
		assertNull(emfInstance.type());
		assertNull(ref1.getType());

		ref1 = new InstanceReferenceMock(null, null, null, null);
		assertNull(ref1.getType());

	}

}
