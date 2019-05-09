package com.sirma.sep.content.batch;

import java.util.Collection;
import java.util.function.BiConsumer;

import javax.inject.Inject;
import javax.inject.Named;

import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.instance.batch.reader.BaseItemReader;

/**
 * Batch item reader that reads content identifiers and loads the corresponding {@link ContentInfo} for that id.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 20/12/2018
 */
@Named
public class ContentJobReader extends BaseItemReader<ContentInfo> {
	@Inject
	private InstanceContentService instanceContentService;

	@Override
	protected void loadBatchData(Collection<String> contentIds, BiConsumer<String, ContentInfo> onLoadedItem) {
		for (String contentId : contentIds) {
			ContentInfo content = instanceContentService.getContent(contentId, null);
			if (!content.exists()) {
				content = null;
			}
			onLoadedItem.accept(contentId, content);
		}
	}
}
