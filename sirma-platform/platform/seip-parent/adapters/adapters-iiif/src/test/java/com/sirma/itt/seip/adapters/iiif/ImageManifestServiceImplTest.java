package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.monitor.NoOpStatistics;
import com.sirma.itt.seip.monitor.Statistics;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.testutil.mocks.ConfigurationPropertyMock;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ImageContentMetadata;
import com.sirma.sep.content.InstanceContentService;
import com.srima.itt.seip.adapters.mock.ContentInfoMock;
import com.srima.itt.seip.adapters.mock.ImageServerConfigurationsMock;

import net.javacrumbs.jsonunit.JsonAssert;

/**
 * Test class for the image manifest service functionalities.
 *
 * @author Nikolay Ch
 * @author BBonev
 */
@RunWith(MockitoJUnitRunner.class)
public class ImageManifestServiceImplTest {

	@InjectMocks
	private ImageManifestServiceImpl imageManifestService = new ImageManifestServiceImpl();

	@Mock
	private InstanceContentService instanceContentService;

	@Mock
	private DatabaseIdManager idManager;

	@Mock
	private SystemConfiguration systemConfiguration;

	@Mock
	private SecurityContext securityContext;

	@Spy
	private Statistics statistics = NoOpStatistics.INSTANCE;

	@Spy
	ImageServerConfigurationsMock configMock = new ImageServerConfigurationsMock();

	@Mock
	private IiifImageContentStore iiifImageContentStore;

	@Before
	public void startProxy() {
		MockitoAnnotations.initMocks(this);
		configMock.setEnabled(true);
		configMock.setAccessAddress("http://access/uri/");
		configMock.setIiifServerAddress("http://localhost:8089/");
		configMock.setContext("http://localhost/context");

		when(systemConfiguration.getRESTAccessUrl())
				.thenReturn(new ConfigurationPropertyMock<>(URI.create("http://localhost:8080/emf/api")));
	}

	/**
	 * Tests the creation of manifest with given correct and incorrect data.
	 */
	@Test
	public void testManifestCreation() throws Exception {
		List<Serializable> testIds = new ArrayList<>();
		testIds.add("testId");
		when(instanceContentService.saveContent(any(Instance.class), any(Content.class))).then(a -> {
			ContentInfo info = mock(ContentInfo.class);
			when(info.getContentId()).thenReturn("manifestId");
			return info;
		});

		String id = imageManifestService.processManifest("manifestId", "imageWidget1", testIds);
		assertEquals(id, "manifestId");

		id = imageManifestService.processManifest("manifestId", "imageWidget1", testIds);
		assertEquals(id, "manifestId");

		when(idManager.getValidId(anyString())).then(a -> a.getArgumentAt(0, String.class));

		id = imageManifestService.processManifest(null, "imageWidgetId", testIds);
		assertEquals(id, "manifestId");
	}

	/**
	 * Tests the receiving of manifest with given correct id.
	 */
	@Test
	public void testManifestReceiving() throws Exception {

		Map<String, Serializable> metadata = new HashMap<>();
		metadata.put("height", 2200.0);
		metadata.put("width", 1400.0);
		ContentInfoMock imageData = new ContentInfoMock("testId", metadata);
		imageData.setName("image.jpg");

		ContentInfoMock imageData2 = new ContentInfoMock("testId2", metadata);
		imageData2.setName("image.jpg");

		List<Serializable> testIds = new ArrayList<>();
		testIds.add("testId");
		testIds.add("testId2");

		// The order might be different from what we requested since the content is put into structure that does not have order
		// We mock this by returning the reverse of the requested id
		when(instanceContentService.getContent(testIds, Content.PRIMARY_CONTENT)).thenReturn(Arrays.asList(imageData2,imageData));

		ContentInfoMock manifestData = new ContentInfoMock("testId", null);
		manifestData.setName("manifestId.json");

		manifestData.setInputStream(new ByteArrayInputStream("[\"testId\",\"testId2\"]".getBytes(StandardCharsets.UTF_8)));
		when(instanceContentService.getContent(any(String.class), any(String.class))).thenReturn(manifestData);

		when(idManager.getValidId(anyString())).then(a -> a.getArgumentAt(0, String.class));

		when(securityContext.getCurrentTenantId()).thenReturn("radoslav.p1");
		Manifest receivedManifest = imageManifestService.getManifest("manifestId");

		assertNotNull(receivedManifest);
		assertNotNull(receivedManifest.getAsString());
		JsonAssert.assertJsonEquals(receivedManifest.getAsString(),
				new String(
						IOUtils.toByteArray(getClass().getClassLoader().getResourceAsStream("manifests/manifest.json")),
						"utf-8"));
	}

	@Test
	public void shouldReturnManifestWithFallbackImageWhenNoObjectContent() throws IOException {
		when(instanceContentService.getContent(anyCollectionOf(Serializable.class), eq(Content.PRIMARY_CONTENT)))
				.thenReturn(new ArrayList<>());

		ContentInfoMock manifestData = new ContentInfoMock("imageId", null);
		manifestData.setInputStream(new ByteArrayInputStream("[\"imageId\"]".getBytes(StandardCharsets.UTF_8)));
		when(instanceContentService.getContent(any(String.class), any(String.class))).thenReturn(manifestData);

		ImageContentMetadata imageMetadata = createImageMetadata(200, 100);
		when(iiifImageContentStore.getMetadata(any())).thenReturn(imageMetadata);

		JsonObject manifest = imageManifestService.getManifest("manifestId").getAsJson();

		verifyNoContentImageIsPresent(manifest, 200, 100);
	}

	@Test
	public void shouldReturnManifestWithFallbackImageOnNonExistingContent() throws IOException {
		ArrayList<ContentInfo> contents = new ArrayList<>();
		ContentInfo contentInfo = mock(ContentInfo.class);
		when(contentInfo.exists()).thenReturn(false);
		contents.add(contentInfo);
		when(instanceContentService.getContent(anyCollectionOf(Serializable.class), eq(Content.PRIMARY_CONTENT)))
				.thenReturn(contents);

		ContentInfoMock manifestData = new ContentInfoMock("imageId", null);
		manifestData.setInputStream(new ByteArrayInputStream("[\"imageId\"]".getBytes(StandardCharsets.UTF_8)));
		when(instanceContentService.getContent(any(String.class), any(String.class))).thenReturn(manifestData);

		ImageContentMetadata imageMetadata = createImageMetadata(200, 100);
		when(iiifImageContentStore.getMetadata(any())).thenReturn(imageMetadata);

		JsonObject manifest = imageManifestService.getManifest("manifestId").getAsJson();

		verifyNoContentImageIsPresent(manifest, 200, 100);
	}

	private ImageContentMetadata createImageMetadata(int width, int height) {
		Map<String, Serializable> metadata = new HashMap<>();
		metadata.put(ImageContentMetadata.WIDTH, width);
		metadata.put(ImageContentMetadata.HEIGHT, height);
		ImageContentMetadata imageMetadata = new ImageContentMetadata(metadata);
		return imageMetadata;
	}

	private void verifyNoContentImageIsPresent(JsonObject manifest, int height, int width) {
		JsonArray canvases = manifest.getJsonArray("sequences").getJsonObject(0).getJsonArray("canvases");

		assertEquals(1, canvases.size());

		JsonObject noContentImageCanvas = canvases.getJsonObject(0);
		assertEquals(noContentImageCanvas.getInt("width"), 200);
		assertEquals(noContentImageCanvas.getInt("height"), 100);

		JsonArray images = noContentImageCanvas.getJsonArray("images");
		assertEquals(1, images.size());
		JsonObject image = images.getJsonObject(0);
		JsonObject resource = image.getJsonObject("resource");
		assertEquals(resource.getInt("width"), 200);
		assertEquals(resource.getInt("height"), 100);

		resource.getJsonObject("service").getString("@id").contains("noContentImage");
	}

}
