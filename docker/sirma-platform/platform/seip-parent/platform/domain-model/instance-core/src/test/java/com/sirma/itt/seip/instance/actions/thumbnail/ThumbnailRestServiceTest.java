package com.sirma.itt.seip.instance.actions.thumbnail;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.relations.AddRelationRequest;

/**
 * Test for {@link ThumbnailRestService}.
 *
 * @author A. Kunchev
 */
public class ThumbnailRestServiceTest {

	@InjectMocks
	private ThumbnailRestService service;

	@Mock
	private Actions actions;

	@Before
	public void setup() {
		service = new ThumbnailRestService();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void addThumbnail_shouldCallAddRelationRequest() {
		AddThumbnailRequest request = new AddThumbnailRequest();
		request.setThumbnailObjectId("emf:thumbnail-source");
		service.addThumbnail(request);
		verify(actions).callAction(any(AddRelationRequest.class));
	}

}
