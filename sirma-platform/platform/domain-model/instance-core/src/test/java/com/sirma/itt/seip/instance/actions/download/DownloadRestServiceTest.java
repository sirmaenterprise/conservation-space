package com.sirma.itt.seip.instance.actions.download;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link DownloadRestService}.
 *
 * @author A. Kunchev
 */
public class DownloadRestServiceTest {

	@InjectMocks
	private DownloadRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		service = new DownloadRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void getDownloadLink() {
		DownloadRequest request = new DownloadRequest();
		service.getDownloadLink(request);
		verify(actions).callAction(request);
	}

}
