package com.sirma.itt.seip.adapters.iiif;

import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.CANVASES;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.CONTEXT;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.FORMAT;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.HEIGHT;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.ID;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.IMAGES;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.LABEL;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.MOTIVATION;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.PROFILE;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.RESOURCE;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.SEQUENCE;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.SERVICE;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.TYPE;
import static com.sirma.itt.seip.adapters.iiif.IIIFPresentationAPI.WIDTH;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.SystemConfiguration;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition;
import com.sirma.itt.seip.monitor.annotations.Monitored;
import com.sirma.itt.seip.monitor.annotations.MetricDefinition.Type;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirma.itt.seip.util.file.FileUtil;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.ContentMetadata;
import com.sirma.sep.content.ImageContentMetadata;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.StoreItemInfo;

/**
 * Service that implements the functionality for processing image manifests.
 *
 * @author Nikolay Ch
 * @author BBonev
 * @author radoslav
 */
@Singleton
public class ImageManifestServiceImpl implements ImageManifestService {

	private static final String MANIFEST_PURPOSE_OLD = "manifestPurpose";
	private static final String MANIFEST_SERVICE_PATH = "/image/manifest/";

	private static final String MANIFEST_LABEL_NAME = "";
	public static final String MANIFEST_SEQUENCE_LABEL = "Current order";

	private static final Logger LOGGER = LoggerFactory.getLogger(ImageManifestServiceImpl.class);

	@Inject
	private InstanceContentService contentService;
	@Inject
	private ImageServerConfigurations imageServerConfig;
	@Inject
	private DatabaseIdManager idManager;
	@Inject
	private SystemConfiguration systemConfiguration;
	@Inject
	private SecurityContext securityContext;
	@Inject
	private IiifImageContentStore iiifImageContentStore;

	@Override
	public String processManifest(String manifestId, String imageWidgetID,
			List<? extends Serializable> uploadedImageIDs) {
		LOGGER.debug("List with images ids: {}", uploadedImageIDs);
		JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
		for (Serializable id : uploadedImageIDs) {
			arrayBuilder.add(id.toString());
		}
		// create content based on the json bytes so that the content length
		// will also be set
		Content manifestContent = Content
				.createEmpty()
					.setContent(arrayBuilder.build().toString(), StandardCharsets.UTF_8)
					.setName(imageWidgetID + ".json")
					.setMimeType(MediaType.APPLICATION_JSON)
					.setPurpose(MANIFEST_PURPOSE);

		EmfInstance manifestInstance = new EmfInstance();
		if (manifestId == null) {
			manifestInstance.setId(idManager.getValidId(imageWidgetID));
		} else {
			manifestInstance.setId(manifestId);
		}
		return contentService.saveContent(manifestInstance, manifestContent).getContentId();
	}

	@Override
	@Monitored(@MetricDefinition(name = "iiif_manifest_build_duration_seconds", type = Type.TIMER, descr = "IIIF manifest build duration in seconds."))
	public Manifest getManifest(String manifestId) {
		ContentInfo content = contentService.getContent(manifestId, MANIFEST_PURPOSE);
		if (content.exists()) {
			String imageWidgetID = FileUtil.getName(content.getName());
			List<String> instanceIds = JSON.readArray(content.getInputStream(), JSON::getStringArray);

			LOGGER.trace("Building manifest {} for container {} and instances: {}", manifestId, imageWidgetID,
					instanceIds);
			JsonObject manifest = buildManifest(imageWidgetID, instanceIds);

			return new Manifest(new ManifestContent(content, manifest));
		}
		// old manifest format for backward compatibility
		return new Manifest(contentService.getContent(manifestId, MANIFEST_PURPOSE_OLD));
	}

	/**
	 * Builds the iiif manifest for the selected images. For each image id tries to obtain the information provided by
	 * the image server. In case the image is still processed by the server or not uploaded yet, sets the its width and
	 * height to 0.
	 *
	 * @param imageWidgetId
	 *            the image widget id
	 * @param instanceIds
	 *            the instance ids
	 * @return the manifest as json object
	 */
	private JsonObject buildManifest(String imageWidgetId, Collection<String> instanceIds) {
		Collection<ContentInfo> uploadedImagesInfo = contentService.getContent(instanceIds, Content.PRIMARY_CONTENT);
		String manifestPath = systemConfiguration.getRESTAccessUrl().get() + MANIFEST_SERVICE_PATH
				+ idManager.getValidId(imageWidgetId);

		JsonObjectBuilder currentSequence = buildSequenceInfo(manifestPath);
		currentSequence.add(CANVASES, buildCanvases(instanceIds, uploadedImagesInfo));

		return buildManifestInfo(manifestPath).add(SEQUENCE, Json.createArrayBuilder().add(currentSequence)).build();
	}

	private static JsonObjectBuilder buildSequenceInfo(String manifestPath) {
		return Json.createObjectBuilder().add(ID, manifestPath).add(TYPE, "sc:Sequence").add(LABEL,
				MANIFEST_SEQUENCE_LABEL);
	}

	private JsonObjectBuilder buildManifestInfo(String manifestPath) {
		return Json
				.createObjectBuilder()
					.add(CONTEXT, imageServerConfig.getIiifContextAddress().get().toString())
					.add(ID, manifestPath)
					.add(TYPE, "sc:Manifest")
					.add(LABEL, MANIFEST_LABEL_NAME);
	}

	private JsonArrayBuilder buildCanvases(Collection<String> instanceIds, Collection<ContentInfo> imagesContent) {
		JsonArrayBuilder canvases = Json.createArrayBuilder();

		if (imagesContent.isEmpty()) {
			useNoContentFallbackImage(canvases, 1);
		}else{
			int counter = 0;
			// we should arrange the canvases in the order they come inside the instance id list
			for (String instanceId : instanceIds) {
				counter = addToCanvasOnlyIfItHasSameId(instanceId, imagesContent, canvases, counter);
			}
		}

		return canvases;
	}

	/**
	 * Adds a canvas for the instance to the canvases json only if the content id equals the instance id
	 *
	 * @param instanceId
	 * 		The instance id.
	 * @param imagesContent
	 * 		All images.
	 * @param canvases
	 * 		The canvases json.
	 * @param counter
	 * 		The last added canvas index.
	 * @return The index of the last added canvas.
	 */
	private int addToCanvasOnlyIfItHasSameId(String instanceId, Collection<ContentInfo> imagesContent,
			JsonArrayBuilder canvases, int counter) {
		int numberOfCanvases = counter;
		for (ContentInfo contentInfo : imagesContent) {
			if (contentInfo.exists()) {
				if (contentInfo.getInstanceId().toString().equals(instanceId)) {
					Dimension<Integer> currentImageDimension = getImageDimension(contentInfo);
					String pathToImage = constructImageAddress(getImageId(contentInfo));
					numberOfCanvases++;
					JsonObjectBuilder service = Json.createObjectBuilder().add(ID, pathToImage);
					JsonObjectBuilder resource = buildResourceInfo(numberOfCanvases, currentImageDimension, service);
					JsonObjectBuilder image = buildImageSection(numberOfCanvases, resource, contentInfo.getName());
					canvases.add(buildCanvas(contentInfo.getInstanceId().toString(), contentInfo.getName(),
											 currentImageDimension, image));
				}
			} else {
				useNoContentFallbackImage(canvases, numberOfCanvases);
			}
		}
		return numberOfCanvases;
	}

	private void useNoContentFallbackImage(JsonArrayBuilder canvases, int canvasNumber) {
		StoreItemInfo info = new StoreItemInfo();
		String noContentImageName = imageServerConfig.getNoContentImageName().get();
		if (StringUtils.isNotEmpty(noContentImageName)) {
			info.setRemoteId(noContentImageName);
			info.setProviderType(IiifImageContentStore.STORE_NAME);

			ImageContentMetadata metadata = (ImageContentMetadata) iiifImageContentStore.getMetadata(info);

			Dimension<Integer> currentImageDimension = new Dimension<>(metadata.getWidth(),
					metadata.getHeight());
			String pathToImage = constructImageAddress(noContentImageName);

			JsonObjectBuilder service = Json.createObjectBuilder().add(ID, pathToImage);
			JsonObjectBuilder resource = buildResourceInfo(canvasNumber, currentImageDimension, service);
			JsonObjectBuilder image = buildImageSection(canvasNumber, resource, noContentImageName);

			canvases.add(buildCanvas(noContentImageName, noContentImageName, currentImageDimension, image));
		}
	}

	private String constructImageAddress(String imageId) {
		String iiifServerAddress = imageServerConfig.getIiifServerAddress().requireConfigured().get().toString();
		String addressSuffix = StringUtils.trimToEmpty(imageServerConfig.getIiifServerAddressSuffix().get());
		return iiifServerAddress + imageId + addressSuffix;
	}

	private static String getImageId(ContentInfo contentInfo) {
		return contentInfo.getMetadata().getString("id", () -> FileUtil.getName(contentInfo.getRemoteId()));
	}

	private JsonObjectBuilder buildCanvas(String instanceId, String imageName, Dimension<Integer> dimensions,
			JsonObjectBuilder image) {
		return Json
				.createObjectBuilder()
					.add(ID, instanceId)
					.add(TYPE, "sc:Canvas")
					.add(LABEL, imageName)
					.add(HEIGHT, dimensions.getHeight())
					.add(WIDTH, dimensions.getWidth())
					.add(SERVICE, buildCanvasService(instanceId, dimensions))
					.add(IMAGES, Json.createArrayBuilder().add(image));
	}

	private JsonObjectBuilder buildCanvasService(String instanceId, Dimension<Integer> dimensions) {
		return Json
				.createObjectBuilder()
					.add(CONTEXT, "http://iiif.io/api/annex/services/physdim/1/context.json")
					.add(ID, buildPhysicalScaleServiceAddress(instanceId, dimensions))
					.add(PROFILE, "http://iiif.io/api/annex/services/physdim");
	}

	private String buildPhysicalScaleServiceAddress(String instanceId, Dimension<Integer> dimensions) {
		return String.format("/remote/api/image/%s/%s/physicalScale/?width=%d&height=%d",
				securityContext.getCurrentTenantId(), instanceId, dimensions.getWidth(), dimensions.getHeight());
	}

	private static JsonObjectBuilder buildResourceInfo(int index, Dimension<Integer> dimensions,
			JsonObjectBuilder service) {
		return Json
				.createObjectBuilder()
					.add(ID, "http://resource/uri" + index)
					.add(TYPE, "dctypes:Image")
					.add(FORMAT, "image/jpeg")
					.add(HEIGHT, dimensions.getHeight())
					.add(WIDTH, dimensions.getWidth())
					.add(SERVICE, service);
	}

	private static JsonObjectBuilder buildImageSection(int index, JsonObjectBuilder resource, String imageLabel) {
		return Json
				.createObjectBuilder()
					.add(ID, "http://image/uri" + index)
					.add(TYPE, "oa:Annotation")
					.add(MOTIVATION, "sc:painting")
				    .add(LABEL, imageLabel)
					.add(RESOURCE, resource);
	}

	/**
	 * Gets the image width and height from the information provided by the image server.
	 *
	 * @param info
	 *            the content info of the image
	 * @return the dimension of the image if already uploaded or null if not present
	 */
	private static Dimension<Integer> getImageDimension(ContentInfo info) {
		ImageContentMetadata imageInfo = ImageContentMetadata.wrapContentMetadata(info.getMetadata());
		return new Dimension<>(imageInfo.getWidth(), imageInfo.getHeight());
	}

	/**
	 * Content info for the generated manifest
	 *
	 * @author BBonev
	 */
	private static class ManifestContent implements ContentInfo {

		private static final long serialVersionUID = 175424509716957140L;
		private final byte[] manifest;
		private final ContentInfo info;

		/**
		 * Instantiates a new manifest content.
		 *
		 * @param info
		 *            the info
		 * @param manifest
		 *            the manifest
		 */
		public ManifestContent(ContentInfo info, JsonObject manifest) {
			this.info = info;
			this.manifest = manifest.toString().getBytes(StandardCharsets.UTF_8);
		}

		@Override
		public String getId() {
			return info.getId();
		}

		@Override
		public String getContainerId() {
			return info.getContainerId();
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(manifest);
		}

		@Override
		public void close() {
			info.close();
		}

		@Override
		public String getName() {
			return info.getName();
		}

		@Override
		public String getContentId() {
			return info.getContentId();
		}

		@Override
		public Serializable getInstanceId() {
			return info.getInstanceId();
		}

		@Override
		public String getContentPurpose() {
			return MANIFEST_PURPOSE;
		}

		@Override
		public boolean exists() {
			return manifest.length > 0;
		}

		@Override
		public String getMimeType() {
			return MediaType.APPLICATION_JSON;
		}

		@Override
		public long getLength() {
			return manifest.length;
		}

		@Override
		public boolean isView() {
			return info.isView();
		}

		@Override
		public String getCharset() {
			return StandardCharsets.UTF_8.name();
		}

		@Override
		public String getRemoteId() {
			return info.getRemoteId();
		}

		@Override
		public String getRemoteSourceName() {
			return info.getRemoteSourceName();
		}

		@Override
		public ContentMetadata getMetadata() {
			return ContentMetadata.NO_METADATA;
		}

		@Override
		public boolean isReuseable() {
			// do not reuse manifests as we override them all the time and will break everything
			return false;
		}

		@Override
		public boolean isIndexable() {
			return false;
		}

		@Override
		public String getChecksum() {
			return null;
		}
	}
}
