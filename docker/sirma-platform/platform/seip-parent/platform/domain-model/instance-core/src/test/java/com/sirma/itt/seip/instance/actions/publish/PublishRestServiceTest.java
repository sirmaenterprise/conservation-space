package com.sirma.itt.seip.instance.actions.publish;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test for {@link PublishRestService}
 *
 * @author BBonev
 */
public class PublishRestServiceTest {

	@InjectMocks
	private PublishRestService publishRestService;

	@Mock
	private Actions actions;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldCallPublish() throws Exception {
		publishRestService.publish(new PublishActionRequest());
		verify(actions).callAction(any(PublishActionRequest.class));
	}

	@Test
	public void shouldCallPublishAsPdf() throws Exception {
		publishRestService.publishAsPdf(new PublishAsPdfActionRequest());
		verify(actions).callAction(any(PublishAsPdfActionRequest.class));
	}
}
