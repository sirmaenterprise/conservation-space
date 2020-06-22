package com.sirma.sep.instance.relations;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.convert.TypeConverterUtil;

/**
 * Utility class containing common methods used in instances property serialization.
 *
 * @author A. Kunchev
 */
public class PropertiesSerializationUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private PropertiesSerializationUtil() {
		// utility
	}

	/**
	 * Converts given instance object property to stream of its values. Supports multivalue properties.
	 *
	 * @param value
	 *            the property value that will be converted
	 * @return {@link Stream} of the values of the passed property or {@link Stream#empty()} if the method could not
	 *         handle the passed value
	 */
	@SuppressWarnings("unchecked")
	public static Stream<Serializable> convertObjectProperty(Serializable value) {
		if (value instanceof String || value instanceof Uri || value instanceof URIImpl) {
			// convert single ids to short uri format
			String uri = Objects.toString(TypeConverterUtil.getConverter().tryConvert(ShortUri.class, value), null);
			return uri == null ? Stream.empty() : Stream.of(uri);
		} else if (value instanceof Collection) {
			// collections elements should be converted one by one
			return ((Collection<Serializable>) value)
					.stream()
						.flatMap(PropertiesSerializationUtil::convertObjectProperty);
		}

		if (value != null) {
			LOGGER.warn("Not supported type of object property value: {}", value);
		}

		return Stream.empty();
	}
}