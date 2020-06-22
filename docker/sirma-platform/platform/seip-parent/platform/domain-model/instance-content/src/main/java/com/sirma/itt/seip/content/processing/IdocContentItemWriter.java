package com.sirma.itt.seip.content.processing;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.stream.Collectors;

import javax.batch.api.chunk.AbstractItemWriter;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;

/**
 * Batch item writer that stores the given contents to the {@link InstanceContentService}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@Named(IdocContentItemWriter.NAME)
public class IdocContentItemWriter extends AbstractItemWriter {

	public static final String NAME = "idocContentItemWriter";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public void writeItems(List<Object> items) throws Exception {
		if (!items.isEmpty()) {
			List<String> modifiedContentIds = items.stream()
					.filter(Content.class::isInstance)
					.map(Content.class::cast)
					.map(Content::getContentId)
					.collect(Collectors.toList());
			LOGGER.info("Saving modified contents {}", modifiedContentIds);
		}
		for (Object item : items) {
			if (item instanceof Content) {
				saveContent((Content) item);
			}
		}
	}

	private void saveContent(Content content) {
		try {
			ContentInfo updated = instanceContentService.updateContent(content.getContentId(), null, content);
			if (!updated.exists()) {
				LOGGER.warn("Failed to update content for id: {}", content.getContentId());
			}
		} catch (RuntimeException e) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Could not save updated content with id {} due to: {}", content.getContentId(), e.getMessage(), e);
			} else {
				LOGGER.warn("Could not save updated content with id {} due to: {}", content.getContentId(), e.getMessage());
			}
		}
	}
}
