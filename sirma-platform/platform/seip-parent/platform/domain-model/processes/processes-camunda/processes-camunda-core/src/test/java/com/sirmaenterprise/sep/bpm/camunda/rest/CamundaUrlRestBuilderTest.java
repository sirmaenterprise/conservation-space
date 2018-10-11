package com.sirmaenterprise.sep.bpm.camunda.rest;

import org.camunda.bpm.engine.ProcessEngine;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.junit.Assert;
import org.junit.Before;

public class CamundaUrlRestBuilderTest {

	
	@Mock
	private ProcessEngine processEngine;
	
	@InjectMocks
	private CamundaUrlRestBuilder builder;
	

	@Before
	public void initialize() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testGetEngineName(){
		Mockito.when(processEngine.getName()).thenReturn("tenant");
		String result = builder.generateEngineBaseURI();
		String expectedResult = "/engine/engine/tenant";
		Assert.assertEquals(expectedResult, result);
	}

}
