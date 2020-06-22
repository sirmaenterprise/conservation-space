package com.sirma.sep.content.rendition;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import com.sirma.itt.seip.plugin.Plugins;
import com.sirma.sep.content.rendition.ThumbnailConfigurations;
import com.sirma.sep.content.rendition.ThumbnailDao;
import com.sirma.sep.content.rendition.ThumbnailLoader;
import com.sirma.sep.content.rendition.ThumbnailProvider;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Test for {@link ThumbnailLoader}
 *
 * @author BBonev
 */
public class ThumbnailLoaderTest {

	@InjectMocks
	private ThumbnailLoader loader;

	@Mock
	private ThumbnailProvider provider;
	private List<ThumbnailProvider> providerInstances = new ArrayList<>();
	@Spy
	private Plugins<ThumbnailProvider> providers = new Plugins<>("", providerInstances);

	@Mock
	private ThumbnailDao thumbnailDao;
	@Mock
	private ThumbnailConfigurations thumbnailConfigurations;

	@Before
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		providerInstances.clear();
		providerInstances.add(provider);

		when(thumbnailConfigurations.getMaxThumbnailRetryCount()).thenReturn(2);
	}

	@Test
	public void load_Success() throws Exception {
		when(provider.getName()).thenReturn("provider");
		when(provider.getThumbnail("endPoint")).thenReturn("thumbnail");

		when(thumbnailDao.updateThumbnailEntity("id", null, "thumbnail", null)).thenReturn(1);

		loader.load("id", "endPoint", "provider", null);

		verify(thumbnailDao).updateThumbnailEntity("id", null, "thumbnail", null);
	}

	@Test
	public void load_Success_fail() throws Exception {
		when(provider.getName()).thenReturn("provider");

		loader.load("id", "endPoint", "provider", null);

		verify(thumbnailDao).updateThumbnailEntity("id", 1, null, null);
	}

	@Test
	public void load_Success_fail_max() throws Exception {
		when(provider.getName()).thenReturn("provider");

		loader.load("id", "endPoint", "provider", 1);

		verify(thumbnailDao).updateThumbnailEntity(eq("id"), eq(2), eq(ThumbnailService.MAX_RETRIES), any(Date.class));
	}

	@Test
	public void load_Success_fail_noProvider() throws Exception {
		providerInstances.clear();

		loader.load("id", "endPoint", "provider", null);

		verify(thumbnailDao).updateThumbnailEntity("id", 1, null, null);
	}

	@Test
	public void load_Success_fail_fetchWithError() throws Exception {
		when(provider.getName()).thenReturn("provider");
		when(provider.getThumbnail(anyString())).thenThrow(IOException.class);

		loader.load("id", "endPoint", "provider", null);

		verify(thumbnailDao).updateThumbnailEntity("id", 1, null, null);
	}

	@Test
	public void load_Success_fail_saveWithError() throws Exception {
		when(provider.getName()).thenReturn("provider");
		when(provider.getThumbnail(anyString())).thenThrow(IOException.class);

		when(thumbnailDao.updateThumbnailEntity(anyString(), anyInt(), any(), any())).thenThrow(Exception.class);

		loader.load("id", "endPoint", "provider", null);

		verify(thumbnailDao).updateThumbnailEntity("id", 1, null, null);
	}
}
