package com.sirma.sep.instance.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/07/2017
 */
public class BatchEntityTest {
	@Test
	public void equals_shouldCheckRequiredFields() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");
		BatchEntity e2 = new BatchEntity("jobName", "jobId", "instanceId");

		assertEquals(e1, e2);
	}

	@Test
	public void equals_shouldRejectDifferentObjects() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");

		assertFalse(e1.equals(new Object()));
	}

	@Test
	public void equals_shouldRejectDifferentPrimaryIds() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");
		e1.setId(1L);
		BatchEntity e2 = new BatchEntity("jobName", "jobId", "instanceId");
		e2.setId(2L);

		assertFalse(e1.equals(e2));
	}

	@Test
	public void equals_shouldRejectNullObjects() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");

		assertFalse(e1.equals(null));
	}

	@Test
	public void equals_shouldMatchSameReference() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");

		assertTrue(e1.equals(e1));
	}

	@Test
	public void equals_shouldRejectWithDifferentProperties() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");

		assertNotEquals(e1, new BatchEntity("jobName1", "jobId", "instanceId"));
		assertNotEquals(e1, new BatchEntity("jobName", "jobId1", "instanceId"));
		assertNotEquals(e1, new BatchEntity("jobName", "jobId", "instanceId1"));
	}

	@Test
	public void hashCode_shouldCheckRequiredFields() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");
		BatchEntity e2 = new BatchEntity("jobName", "jobId", "instanceId");

		assertEquals(e1.hashCode(), e2.hashCode());
	}

	@Test
	public void hashCode_shouldDifferForChangedProperties() throws Exception {
		BatchEntity e1 = new BatchEntity("jobName", "jobId", "instanceId");

		assertNotEquals(e1.hashCode(), new BatchEntity("jobName1", "jobId", "instanceId").hashCode());
		assertNotEquals(e1.hashCode(), new BatchEntity("jobName", "jobId1", "instanceId").hashCode());
		assertNotEquals(e1.hashCode(), new BatchEntity("jobName", "jobId", "instanceId1").hashCode());
	}

}
