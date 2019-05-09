package com.sirma.itt.seip.template;

import static com.sirma.itt.seip.collections.CollectionUtils.isNotEmpty;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.Idoc;
import com.sirma.sep.content.idoc.nodes.image.ImageNode;

/**
 * Extension point to embed extracted images into the template before it's saved. This will make the template
 * transferable without the dependency of the current application instance.
 *
 * @author BBonev
 */
@Extension(target = TemplatePreProcessor.PLUGIN_NAME, order = 20)
public class EmbeddedImagesTemplatePreProcessor implements TemplatePreProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceContentService contentService;
	@Inject
	private TaskExecutor taskExecutor;

	@Override
	public void process(TemplateContext context) {
		Template template = context.getTemplate();
		if (StringUtils.isBlank(template.getContent())) {
			return;
		}

		Idoc idoc = Idoc.parse(template.getContent());
		List<Pair<ImageNode, ContentInfo>> nodes = idoc
				.children()
					.filter(ContentNode::isImage)
					.map(ImageNode.class::cast)
					.filter(image -> StringUtils.isNotBlank(image.getEmbeddedId()))
					.map(loadContentInfo())
					.collect(Collectors.toList());

		if (isNotEmpty(nodes)) {
			TimeTracker tracker = TimeTracker.createAndStart();
			int processed = processImageNodes(nodes);

			template.setContent(idoc.asHtml());
			LOGGER.debug("Found {} embedded image nodes and loaded {} for template '{}' and took {} ms", nodes.size(),
					processed, template.getId(), tracker.stop());
		}
	}

	private int processImageNodes(List<Pair<ImageNode, ContentInfo>> nodes) {
		Collection<Future<?>> futures = new LinkedList<>();

		for (Pair<ImageNode, ContentInfo> pair : nodes) {
			ImageNode node = pair.getFirst();
			ContentInfo content = pair.getSecond();
			String embeddedId = node.getEmbeddedId();

			// do not schedule non existing content nodes
			if (!content.exists()) {
				LOGGER.warn("Could not load embedded image content {}", embeddedId);
				// no embedded image and no valid source value we should remove the node completely
				if (StringUtils.isBlank(node.getSource())) {
					node.remove();
				}
				continue;
			}

			Future<String> future = taskExecutor.submit(loadImageData(content),
					setLoadedData(node, content.getMimeType()), processError(embeddedId));

			futures.add(future);
		}

		taskExecutor.waitForAll(futures);
		failInCaseOfErrors(futures);

		return futures.size();
	}

	private static void failInCaseOfErrors(Collection<Future<?>> futures) {
		List<Throwable> exceptions = new LinkedList<>();
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (ExecutionException e) { // NOSONAR
				exceptions.add(e.getCause());
			} catch (CancellationException | InterruptedException e) { // NOSONAR
				exceptions.add(e);
			}
		}
		if (isNotEmpty(exceptions)) {
			RollbackedRuntimeException e = new RollbackedRuntimeException("Could not load all template images!");
			exceptions.forEach(e::addSuppressed);
			throw e;
		}
	}

	private Function<ImageNode, Pair<ImageNode, ContentInfo>> loadContentInfo() {
		return node -> {
			String embeddedId = node.getEmbeddedId();
			ContentInfo content = contentService.getContent(embeddedId, null);
			return new Pair<>(node, content);
		};
	}

	private static Supplier<String> loadImageData(ContentInfo content) {
		return () -> {
			try (InputStream stream = content.getInputStream()) {
				return Base64.getEncoder().encodeToString(IOUtils.toByteArray(stream));
			} catch (IOException e) {
				throw new EmfRuntimeException(e);
			}
		};
	}

	private static Consumer<String> setLoadedData(ImageNode node, String mimetype) {
		return data -> {
			node.setSource(mimetype, data);
			node.removeEmbeddedId();
		};
	}

	private static Consumer<Throwable> processError(String embeddedId) {
		return e ->
		// the passed exception will be registered in the future and will be processed later
		// we just log it here. If exception is thrown from here it will hide the current exception
		LOGGER.warn("Could not load image {}", embeddedId, e);
	}

}
