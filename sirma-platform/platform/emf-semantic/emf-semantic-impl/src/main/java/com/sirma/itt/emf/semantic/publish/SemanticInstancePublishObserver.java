package com.sirma.itt.emf.semantic.publish;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.openrdf.model.URI;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.relation.LinkConstants;
import com.sirma.itt.seip.instance.relation.LinkService;
import com.sirma.itt.seip.instance.revision.PublishApprovedRevisionEvent;
import com.sirma.itt.seip.instance.revision.PublishRejectedRevisionEvent;
import com.sirma.itt.semantic.NamespaceRegistryService;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Semantic observer to update new published revisions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticInstancePublishObserver {

	@Inject
	private NamespaceRegistryService registryService;
	@Inject
	private LinkService linkService;

	/**
	 * Listens for after instance event to update semantic data with that new revision has been published to update the
	 * required relations.
	 *
	 * @param event
	 *            the event
	 */
	public void onPublishApproved(@Observes PublishApprovedRevisionEvent event) {
		setRevisionType(event.getInstance(), EMF.TYPE_CURRENT);

		Instance newRevision = event.getNewRevision();
		setRevisionType(newRevision, EMF.TYPE_REVISION);

		Instance latestRevisionInstance = event.getLatestRevisionInstance();
		if (latestRevisionInstance != null) {
			setRevisionType(latestRevisionInstance, EMF.TYPE_LATEST_REVISION);

			latestRevisionInstance.add(LinkConstants.ACTUAL_OF,
					registryService.buildUri(newRevision.getId().toString()));

			latestRevisionInstance.add(DefaultProperties.SEMANTIC_TYPE, getRdfType(event.getInstance()));
			linkService.unlinkSimple(latestRevisionInstance.toReference(), LinkConstants.ACTUAL_OF);
		}
	}

	private static Serializable getRdfType(Instance instance) {
		return instance.get(DefaultProperties.SEMANTIC_TYPE, EMF.DOMAIN_OBJECT);
	}

	/**
	 * Listens for after instance event to update semantic data with that new revision has been published to update the
	 * required relations.
	 *
	 * @param event
	 *            the event
	 */
	@SuppressWarnings("static-method")
	public void onPublishRejected(@Observes PublishRejectedRevisionEvent event) {
		setRevisionType(event.getInstance(), EMF.TYPE_CURRENT);

		Instance newRevision = event.getRevision();
		setRevisionType(newRevision, EMF.TYPE_REVISION);
	}

	/**
	 * Set the revision type to the given instance
	 *
	 * @param instance
	 *            the instance
	 * @param revisionType
	 *            the revision type
	 */
	private static void setRevisionType(Instance instance, URI revisionType) {
		instance.add(DefaultProperties.REVISION_TYPE, revisionType);
	}
}
