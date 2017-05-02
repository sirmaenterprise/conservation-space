package com.sirmaenterprise.sep.helprequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test for HelpRequestMessageBodyReader.
 * 
 * @author Boyan Tonchev
 *
 */
@SuppressWarnings("static-method")
public class HelpRequestMessageBodyReaderTest {
	
	private static final String SUBJECT = "subject";
	private static final String TYPE = "RH02";
	private static final String MESSAGE = "content of mail";
	
	/**
	 * Test for method readFrom(...).
	 * 
	 * @throws WebApplicationException
	 * @throws IOException
	 */
	@Test
	public void readFromTest() throws WebApplicationException, IOException {
		//setup test
		JsonObject params = Json.createObjectBuilder().add(HelpRequestMessageBodyReader.JSON_KEY_SUBJECT, SUBJECT)
				.add(HelpRequestMessageBodyReader.JSON_KEY_TYPE, TYPE).add(HelpRequestMessageBodyReader.JSON_KEY_DESCRIPTION, MESSAGE).build();
				
		String json = Json.createObjectBuilder().add(HelpRequestMessageBodyReader.JSON_KEY_PARAMS, params).build().toString();
		HelpRequestMessageBodyReader mailMessageBodyReader = new HelpRequestMessageBodyReader();
		
		//execute tested method
		HelpRequestMessage requestMessage = mailMessageBodyReader.readFrom(null, null, null, null, null, IOUtils.toInputStream(json.toString(), StandardCharsets.UTF_8));
		
		//verification
		Assert.assertEquals(requestMessage.getSubject(), SUBJECT);
		Assert.assertEquals(requestMessage.getType(), TYPE);
		Assert.assertEquals(requestMessage.getDescription(), MESSAGE);
		
	}
	
	/**
	 * Tests method readForm scenario with exception.
	 * @throws WebApplicationException
	 * @throws IOException
	 */
	@Test(expectedExceptions = BadRequestException.class)
	public void readFromBadRequestExceptionTest() throws WebApplicationException, IOException {
		//execute tested method
		new HelpRequestMessageBodyReader().readFrom(null, null, null, null, null, IOUtils.toInputStream("{}", StandardCharsets.UTF_8));
	}
	
	/**
	 * Test method isReadable.
	 * @param type different kind of classes.
	 * @param expectedResult expected result.
	 */
	@Test(dataProvider = "isReadableDP")
	public void isReadableTest(Class<?> type, boolean expectedResult) {
		Assert.assertEquals((new HelpRequestMessageBodyReader()).isReadable(type, null, null, null), expectedResult);
	}
	
	/**
	 * 
	 * @return data provider for isReadableTest.
	 */
	@DataProvider
	public Object[][] isReadableDP() {
		return new Object[][] {
			{HelpRequestMessage.class, true},
			{HelpRequestMessageBodyReaderTest.class, false},
		};
	}
}
