package com.sirma.itt.seip.instance.actions.compare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.version.compare.VersionCompareContext;
import com.sirma.itt.seip.instance.version.compare.VersionCompareException;
import com.sirma.itt.seip.instance.version.compare.VersionCompareService;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.rest.exceptions.ResourceException;
import com.sirma.itt.seip.testutil.CustomMatcher;

/**
 * Test for {@link VersionCompareAction}.
 *
 * @author A. Kunchev
 */
public class VersionCompareActionTest {

	@InjectMocks
	private VersionCompareAction action;

	@Mock
	private VersionCompareService versionCompareService;

	@Before
	public void setup() {
		action = new VersionCompareAction();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getName() {
		assertEquals("compareVersions", action.getName());
	}

	@Test(expected = BadRequestException.class)
	public void perform_failedValidation() {
		when(versionCompareService.compareVersionsContent(any(VersionCompareContext.class)))
				.thenThrow(new IllegalArgumentException());
		action.perform(new VersionCompareRequest());
	}

	@Test(expected = ResourceException.class)
	public void perform_compareFailed() {
		when(versionCompareService.compareVersionsContent(any())).thenThrow(new VersionCompareException());
		action.perform(new VersionCompareRequest());
	}

	@Test
	public void perform_internalServiceCalled_successful() {
		VersionCompareRequest request = new VersionCompareRequest();
		request.setTargetId("instance-id");
		request.setFirstSourceId("instance-id-v1.5");
		request.setSecondSourceId("instance-id-v1.8");
		request.setAuthenticationHeaders(new HashMap<>());
		action.perform(request);
		verify(versionCompareService)
				.compareVersionsContent(argThat(CustomMatcher.of((VersionCompareContext context) -> {
					assertEquals("instance-id", context.getOriginalInstanceId());
					assertEquals("instance-id-v1.5", context.getFirstIdentifier());
					assertEquals("instance-id-v1.8", context.getSecondIdentifier());
					assertNotNull(context.getAuthenticationHeaders());
				})));
	}

}
