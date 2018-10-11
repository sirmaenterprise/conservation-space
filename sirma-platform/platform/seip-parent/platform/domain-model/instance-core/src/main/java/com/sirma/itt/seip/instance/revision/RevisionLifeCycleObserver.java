package com.sirma.itt.seip.instance.revision;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.sirma.itt.seip.domain.instance.CMInstance;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.PropertiesUtil;
import com.sirma.sep.content.rendition.ThumbnailService;

/**
 * Observer to revision lifecycle events. Adds additional logic not needed for the revision service.
 *
 * @author BBonev
 */
@ApplicationScoped
public class RevisionLifeCycleObserver {

	@Inject
	private ThumbnailService thumbnailService;

	/**
	 * Life cycle method on created new revision
	 *
	 * @param event
	 *            the event
	 */
	public void onNewRevision(@Observes CreatedRevisionEvent event) {
		Instance original = event.getInstance();
		Instance revision = event.getRevision();
		ensureUniqueIdentifierSynchronization(original, revision);
		thumbnailService.copyThumbnailFromSource(revision.toReference(), original.toReference());
	}

	/**
	 * Ensure the unique identifier/s synchronizations.
	 *
	 * @param copyFrom the copy from
	 * @param copyTo the copy to
	 */
	private static void ensureUniqueIdentifierSynchronization(Instance copyFrom, Instance copyTo) {
		PropertiesUtil.copyValue(copyFrom, copyTo, DefaultProperties.UNIQUE_IDENTIFIER);
		if (copyFrom instanceof CMInstance) {
			((CMInstance) copyTo).setContentManagementId(((CMInstance) copyFrom).getContentManagementId());
		}
	}
}
