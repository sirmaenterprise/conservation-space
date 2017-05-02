package com.sirmaenterprise.sep.helprequest;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests for HelpRequestRestService.
 * 
 * @author Boyan Tonchev
 *
 */
public class HelpRequestRestServiceTest {

	@Mock
	private HelpRequestService helpRequestService;
	
	@InjectMocks
	private HelpRequestRestService helpRequestRestService;
	
	@BeforeMethod
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void sendHelpRequestTest() {
		HelpRequestMessage message = Mockito.mock(HelpRequestMessage.class);
		
		//execute tested method.
		helpRequestRestService.sendHelpRequest(message);
		
		//verification
		Mockito.verify(helpRequestService).sendHelpRequest(message);
	}

}
