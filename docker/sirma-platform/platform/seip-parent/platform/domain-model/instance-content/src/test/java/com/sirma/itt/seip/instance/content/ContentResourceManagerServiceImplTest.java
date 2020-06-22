package com.sirma.itt.seip.instance.content;

import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.content.ContentResourceManagerService;
import com.sirma.itt.seip.content.ContentResourceManagerServiceImpl;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.InstanceContentService;

/**
 * Test for the content resource manager service.
 *
 * @author Nikolay Ch
 */
public class ContentResourceManagerServiceImplTest {

	@Mock
	private InstanceContentService instanceContentService;

	@InjectMocks
	private ContentResourceManagerService contentManager = new ContentResourceManagerServiceImpl();

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Test the upload of the content.
	 */
	@Test
	public void testUploadContent(){
		String firstBase64 = Base64.getEncoder().encodeToString(new String("firstimage").getBytes());
		String secondBase64 = Base64.getEncoder().encodeToString(new String("secondimage").getBytes());
		Mockito.when(instanceContentService.saveContent(Mockito.any(Serializable.class), Mockito.any(Content.class))).thenReturn(null);
		Serializable instanceId = "instanceId";
		Map<Serializable, String > contentMapping = new HashMap<Serializable, String>();
		contentMapping.put(1, "data:image/png;data,"+firstBase64);
		contentMapping.put(2, "data:image/png;data,"+secondBase64);
		contentManager.uploadContent(instanceId, contentMapping);

		Mockito.verify(instanceContentService, Mockito.times(2)).saveContent(Mockito.any(Serializable.class), Mockito.any(Content.class));
	}

	/**
	 * Test the retrieving of the content.
	 */
	@Test
	public void testGetContent(){
		Mockito.when(instanceContentService.getContent(Mockito.any(Serializable.class), Mockito.any(String.class))).thenReturn(null);
		Serializable instanceId = "instanceId";
		contentManager.getContent(instanceId, "firstIcon");
		Mockito.verify(instanceContentService, Mockito.times(1)).getContent(Mockito.any(Serializable.class), Mockito.any(String.class));
	}
}
