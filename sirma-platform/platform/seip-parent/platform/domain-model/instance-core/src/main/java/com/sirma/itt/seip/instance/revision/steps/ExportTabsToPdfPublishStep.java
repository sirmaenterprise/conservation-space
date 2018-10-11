package com.sirma.itt.seip.instance.revision.steps;

import javax.inject.Inject;

import com.sirma.itt.seip.domain.rest.EmfApplicationException;
import com.sirma.itt.seip.exception.RollbackedRuntimeException;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.sep.export.ExportURIBuilder;

/**
 * Export the tabs marked as publishable and replace them with a tab that will display the content of the instance. This
 * generally is the exported PDF.
 *
 * @author BBonev
 */
@Extension(target = PublishStep.EXTENSION_NAME, order = 102)
public class ExportTabsToPdfPublishStep implements PublishStep {

	@Inject
	private ExportToPdfPublishService publisher;
	@Inject
	private ExportURIBuilder uriBuilder;

	@Override
	public String getName() {
		return Steps.EXPORT_TABS_AS_PDF.getName();
	}

	@Override
	public void execute(PublishContext publishContext) {
		String sourceInstanceId = (String) publishContext.getRequest().getInstanceToPublish().getId();
		try {
			publisher.publishInstance(uriBuilder::getCurrentJwtToken,
					() -> ExportToPdfPublishService.getExportedTabs(publishContext), () -> sourceInstanceId,
					publishContext::getRevision);
		} catch (RollbackedRuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new EmfApplicationException(e.getMessage(), e.getCause() != null ? e.getCause() : e);
		}
	}

}
