package com.sirma.itt.seip.instance.actions.compare;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link VersionCompareRestService}.
 *
 * @author A. Kunchev
 */
public class VersionCompareRestServiceTest {

	@InjectMocks
	private VersionCompareRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		service = new VersionCompareRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void compareVersions_actionsServiceCalled() {
		VersionCompareRequest request = new VersionCompareRequest();
		service.compareVersions(request);
		verify(actions).callAction(request);
	}

}
