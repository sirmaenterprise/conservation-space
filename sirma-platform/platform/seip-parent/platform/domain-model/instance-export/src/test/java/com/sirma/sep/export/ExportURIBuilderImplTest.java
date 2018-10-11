package com.sirma.sep.export;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;

/**
 * Test for {@link ExportURIBuilderImpl}.
 *
 * @author A. Kunchev
 */
public class ExportURIBuilderImplTest {

	@InjectMocks
	private ExportURIBuilder builder;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private SecurityTokensManager securityTokensManager;

	@Before
	public void setup() {
		builder = new ExportURIBuilderImpl();
		MockitoAnnotations.initMocks(this);
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateURI_emptyInstanceId() {
		builder.generateURI("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateURI_nullInstanceId() {
		builder.generateURI(null);
	}

	@Test
	public void generateURI_defaultToken() {
		when(securityTokensManager.getCurrentJwtToken()).thenReturn(Optional.of("current-user-jwt-token"));
		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>("http://localhost:5000/"));
		URI generatedURI = builder.generateURI("instance-id", null);
		assertEquals("http://localhost:5000/#/idoc/instance-id?jwt=current-user-jwt-token&mode=print",
				generatedURI.toString());
	}

	@Test
	public void generateURIForTabs_withTokenAndTabs() {
		when(securityTokensManager.getCurrentJwtToken()).thenReturn(Optional.of("current-user-jwt-token"));
		when(systemConfiguration.getUi2Url()).thenReturn(new ConfigurationPropertyMock<>("http://localhost:5000/"));
		URI generatedURI = builder.generateURIForTabs(Arrays.asList("tab-1", "tab-2"), "instance-id", "user-token");
		assertEquals("http://localhost:5000/#/idoc/instance-id?tab=tab-1&tab=tab-2&jwt=user-token&mode=print",
				generatedURI.toString());
	}

	@Test(expected = SecurityException.class)
	public void getCurrentJwtToken_noAuthenticatedUser() {
		when(securityTokensManager.getCurrentJwtToken()).thenReturn(Optional.empty());
		builder.getCurrentJwtToken();
	}

	@Test
	public void getCurrentJwtToken_withAuthenticatedUser() {
		when(securityTokensManager.getCurrentJwtToken()).thenReturn(Optional.of("current-user-token"));
		assertEquals("current-user-token", builder.getCurrentJwtToken());
	}

}