package com.sirma.itt.emf.forum;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.forum.model.CommentInstance;
import com.sirma.itt.emf.forum.model.ImageAnnotation;
import com.sirma.itt.emf.forum.model.TopicInstance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.converters.AbstractInstanceToInstanceReferenceConverterProvider;

/**
 * Converter provider extension to register conversions for comment and topic instances to instance
 * references.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class CommentToInstanceReferenceConverterProvider extends
		AbstractInstanceToInstanceReferenceConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		addEntityConverter(converter, TopicInstance.class, LinkSourceId.class);
		addEntityConverter(converter, TopicInstance.class, InstanceReference.class);
		addEntityConverter(converter, CommentInstance.class, LinkSourceId.class);
		addEntityConverter(converter, CommentInstance.class, InstanceReference.class);
		addEntityConverter(converter, ImageAnnotation.class, LinkSourceId.class);
		addEntityConverter(converter, ImageAnnotation.class, InstanceReference.class);
	}

}
