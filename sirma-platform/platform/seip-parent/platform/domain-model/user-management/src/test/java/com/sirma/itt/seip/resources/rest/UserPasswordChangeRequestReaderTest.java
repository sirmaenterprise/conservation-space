package com.sirma.itt.seip.resources.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.resources.security.UserPasswordChangeRequest;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;

/**
 * Tests for {@link UserPasswordChangeRequestReader}.
 *
 * @author smustafov
 */
public class UserPasswordChangeRequestReaderTest {

	@InjectMocks
	private UserPasswordChangeRequestReader reader;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testIsReadable_wrongType() {
		assertFalse(reader.isReadable(String.class, null, null, null));
	}

	@Test
	public void testIsReadable_correctType() {
		assertTrue(reader.isReadable(UserPasswordChangeRequest.class, null, null, null));
	}

	@Test(expected = BadRequestException.class)
	public void testReadFrom_emptyJson() throws IOException {
		try (InputStream stream = new ByteArrayInputStream("{}".getBytes())) {
			reader.readFrom(UserPasswordChangeRequest.class, null, null, null, null, stream);
		}
	}

	@Test
	public void readFrom_correctRequest() throws IOException {
		String json = IOUtils.toString(new FileInputStream("src/test/resources/user/changePassword.json"));
		try (InputStream stream = new ByteArrayInputStream(json.getBytes())) {
			UserPasswordChangeRequest request = reader.readFrom(UserPasswordChangeRequest.class, null, null, null, null, stream);
			assertEquals("john", request.getUsername());
			assertEquals("123456", request.getOldPassword());
			assertEquals("password123", request.getNewPassword());
		}
	}

}
