package com.sirma.itt.cmf.alfresco4.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.adapters.remote.DMSClientException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * Test for {@link GroupAlfresco4Service}.
 *
 * @author A. Kunchev
 */
public class GroupAlfresco4ServiceTest {

	@Mock
	private RESTClient restClient;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@InjectMocks
	private GroupAlfresco4Service service;

	@Before
	public void setup() {
		service = new GroupAlfresco4Service();
		MockitoAnnotations.initMocks(this);

		when(securityConfiguration.getSystemAdminUsername()).thenReturn("SystemAdmin@test.com");
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void getUsersInAuthority_rollbackedException() throws DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class))).then(a -> {
			return new UnsupportedEncodingException();
		});
		service.getUsersInAuthority(new EmfGroup("GROUP_TEST", "TEST"));
	}

	@Test(expected = RollbackedRuntimeException.class)
	public void getUsersInAuthority_DMSClientExceptionWithStatus500() throws DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class))).then(a -> {
			return new DMSClientException("Problem.", 500);
		});
		service.getUsersInAuthority(new EmfGroup("GROUP_TEST", "TEST"));
	}

	@Test
	public void getUsersInAuthority_notFoundGroup() throws DMSClientException {
		when(restClient.request(anyString(), any(HttpMethod.class)))
				.thenThrow(new DMSClientException("Not found.", Status.NOT_FOUND.getStatusCode()));
		List<String> members = service.getUsersInAuthority(new EmfGroup("GROUP_TEST", "TEST"));
		assertEquals(Collections.emptyList(), members);
	}

	@Test
	public void getUsersInAuthority_successful() throws DMSClientException, IOException {
		try (InputStream stream = GroupAlfresco4Service.class
				.getClassLoader()
					.getResourceAsStream("retrieve-group-members-test.txt")) {
			when(restClient.request(anyString(), any(HttpMethod.class))).thenReturn(IOUtils.toString(stream));
			List<String> members = service.getUsersInAuthority(new EmfGroup("GROUP_TEST", "TEST"));
			assertEquals(2, members.size());
		}
	}

}
