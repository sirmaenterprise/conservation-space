package com.sirma.itt.seip.instance.convert;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Type converter provider for converting instance reference to concrete instance class
 *
 * @author BBonev
 */
public class InstanceRefereneToClassConverterProvider implements TypeConverterProvider {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(InstanceReference.class, Class.class,
				InstanceRefereneToClassConverterProvider::convertInstanceReference);
		converter.addConverter(LinkSourceId.class, Class.class,
				InstanceRefereneToClassConverterProvider::convertInstanceReference);
	}

	/**
	 * Convert instance reference.
	 *
	 * @param source
	 *            the source
	 * @return the class
	 */
	static Class<?> convertInstanceReference(InstanceReference source) {
		if (source.getReferenceType() != null && source.getReferenceType().getJavaClass() != null) {
			return source.getReferenceType().getJavaClass();
		}
		return null;
	}

}
