package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.emf.adapter.DMSException;
import com.sirma.itt.seip.adapters.remote.RESTClient;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.io.FileDescriptor;

/**
 * Tests the image content access provider.
 *
 * @author Nikoly Ch
 * @author A. Kunchev
 */
public class ImageContentAccessProviderTest {

	private static final String IMAGE_JPEG = "image/jpeg";

	@InjectMocks
	private ImageContentAccessProvider provider;

	@Mock
	private ImageAdapterService imageAdapterService;

	@Mock
	private Instance<RESTClient> restClient;

	@Before
	public void setup() {
		provider = new ImageContentAccessProvider();
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testProvidedURI() throws DMSException {
		EmfInstance instance = new EmfInstance();
		instance.setId("emftest");
		assertNull(provider.getContentURI(instance));
		Map<String, Serializable> properties = new HashMap<>();
		properties.put(DefaultProperties.MIMETYPE, IMAGE_JPEG);
		instance.setProperties(properties);
		assertEquals("/share/content/emftest", provider.getContentURI(instance));
	}

	@Test
	public void getDescriptor_instanceNoMymeType_null() throws DMSException {
		FileDescriptor result = provider.getDescriptor(new EmfInstance());
		assertNull(result);
	}

	@Test
	public void getDescriptor_nullContentUrl_null() throws DMSException {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.MIMETYPE, IMAGE_JPEG);
		when(imageAdapterService.getContentUrl(instance)).thenReturn(null);
		FileDescriptor result = provider.getDescriptor(instance);
		assertNull(result);
	}

	@Test
	public void getDescriptor_successful() throws DMSException {
		EmfInstance instance = new EmfInstance();
		instance.add(DefaultProperties.MIMETYPE, IMAGE_JPEG);
		when(imageAdapterService.getContentUrl(instance)).thenReturn("someURL");
		FileDescriptor result = provider.getDescriptor(instance);
		verify(restClient).get();
		assertNotNull(result);
	}
}
