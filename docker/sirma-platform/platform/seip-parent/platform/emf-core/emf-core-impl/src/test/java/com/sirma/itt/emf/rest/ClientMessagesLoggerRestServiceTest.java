package com.sirma.itt.emf.rest;

import org.testng.annotations.Test;

import com.sirma.itt.seip.domain.rest.BadRequestException;

/**
 * Tests for rest service that logs client messages.
 *
 * @author svelikov
 */
public class ClientMessagesLoggerRestServiceTest {

	private ClientMessagesLoggerRestService service;

	/**
	 * Instantiates a new client messages logger rest service test.
	 */
	public ClientMessagesLoggerRestServiceTest() {

		service = new ClientMessagesLoggerRestService();
	}

	/**
	 * Log web errors_no_data.
	 */
	@Test(expectedExceptions = BadRequestException.class)
	public void logWebErrors_no_data() {
		service.logWebErrors(null);
	}

}
