package com.sirma.itt.emf.semantic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.json.JsonObject;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.context.SecurityContext;

/**
 * Test for {@link RepositoryConnectionMonitor}
 *
 * @author BBonev
 */
public class RepositoryConnectionMonitorTest {

	@InjectMocks
	private RepositoryConnectionMonitor monitor;
	@Mock
	private SecurityContext securityContext;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		User user = mock(User.class);
		when(user.getSystemId()).thenReturn("userId");
		when(securityContext.getAuthenticated()).thenReturn(user);
		when(securityContext.getRequestId()).thenReturn("requestid");
	}

	@Test
	public void shouldReportBrokenInfoOnContextChange() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant1", "tenant2");
		monitor.onNewConnection(1, true);
		monitor.onNewConnection(2, true);

		JsonObject info = monitor.getInfo();
		assertEquals(1, info.getJsonArray("broken").size());
		assertEquals(1, info.getJsonObject("active").size());
	}

	@Test
	public void shouldReportBrokenInfoOnConnectionUpgreade() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant1", "tenant2");
		monitor.upgreadConnectionToWrite(1);
		monitor.upgreadConnectionToWrite(2);

		JsonObject info = monitor.getInfo();
		assertEquals(1, info.getJsonArray("broken").size());
		assertEquals(1, info.getJsonObject("active").size());
	}

	@Test
	public void shouldClearLoggedInfoOnClose() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant");
		monitor.onNewConnection(1, true);

		assertEquals(1, monitor.getInfo().getJsonObject("active").size());
		monitor.onConnectionClose(1);
		assertEquals(0, monitor.getInfo().getJsonObject("active").size());
	}

	@Test
	public void shouldReportBrokenConnectionsOnCloseIfDifferentTenant() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant1", "tenant1", "tenant2");
		monitor.onNewConnection(1, true);
		monitor.onConnectionClose(1);

		JsonObject info = monitor.getInfo();
		assertEquals(1, info.getJsonArray("broken").size());
		assertEquals(0, info.getJsonObject("active").size());
	}

	@Test
	public void upgreadShouldSetUpgreadedConnectionFlag() throws Exception {
		when(securityContext.getCurrentTenantId()).thenReturn("tenant1");
		monitor.onNewConnection(1, false);
		monitor.upgreadConnectionToWrite(1);

		JsonObject info = monitor.getInfo();
		assertEquals(0, info.getJsonArray("broken").size());
		assertTrue(info
				.getJsonObject("active")
					.getJsonObject(Thread.currentThread().getName())
					.getJsonArray("connections")
					.getJsonObject(0)
					.getBoolean("upgraded"));
	}
}
