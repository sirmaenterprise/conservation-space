package com.sirmaenterprise.sep.bpm.camunda.actions;

import static com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionExceptionMapper.LABEL_ID;
import static com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionExceptionMapper.MESSAGE;

import java.io.StringReader;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for {@link BPMActionExceptionMapper}
 * 
 * @author Hristo Lungov
 */
@RunWith(MockitoJUnitRunner.class)
public class BPMActionExceptionMapperTest {

	private static final String TEST_LABEL_ID = "testLabelId";
	private static final String TEST_MESSAGE = "testMessage";
	@InjectMocks
	private BPMActionExceptionMapper bpmActionExceptionMapper;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void test_toResponse() {
		Response response = bpmActionExceptionMapper
				.toResponse(new BPMActionRuntimeException(TEST_MESSAGE, TEST_LABEL_ID));
		JsonObject jsonResponse = Json.createReader(new StringReader(response.getEntity().toString())).readObject();
		Assert.assertEquals(TEST_MESSAGE, jsonResponse.getString(MESSAGE));
		Assert.assertEquals(TEST_LABEL_ID, jsonResponse.getString(LABEL_ID));
	}
}
