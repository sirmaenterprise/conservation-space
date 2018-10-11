package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.exception.EmfException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.content.Content;
import com.sirma.sep.content.ContentInfo;
import com.sirma.sep.content.InstanceContentService;
import com.sirma.sep.content.idoc.ContentNode;
import com.sirma.sep.content.idoc.SectionNode.PublishMode;
import com.sirma.sep.export.ExportService;
import com.sirma.sep.export.ExportURIBuilder;
import com.sirma.sep.export.pdf.PDFExportRequest;
import com.sirma.sep.export.pdf.PDFExportRequest.PDFExportRequestBuilder;

/**
 * Service to handle publish step invocations reagarding exporting to pdf. The client method
 * {@link #publishInstance(Supplier, Supplier, Supplier, Supplier)} executes the publish action using the provided by
 * the suppliers arguments.
 *
 * @author bbanchev
 */
@ApplicationScoped
class ExportToPdfPublishService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private ExportService exportService;
	@Inject
	private InstanceContentService instanceContentService;
	@Inject
	private ExportURIBuilder uriBuilder;
	@Inject
	private TransactionSupport transactionSupport;

	void publishInstance(Supplier<String> tokenSupplier, Supplier<Collection<String>> tabIdsSupplier,
			Supplier<String> sourceInstanceIdSupplier, Supplier<Instance> revisionSupplier) throws EmfException {
		String sourceInstanceId = sourceInstanceIdSupplier.get();
		Collection<String> tabIds = tabIdsSupplier.get();
		if (CollectionUtils.isEmpty(tabIds)) {
			throw new EmfException("There are no tabs configured for export.");
		}
		TimeTracker tracker = TimeTracker.createAndStart();
		Instance revision = revisionSupplier.get();
		if (revision == null) {
			throw new EmfException("Target revision for instance " + sourceInstanceId + " not found");
		}

		String name = revision.getString(NAME);
		PDFExportRequest request = new PDFExportRequestBuilder()
				.setFileName(name)
					.setInstanceId(sourceInstanceId)
					.setInstanceURI(uriBuilder.generateURIForTabs(tabIds, sourceInstanceId, tokenSupplier.get()))
					.buildRequest();

		LOGGER.debug("Exporting tabs {} for revision {}", tabIds, revision.getId());

		File exportedFile = callExport(request);
		try {
			name = name == null ? exportedFile.getName() : name;
			ContentInfo info = saveContent(revision, name, exportedFile);
			if (!info.exists()) {
				throw new RollbackedRuntimeException("Could not persist exported content!");
			}
			revision.add(PRIMARY_CONTENT_ID, info.getContentId());
			revision.add(CONTENT_LENGTH, info.getLength());
			revision.add(NAME, info.getName());
			revision.add(MIMETYPE, info.getMimeType());
		} finally {
			// delete the temporal storage
			FileUtils.deleteQuietly(exportedFile);
		}
		// return without saving which should be executed in the main service
		LOGGER.debug("Export for revision {} completed in {} ms", revision.getId(), tracker.stop());
	}

	private File callExport(PDFExportRequest request) {
		return exportService.export(request);
	}

	private ContentInfo saveContent(Instance revision, String name, File exportedFile) {
		Content content = Content
				.createEmpty()
					.setContent(exportedFile)
					.setIndexable(true)
					.setMimeType("application/pdf")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setVersionable(true)
					.setName(name);
		return transactionSupport.invokeInTx(() -> instanceContentService.saveContent(revision, content));
	}

	static Collection<String> getExportedTabs(PublishContext publishContext) {
		return publishContext
				.getView()
					.getSections()
					.stream()
					.filter(node -> node.getPublishMode() == PublishMode.EXPORT)
					.map(ContentNode::getId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
	}

}
