package com.sirma.itt.seip.instance.version.rest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.version.InstanceVersionService;
import com.sirma.itt.seip.instance.version.VersionsResponse;

/**
 * Test for {@link InstanceVersionRestService}.
 *
 * @author A. Kunchev
 */
public class InstanceVersionRestServiceTest {

	@InjectMocks
	private InstanceVersionRestService service;

	@Mock
	private InstanceVersionService instanceVersionService;

	@Before
	public void setup() {
		service = new InstanceVersionRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getVersions_internalServiceCalled() {
		VersionsResponse response = service.getVersions("instance-id", 0, -1);
		verify(instanceVersionService).getInstanceVersions("instance-id", 0, -1);
	}

}
