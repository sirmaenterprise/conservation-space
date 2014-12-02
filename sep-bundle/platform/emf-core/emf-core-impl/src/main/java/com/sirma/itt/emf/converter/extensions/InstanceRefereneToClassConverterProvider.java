package com.sirma.itt.emf.converter.extensions;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.InstanceReference;

/**
 * Type converter provider for converting instance reference to concrete instance class
 *
 * @author BBonev
 */
public class InstanceRefereneToClassConverterProvider implements TypeConverterProvider {

	/**
	 * {@link InstanceReference} to class converter.
	 * 
	 * @param <T>
	 *            the concrete reference implementation type
	 * @author BBonev
	 */
	@SuppressWarnings("rawtypes")
	public static class ReferenceToClassConverter<T extends InstanceReference> implements
			Converter<T, Class> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> convert(T source) {
			if ((source.getReferenceType() != null)
					&& (source.getReferenceType().getJavaClass() != null)) {
				return source.getReferenceType().getJavaClass();
			}
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(InstanceReference.class, Class.class,
				new ReferenceToClassConverter<InstanceReference>());
		converter.addConverter(LinkSourceId.class, Class.class,
				new ReferenceToClassConverter<LinkSourceId>());
	}

}
