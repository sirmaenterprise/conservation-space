package com.sirma.itt.seip.content.processing;

import static com.sirma.sep.content.idoc.nodes.image.ImageNode.ATTR_SRC;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import javax.inject.Inject;

import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliterator;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.security.context.ContextualWrapper;
import com.sirma.itt.seip.security.context.SecurityContextManager;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.IdResolver;
import com.sirma.sep.content.ImageContentMetadata;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.InstanceViewPreProcessor;
import com.sirma.sep.content.ViewPreProcessorContext;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.nodes.image.Dimension;
import com.sirma.sep.content.idoc.nodes.image.ImageNode;

/**
 * Extract embedded images from idoc view as new files. The files are stored as embedded content to the instance and
 * will be statically loaded via rest end point: {@code /content/static/<contentId>?tenant=<tenantId>}. The links will
 * be generated in the UI before idoc renderer and prepared for lazy loading.
 * <p>
 * The implementation will use own thread pool to process and save the available images and will update the Idoc. The
 * source attribute will be removed and new attribute {@code data-embedded-id} with the content id of the file.
 *
 * @author BBonev
 */
@Extension(target = InstanceViewPreProcessor.TARGET_NAME, order = 50)
public class EmbeddedImageExtraction implements InstanceViewPreProcessor {
	/**
	 * Specifies the maximum items to be processed per thread. If this value is low and there are a lot of images the
	 * thread pool will spawn multiple threads and the simultaneously loaded images will increase dramatically that may
	 * cause {@link OutOfMemoryError}. <br>
	 * The higher number will decrees the memory footprint by limiting the maximum processed elements.<br>
	 * Example: <br>
	 * <i>For 100 images and batch size 25 max processed images will be 4 at a time <br>
	 * For 100 images and batch size 5 max processed images will be 20 at a time</i>
	 */
	private static final int BATCH_SIZE = 25;
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	@Inject
	private InstanceContentService contentService;
	@Inject
	private SecurityContextManager securityContextManager;
	@Inject
	private TransactionSupport transactionSupport;
	@Inject
	private IdResolver idResolver;
	@Inject
	private ImageDownloader imageDownloader;

	private ForkJoinPool processingPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	@Override
	@SuppressWarnings("boxing")
	public void process(ViewPreProcessorContext context) {
		Document document = context.getParsedView();
		if (document == null) {
			return;
		}
		try {
			TimeTracker tracker = TimeTracker.createAndStart();
			Long images = extractImagesAsync(context, document);
			if (images == null || images.longValue() == 0L) {
				return;
			}
			// marks the view as modified so that the document changes to be flushed back to the content to be persisted
			context.setViewUpdated();
			LOGGER.debug("Found and processed {} embedded images for {} ms for instance {}", images, tracker.stop(),
					idResolver.resolve(context.getOwner()));
		} catch (InterruptedException e) {
			LOGGER.warn("Image extraction aborted", e);
		} catch (ExecutionException e) {
			LOGGER.warn("Could not process image extraction for idoc {}", context.getOwner(), e);
		}
	}

	@SuppressWarnings("boxing")
	private Long extractImagesAsync(ViewPreProcessorContext context, Document document)
			throws InterruptedException, ExecutionException {
		Serializable owner = context.getOwner();

		ContextualWrapper contextualWrapper = securityContextManager.wrap();
		// prepare functions for async invocation
		Function<ImageNode, String> saveImages = contextualWrapper.function(extractEmbeddedImages(owner));
		Predicate<ImageNode> validateEmbeddedId = contextualWrapper.predicate(validateEmbeddedId());

		// wrap the calls to be in new transactions for each image
		Function<ImageNode, String> saveImagesInTx = node -> transactionSupport.invokeFunctionInNewTx(saveImages, node);

		// this will download the image and set it as base64 content in the src attribute
		// and then in new transaction will save the content and update the node
		Function<ImageNode, String> downloadAndSaveImage = contextualWrapper.function(downloadImage(saveImagesInTx));

		Idoc idoc = new Idoc(document);

		// both parallel tasks work on different nodes so it should not be an issue for concurrent modifications
		Future<Long> embeddedCountFuture = processingPool
				.submit(() -> extractAndSaveImages(idoc, saveImagesInTx));

		Future<Long> downloadedCountFuture = processingPool
				.submit(() -> downloadAndSaveImages(idoc, downloadAndSaveImage, validateEmbeddedId));

		// clean nodes that where extracted before
		// but now again have an embedded source (dummy for lazy loading)

		// after completed the parallel jobs do the cleanup and return
		Long embedded = embeddedCountFuture.get();
		Long downloaded = downloadedCountFuture.get();
		long cleaned = cleanupExtractedNodes(idoc);
		// note that the total count could be more than the actual nodes
		// due to the fact that cleaned can duplicate some of the nodes in the embedded and download counts
		return embedded + downloaded + cleaned;
	}

	/**
	 * Download image and then pass save the image in the content store and return
	 */
	private Function<ImageNode, String> downloadImage(Function<ImageNode, String> saveImagesInTx) {
		return node -> {
			String source = node.getSource();
			URI imageAddress = URI.create(source);
			return imageDownloader.download(imageAddress, (mimetype, data) -> {
				node.setSource(mimetype, data);
				return saveImagesInTx.apply(node);
			}, () -> null);
		};
	}

	private Predicate<ImageNode> validateEmbeddedId() {
		return node -> !contentService.getContent(node.getEmbeddedId(), null).exists();
	}

	private static long downloadAndSaveImages(Idoc document, Function<ImageNode, String> downloadAndSaveContent,
			Predicate<ImageNode> isEmbeddedIdNotValid) {

		return FixedBatchSpliterator
				.withBatchSize(document.children(), BATCH_SIZE)
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.filter(node -> node.getEmbeddedId() != null)
					.filter(isEmbeddedIdNotValid)
					.peek(ImageNode::removeEmbeddedId)
					.map(downloadAndSaveContent)
					.filter(Objects::nonNull)
					.count();
	}

	private static long cleanupExtractedNodes(Idoc idoc) {
		return idoc
				.children()
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.filter(ImageNode::isEmbedded)
					.map(cleanLazyLoadedNodes())
					.filter(Objects::nonNull)
					.count();
	}

	/**
	 * Extract and save images that are base64 encoded in the source. This does not process nodes that have embedded ids
	 */
	private static long extractAndSaveImages(Idoc document, Function<ImageNode, String> saveContent) {
		return FixedBatchSpliterator
				.withBatchSize(document.children(), BATCH_SIZE)
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.filter(ImageNode::isEmbedded)
					.filter(node -> node.getEmbeddedId() == null)
					.map(saveContent)
					.filter(Objects::nonNull)
					.count();
	}

	private static Function<ImageNode, ImageNode> cleanLazyLoadedNodes() {
		return node -> {
			if (node.getEmbeddedId() != null) {
				node.removeProperty(ATTR_SRC);
				return node;
			}
			return null;
		};
	}

	private Function<ImageNode, String> extractEmbeddedImages(Serializable owner) {
		return node -> {
			Content content = buildContentForNode(node);
			ContentInfo contentInfo;
			try {
				// each image will be saved in a separate transaction so if one fails it should not break all other
				contentInfo = contentService.saveContent(owner, content);
				if (!contentInfo.exists()) {
					return null;
				}
			} catch (Exception e) {
				LOGGER.error("Failed to save image", e);
				return null;
			}
			// if content save is successful remove the embedded image and replace it with internal link
			replaceEmbeddedContentWithAccessLink(node, contentInfo);
			String contentId = contentInfo.getContentId();
			LOGGER.debug("Extracted image and saved under id: {}", contentId);
			return contentId;
		};
	}

	private static Content buildContentForNode(ImageNode node) {
		byte[] data = node.getEmbeddedData().get();
		String mimeType = node.getEmbeddedImageMimetype().get();
		Dimension dimensions = getDimensions(data, node);
		String embeddedId = UUID.randomUUID().toString();
		return Content
				.createEmpty()
					.setName(embeddedId + "." + getExtensionByMimeType(mimeType))
					.setContent(data)
					.setIndexable(false)
					.setVersionable(false)
					.setPurpose("embeddedImage-" + embeddedId)
					.setMimeType(mimeType)
					.setView(false)
					.setProperties(buildImageProperties(dimensions))
					.allowReuse();
	}

	private static void replaceEmbeddedContentWithAccessLink(ImageNode node, ContentInfo content) {
		node.removeProperty(ATTR_SRC);
		// store the embedded content identifier to allow identification of the content
		node.setEmbeddedId(content.getContentId());
	}

	private static Map<String, Serializable> buildImageProperties(Dimension dimension) {
		return ImageContentMetadata.build("", dimension.getWidth(), dimension.getHeight()).getProperties();
	}

	private static Dimension getDimensions(byte[] data, ImageNode node) {
		Optional<Dimension> dimensions = node.getImageDimensions();
		Dimension dimension;
		if (dimensions.isPresent()) {
			dimension = dimensions.get();
		} else {
			dimension = calculateImageDimensions(data);
			// update them missing dimensions
			// this is the case for old idocs as the new should have this set by the UI
			node.setImageDimensions(dimension.getWidth(), dimension.getHeight());
		}
		return dimension;
	}

	private static Dimension calculateImageDimensions(byte[] imageData) {
		try (InputStream input = new ByteArrayInputStream(imageData)) {
			BufferedImage image = ImageIO.read(input);
			return new Dimension(image.getWidth(), image.getHeight());
		} catch (IOException e) {
			// should not happen but just in case
			LOGGER.warn("Could not read image stream", e);
			return new Dimension(0, 0);
		}
	}

	private static String getExtensionByMimeType(String mimeType) {
		if (mimeType.contains("png")) {
			return "png";
		}
		if (mimeType.contains("gif")) {
			return "gif";
		}
		return "jpg";
	}

}
