package com.sirmaenterprise.sep.bpm.camunda.util;

import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.extractBusinessId;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.getTypeAndSubtype;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.isSkipped;
import static com.sirmaenterprise.sep.bpm.camunda.util.ActivityIdUtil.markSkipped;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActivityIdUtilTest {

	@Test
	public void testIsSkipped() throws Exception {
		assertTrue(isSkipped("skip_"));
		assertTrue(isSkipped("skip_id"));
		assertFalse(isSkipped("notSkipped"));
		assertFalse(isSkipped("any_id"));
	}

	@Test
	public void testMarkSkipped() throws Exception {
		assertEquals("skip_id", markSkipped("id"));
		assertEquals("skip_id", markSkipped("skip_id"));
	}

	@Test
	public void testExtractBusinessId() throws Exception {

		assertEquals("mytask", extractBusinessId("mytask_id"));
		assertEquals("mytask", extractBusinessId("mytask"));
		assertEquals("mytask-id", extractBusinessId("mytask-id"));
	}

	@Test
	public void testGetTypeAndSubtype() throws Exception {
		assertEquals("mytask", getTypeAndSubtype("mytask-id")[0]);
		assertEquals("id", getTypeAndSubtype("mytask-id")[1]);
		assertEquals(1, getTypeAndSubtype("mytask=id").length);
		assertEquals("mytask=id", getTypeAndSubtype("mytask=id")[0]);
	}

}
