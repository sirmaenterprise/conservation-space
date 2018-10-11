package com.sirma.itt.seip.serialization.convert;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.json.JsonUtil;
import com.sirma.itt.seip.serialization.SerializationEngine;
import com.sirma.itt.seip.serialization.SerializationHelper;
import com.sirma.itt.seip.serialization.kryo.KryoConvertableWrapper;
import com.sirma.itt.seip.serialization.xstream.XStreamConvertableWrapper;
import com.sirma.itt.seip.serialization.xstream.XStreamSerializationEngine;

/**
 * Registers converters for different output formats defined by {@link OutputFormat}. For definitions converter is used
 * wrapper object because the definitions are {@link Serializable} by default and the {@link TypeConverter} will not
 * detect that they need to be converted to serializable at all.
 *
 * @author BBonev
 */
@ApplicationScoped
public class SerializationConverterProvider implements TypeConverterProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(SerializationConverterProvider.class);
	private static final OutputFormat DEFAULT_OUTPUT = OutputFormat.XML;
	private Map<OutputFormat, SerializationEngine> engines = new HashMap<>();

	@Inject
	private SerializationHelper serializationHelper;

	/**
	 * Converter deserializer from xStream xml or json format
	 *
	 * @author BBonev
	 */
	public class XStreamDeserializerConverter implements Converter<String, XStreamConvertableWrapper> {

		@Override
		public XStreamConvertableWrapper convert(String source) {
			OutputFormat format = OutputFormat.XML;
			if (JsonUtil.isJsonObject(source) || JsonUtil.isArray(source)) {
				format = OutputFormat.JSON;
			}
			return new XStreamConvertableWrapper(
					serializationHelper.deserialize(source, getSerializationEngine(format)));
		}
	}

	/**
	 * Converter serializer to specific xStream format defined by {@link OutputFormat}
	 *
	 * @author BBonev
	 */
	public class XStreamSerializerConverter implements Converter<XStreamConvertableWrapper, String> {

		@Override
		public String convert(XStreamConvertableWrapper source) {
			return (String) serializationHelper.serialize(source.getTarget(),
					getSerializationEngine(source.getOutputFormat()));
		}
	}

	/**
	 * Gets the serialization engine.
	 *
	 * @param outputFormat
	 *            the output format
	 * @return the serialization engine
	 */
	protected SerializationEngine getSerializationEngine(OutputFormat outputFormat) {
		OutputFormat local = outputFormat;
		if (local == null) {
			local = DEFAULT_OUTPUT;
		}
		SerializationEngine engine = engines.get(local);
		if (engine != null) {
			return engine;
		}

		try {
			switch (local) {
				case XML:
					engine = XStreamSerializationEngine.createXmlEngine();
					break;
				case JSON:
					engine = XStreamSerializationEngine.createJettisonEngine();
					break;
				default:
					throw new EmfRuntimeException("Not supported output format " + local);
			}
		} catch (NoClassDefFoundError e) {
			LOGGER.error("Could not load serialization engine for type {}", local, e);
			throw new EmfRuntimeException("Could not load serialization engine for type " + local, e);
		}

		engines.put(local, engine);
		return engine;
	}

	/**
	 * Converter deserializer from Kryo format
	 *
	 * @author BBonev
	 */
	public class KryoDeserializerConverter implements Converter<Serializable, KryoConvertableWrapper> {

		@Override
		public KryoConvertableWrapper convert(Serializable source) {
			return new KryoConvertableWrapper(serializationHelper.deserialize(source));
		}
	}

	/**
	 * Converter serializer to Kryo format
	 *
	 * @author BBonev
	 */
	public class KryoSerializerConverter implements Converter<KryoConvertableWrapper, Serializable> {

		@Override
		public Serializable convert(KryoConvertableWrapper source) {
			return serializationHelper.serialize(source.getTarget());
		}
	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(KryoConvertableWrapper.class, Serializable.class, new KryoSerializerConverter());
		converter.addConverter(Serializable.class, KryoConvertableWrapper.class, new KryoDeserializerConverter());
		converter.addConverter(XStreamConvertableWrapper.class, String.class, new XStreamSerializerConverter());
		converter.addConverter(String.class, XStreamConvertableWrapper.class, new XStreamDeserializerConverter());
		converter.addConverter(String.class, JSONObject.class, source -> JsonUtil.createObjectFromString(source));
		converter.addConverter(String.class, JSONArray.class, source -> JsonUtil.createArrayFromString(source));

		converter.addDynamicTwoStageConverter(XStreamConvertableWrapper.class, String.class, JSONObject.class);
		converter.addDynamicTwoStageConverter(XStreamConvertableWrapper.class, String.class, JSONArray.class);
	}

}
