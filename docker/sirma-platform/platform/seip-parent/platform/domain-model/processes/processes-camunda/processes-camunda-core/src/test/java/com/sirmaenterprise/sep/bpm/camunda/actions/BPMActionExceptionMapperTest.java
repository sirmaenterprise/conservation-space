package com.sirmaenterprise.sep.bpm.camunda.actions;

import static com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionExceptionMapper.LABEL_ID;
import static com.sirmaenterprise.sep.bpm.camunda.actions.BPMActionExceptionMapper.MESSAGE;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.Response;
import java.io.StringReader;

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
		// get the logger you want to suppress
		Logger log4j = Logger.getLogger(BPMActionRuntimeException.class);
		// get root logger and remove all appenders.
		log4j.getLoggerRepository().getRootLogger().removeAllAppenders();
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
