package com.sirma.itt.emf.semantic.publish;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.eclipse.rdf4j.model.IRI;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstancePropertyNameResolver;
import com.sirma.itt.seip.instance.revision.CreatedRevisionEvent;
import com.sirma.itt.semantic.model.vocabulary.EMF;

/**
 * Semantic observer to update new published revisions.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SemanticInstancePublishObserver {

	@Inject
	private InstancePropertyNameResolver fieldConverter;
	/**
	 * Listens for after instance event to update semantic data with that new revision has been published to update the
	 * required relations.
	 *
	 * @param event the event
	 */
	@SuppressWarnings("static-method")
	public void onCreatedRevision(@Observes CreatedRevisionEvent event) {
		setRevisionType(event.getInstance(), EMF.TYPE_CURRENT);

		Instance newRevision = event.getRevision();
		setRevisionType(newRevision, EMF.TYPE_REVISION);
	}

	/**
	 * Set the revision type to the given instance
	 *
	 * @param instance the instance
	 * @param revisionType the revision type
	 */
	private void setRevisionType(Instance instance, IRI revisionType) {
		instance.add(DefaultProperties.REVISION_TYPE, revisionType, fieldConverter);
	}
}
