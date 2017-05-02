package com.sirma.itt.seip.instance.actions.save;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test the upload new version rest service.
 *
 * @author nvelkov
 */
public class CreateOrUpdateRestServiceTest {

	@Mock
	private Actions actions;

	@InjectMocks
	private CreateOrUpdateRestService restService = new CreateOrUpdateRestService();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testUploadNewVersion() {
		restService.createOrUpdate(new CreateOrUpdateRequest());
		Mockito.verify(actions).callAction(Matchers.any(CreateOrUpdateRequest.class));
	}
}
