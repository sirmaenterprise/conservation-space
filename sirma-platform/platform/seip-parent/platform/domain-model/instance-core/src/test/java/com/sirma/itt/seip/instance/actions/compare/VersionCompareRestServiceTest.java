package com.sirma.itt.seip.instance.actions.compare;

import java.io.File;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link VersionCompareRestService}.
 *
 * @author A. Kunchev
 * @author yasko
 */
@RunWith(MockitoJUnitRunner.class)
public class VersionCompareRestServiceTest {

	@InjectMocks
	private VersionCompareRestService service;

	@Mock
	private Actions actions;

	@Mock
	private HttpServletResponse response;

	@Before
	public void init() throws URISyntaxException {
		File file = new File(VersionCompareRestServiceTest.class.getResource("/test-file.txt").toURI());
		Mockito.when(actions.callAction(Mockito.any())).thenReturn(file);
	}

	@Test
	public void testSuccessfull() {
		File result = service.compareVersions("1", "Bearer xxx.yyy.zzz", "2", "3");

		Assert.assertNotNull(result);
		Mockito.verify(actions).callAction(Mockito.any());
	}
}
