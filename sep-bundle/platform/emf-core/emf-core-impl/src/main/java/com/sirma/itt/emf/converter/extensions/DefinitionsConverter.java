package com.sirma.itt.emf.converter.extensions;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterProvider;
import com.sirma.itt.emf.serialization.KryoConvertableWrapper;
import com.sirma.itt.emf.serialization.SerializationUtil;
import com.sirma.itt.emf.serialization.XStreamConvertableWrapper;
import com.sirma.itt.emf.serialization.xstream.XStreamSerializationEngine;

/**
 * Definitions converter. For definitions converter is used wrapper object because the definitions
 * are {@link Serializable} by default and the {@link TypeConverter} will not detect that they need
 * to be converted to serializable at all.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionsConverter implements TypeConverterProvider {

	/**
	 * The Class XStreamDeserializerConverter.
	 * 
	 * @author BBonev
	 */
	public static class XStreamDeserializerConverter implements
			Converter<String, XStreamConvertableWrapper> {
		/** The x stream serialization engine. */
		private final XStreamSerializationEngine X_STREAM_SERIALIZATION_ENGINE = new XStreamSerializationEngine();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public XStreamConvertableWrapper convert(String source) {
			return new XStreamConvertableWrapper(SerializationUtil.deserialize(source,
					X_STREAM_SERIALIZATION_ENGINE));
		}
	}

	/**
	 * The Class XStreamSerializerConverter.
	 *
	 * @author BBonev
	 */
	public static class XStreamSerializerConverter implements
			Converter<XStreamConvertableWrapper, String> {

		/** The x stream serialization engine. */
		private final XStreamSerializationEngine X_STREAM_SERIALIZATION_ENGINE = new XStreamSerializationEngine();

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String convert(XStreamConvertableWrapper source) {
			return (String) SerializationUtil.serialize(source.getTarget(),
					X_STREAM_SERIALIZATION_ENGINE);
		}

	}

	/**
	 * The Class KryoDeserializerConverter.
	 *
	 * @author BBonev
	 */
	public static class KryoDeserializerConverter implements
			Converter<Serializable, KryoConvertableWrapper> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public KryoConvertableWrapper convert(Serializable source) {
			return new KryoConvertableWrapper(SerializationUtil.deserialize(source,
					SerializationUtil.getCachedSerializationEngine()));
		}
	}

	/**
	 * The Class KryoSerializerConverter.
	 *
	 * @author BBonev
	 */
	public static class KryoSerializerConverter implements
			Converter<KryoConvertableWrapper, Serializable> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Serializable convert(KryoConvertableWrapper source) {
			return SerializationUtil.serialize(source.getTarget(),
					SerializationUtil.getCachedSerializationEngine());
		}
	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(KryoConvertableWrapper.class, Serializable.class,
				new KryoSerializerConverter());
		converter.addConverter(Serializable.class, KryoConvertableWrapper.class,
				new KryoDeserializerConverter());
		converter.addConverter(XStreamConvertableWrapper.class, String.class,
				new XStreamSerializerConverter());
		converter.addConverter(String.class, XStreamConvertableWrapper.class,
				new XStreamDeserializerConverter());
	}

}
