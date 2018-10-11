package com.sirma.itt.emf.semantic.persistence;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.sirma.itt.seip.testutil.mocks.InstanceReferenceMock;

/**
 * Test for {@link ChangedInstanceBuffer}
 *
 * @author BBonev
 */
public class ChangedInstanceBufferTest {

	private static final String INSTANCE_ID = "emf:instanceId";

	@Test
	public void testAddChange_instance() throws Exception {
		ChangedInstanceBuffer buffer = new ChangedInstanceBuffer();
		buffer.addChange(InstanceReferenceMock.createGeneric(INSTANCE_ID).toInstance());

		assertArrayEquals(Arrays.asList(INSTANCE_ID).toArray(), buffer.getChanges().toArray());
	}

	@Test
	public void testAddChange_reference() throws Exception {
		ChangedInstanceBuffer buffer = new ChangedInstanceBuffer();
		buffer.addChange(InstanceReferenceMock.createGeneric(INSTANCE_ID));

		assertArrayEquals(Arrays.asList(INSTANCE_ID).toArray(), buffer.getChanges().toArray());
	}

	@Test
	public void testAddChange_id() throws Exception {
		ChangedInstanceBuffer buffer = new ChangedInstanceBuffer();
		buffer.addChange(INSTANCE_ID);

		assertArrayEquals(Arrays.asList(INSTANCE_ID).toArray(), buffer.getChanges().toArray());
	}

	@Test
	public void testGetAndReset() throws Exception {
		ChangedInstanceBuffer buffer = new ChangedInstanceBuffer();
		buffer.addChange(INSTANCE_ID);

		assertArrayEquals(Arrays.asList(INSTANCE_ID).toArray(), buffer.getChangesAndReset().toArray());
		assertArrayEquals(Collections.emptyList().toArray(), buffer.getChangesAndReset().toArray());
	}
}
