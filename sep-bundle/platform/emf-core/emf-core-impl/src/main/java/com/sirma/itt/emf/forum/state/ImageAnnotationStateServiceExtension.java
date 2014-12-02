package com.sirma.itt.emf.forum.state;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.domain.ObjectTypes;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.dao.InstanceType;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.state.BaseStateServiceExtension;
import com.sirma.itt.emf.state.GenericInstanceStateServiceExtension;
import com.sirma.itt.emf.state.StateServiceExtension;
import com.sirma.itt.emf.state.operation.Operation;

/**
 * State service extension to handle {@link ImageAnnotation} instances. The current implementation
 * does not support states but delegates the implementation to the topic instance state service
 * extension.
 * 
 * @author BBonev
 */
@ApplicationScoped
@InstanceType(type = ObjectTypes.IMAGE_ANNOTATION)
@Extension(target = StateServiceExtension.TARGET_NAME, order = 5.2)
public class ImageAnnotationStateServiceExtension extends
		GenericInstanceStateServiceExtension<ImageAnnotation> {
	/** The extension. */
	@Inject
	@InstanceType(type = ObjectTypes.TOPIC)
	private BaseStateServiceExtension<TopicInstance> extension;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean changeState(ImageAnnotation instance, Operation operation) {
		// no state to change for now
		return false;
	}

	@Override
	public String getPrimaryState(ImageAnnotation instance) {
		TopicInstance topic = instance.getTopic();
		if (topic != null) {
			return extension.getPrimaryState(topic);
		}
		return super.getPrimaryState(instance);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Class<ImageAnnotation> getInstanceClass() {
		return ImageAnnotation.class;
	}

}
