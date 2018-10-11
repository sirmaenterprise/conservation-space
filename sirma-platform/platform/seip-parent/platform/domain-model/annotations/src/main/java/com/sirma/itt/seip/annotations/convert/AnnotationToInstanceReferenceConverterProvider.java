package com.sirma.itt.seip.annotations.convert;

import javax.enterprise.context.ApplicationScoped;

import org.json.JSONObject;

import com.sirma.itt.seip.annotations.model.Annotation;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.convert.AbstractInstanceToInstanceReferenceConverterProvider;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Provider class that registers the converters for the default implementations of. {@link Annotation} interface to
 * {@link InstanceReference}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class AnnotationToInstanceReferenceConverterProvider
		extends AbstractInstanceToInstanceReferenceConverterProvider {

	@Override
	public void register(TypeConverter converter) {
		addEntityConverter(converter, Annotation.class, LinkSourceId.class);
		addEntityConverter(converter, Annotation.class, InstanceReference.class);
		converter.addDynamicTwoStageConverter(LinkSourceId.class, JSONObject.class, String.class);
	}

}
