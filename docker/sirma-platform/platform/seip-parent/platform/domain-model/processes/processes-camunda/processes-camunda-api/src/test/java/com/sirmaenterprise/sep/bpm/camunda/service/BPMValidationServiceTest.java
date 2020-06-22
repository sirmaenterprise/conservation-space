package com.sirmaenterprise.sep.bpm.camunda.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BPMValidationServiceTest {

	@Test
	public void testProcessEngineStatus() {
		ProcessEngineStatus withoutMessage = ProcessEngineStatus.AVAILABLE;
		assertEquals("", withoutMessage.getMessage());
		ProcessEngineStatus withMessage = ProcessEngineStatus.AVAILABLE.withMessage("test1");
		assertEquals("test1", withMessage.getMessage());
		assertEquals(0, withMessage.compareTo(withoutMessage));
		assertEquals(withMessage.hashCode(), withoutMessage.hashCode());
		assertTrue(withMessage.equals(withoutMessage));
		ProcessEngineStatus unAvailable = ProcessEngineStatus.UNAVAILABLE;
		assertNotEquals(withMessage, unAvailable);
		assertNotEquals(0, withMessage.compareTo(unAvailable));
		assertNotEquals(withMessage.hashCode(), unAvailable.hashCode());

	}

}
