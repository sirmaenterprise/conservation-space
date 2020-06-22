package com.sirmaenterprise.sep.bpm.camunda.service;

import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.DEFAULT_ENGINE;
import static com.sirmaenterprise.sep.bpm.camunda.MockProvider.mockProcessEngine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Objects;

import org.camunda.bpm.engine.ProcessEngines;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

/**
 * Tests {@link SepProcessEngineProvider}
 * 
 * @author bbanchev
 */
public class SepProcessEngineProviderTest {
	@InjectMocks
	private SepProcessEngineProvider sepProcessEngineProvider;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ProcessEngines.destroy();
		ProcessEngines.registerProcessEngine(mockProcessEngine(DEFAULT_ENGINE));
		ProcessEngines.registerProcessEngine(mockProcessEngine("camunda_engine2"));
	}

	@AfterClass
	public static void tearDown() throws Exception {
		ProcessEngines.destroy();
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.SepProcessEngineProvider#getDefaultProcessEngine()}.
	 */
	@Test
	public void testGetDefaultProcessEngine() throws Exception {
		assertNull(sepProcessEngineProvider.getDefaultProcessEngine());
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.SepProcessEngineProvider#getProcessEngine(java.lang.String)}.
	 */
	@Test
	public void testGetProcessEngine() throws Exception {
		assertNotNull(sepProcessEngineProvider.getProcessEngine(DEFAULT_ENGINE));
		assertEquals(DEFAULT_ENGINE, sepProcessEngineProvider.getProcessEngine(DEFAULT_ENGINE).getName());

		assertNotNull(sepProcessEngineProvider.getProcessEngine("camunda_engine2"));
		assertNotEquals(sepProcessEngineProvider.getProcessEngine("camunda_engine2"),
				sepProcessEngineProvider.getProcessEngine(DEFAULT_ENGINE));
	}

	/**
	 * Test method for
	 * {@link com.sirmaenterprise.sep.bpm.camunda.service.SepProcessEngineProvider#getProcessEngineNames()}.
	 */
	@Test
	public void testGetProcessEngineNames() throws Exception {
		assertEquals(Objects.toString(sepProcessEngineProvider.getProcessEngineNames()), 2,
				sepProcessEngineProvider.getProcessEngineNames().size());
	}

}
