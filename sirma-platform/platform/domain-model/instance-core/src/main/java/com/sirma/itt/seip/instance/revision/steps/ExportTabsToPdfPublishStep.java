package com.sirma.itt.seip.instance.revision.steps;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT_LENGTH;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.MIMETYPE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.NAME;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;

import java.io.File;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.content.Content;
import com.sirma.itt.seip.content.ContentInfo;
import com.sirma.itt.seip.content.InstanceContentService;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.export.PDFExporter;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.secirity.SecurityTokensManager;
import com.sirma.itt.seip.security.exception.SecurityException;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirmaenterprise.sep.content.idoc.ContentNode;
import com.sirmaenterprise.sep.content.idoc.SectionNode.PublishMode;

/**
 * Export the tabs marked as publishable and replace them with a tab that will display the content of the instance. This
 * generally is the exported PDF.
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 102)
public class ExportTabsToPdfPublishStep implements PublishStep {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String PATH = "/#/idoc/";

	@Inject
	private PDFExporter exporter;
	@Inject
	private SecurityTokensManager tokensManager;
	@Inject
	private InstanceContentService instanceContentService;

	@Override
	public String getName() {
		return Steps.EXPORT_TABS_AS_PDF.getName();
	}

	@Override
	public void execute(PublishContext publishContext) {
		String token = getSecurityToken();
		Collection<String> tabIds = getExportedTabs(publishContext);
		if (CollectionUtils.isEmpty(tabIds)) {
			throw new EmfApplicationException("There are no tabs configured for export.");
		}
		Serializable sourceInstanceId = publishContext.getRequest().getInstanceToPublish().getId();

		TimeTracker tracker = TimeTracker.createAndStart();

		Instance revision = publishContext.getRevision();
		LOGGER.debug("Exporting tabs {} for revision {}", tabIds, revision.getId());

		String requestUri = buildPublishRequestUri(sourceInstanceId, tabIds, token);

		File exportedFile = callExport(requestUri);

		ContentInfo info = saveContent(revision, exportedFile);

		if (!info.exists()) {
			throw new RollbackedRuntimeException("Could not persist exported content!");
		}
		revision.add(PRIMARY_CONTENT_ID, info.getContentId());
		revision.add(CONTENT_LENGTH, info.getLength());
		revision.add(NAME, info.getName());
		revision.add(MIMETYPE, info.getMimeType());

		LOGGER.debug("Export for revision {} completed in {} ms", revision.getId(), tracker.stop());
	}

	private File callExport(String printRequestUri) {
		try {
			return exporter.export(printRequestUri);
		} catch (TimeoutException e) {
			throw new RollbackedRuntimeException("Export to pdf could not finish in time", e);
		}
	}

	private ContentInfo saveContent(Instance revision, File exportedFile) {
		Content content = Content
				.createEmpty()
					.setContent(exportedFile)
					.setIndexable(true)
					.setMimeType("application/pdf")
					.setPurpose(Content.PRIMARY_CONTENT)
					.setVersionable(true)
					.setName(exportedFile.getName());
		return instanceContentService.saveContent(revision, content);
	}

	private static String buildPublishRequestUri(Serializable instanceId, Collection<String> tabs, String token) {
		URIBuilder builder = new URIBuilder();
		tabs.forEach(tabId -> builder.addParameter("tab", tabId));

		builder.addParameter("jwt", token).addParameter("mode", "print");

		try {
			return PATH + instanceId + builder.build().toASCIIString();
		} catch (URISyntaxException e) {
			throw new RollbackedRuntimeException("Could not build publish address", e);
		}
	}

	private static Collection<String> getExportedTabs(PublishContext publishContext) {
		return publishContext
				.getView()
					.getSections()
					.stream()
					.filter(node -> node.getPublishMode() == PublishMode.EXPORT)
					.map(ContentNode::getId)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
	}

	private String getSecurityToken() {
		return tokensManager.getCurrentJwtToken().orElseThrow(
				() -> new SecurityException("Current user should have a security token"));
	}

}
