package com.sirma.itt.seip.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.UserStore;
import com.sirma.itt.seip.security.configuration.SecurityConfiguration;

/**
 * Tests for {@link ResourceUserStore}.
 *
 * @author smustafov
 */
public class ResourceUserStoreTest {

	@InjectMocks
	private ResourceUserStore userStore;

	@Mock
	private ResourceService resourceService;

	@Mock
	private SecurityConfiguration securityConfiguration;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Before
	public void before() {
		userStore = new ResourceUserStore();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testSetRequestProperties_withNoTimezone() {
		User user = new EmfUser();
		UserStore.RequestInfo info = mock(UserStore.RequestInfo.class);

		when(systemConfiguration.getSystemLanguage()).thenReturn("en");

		userStore.setRequestProperties(user, info);

		assertEquals("en", user.getProperties().get(ResourceProperties.LANGUAGE));
		assertNull(user.getProperties().get(ResourceProperties.TIMEZONE));
	}

	@Test
	public void testSetRequestProperties_withTimezone() {
		User user = new EmfUser();
		UserStore.RequestInfo info = mock(UserStore.RequestInfo.class);
		Map<String, List<String>> headers = new HashMap<>();
		headers.put("Timezone", Arrays.asList("Bulgaria/Bulgaira"));

		when(info.getHeaders()).thenReturn(headers);

		userStore.setRequestProperties(user, info);

		assertTrue(user.getProperties().get(ResourceProperties.TIMEZONE) instanceof TimeZone);
	}

}
