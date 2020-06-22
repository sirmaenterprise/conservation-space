package com.sirma.itt.seip.instance.actions.save;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Test the upload new version rest service.
 *
 * @author nvelkov
 */
public class SaveRestServiceTest {

	@Mock
	private Actions actions;

	@InjectMocks
	private SaveRestService restService = new SaveRestService();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void uploadNewVersion() {
		restService.save(SaveRequest.buildSaveRequest(new EmfInstance(), target -> false));
		Mockito.verify(actions).callAction(Matchers.any(SaveRequest.class));
	}
}