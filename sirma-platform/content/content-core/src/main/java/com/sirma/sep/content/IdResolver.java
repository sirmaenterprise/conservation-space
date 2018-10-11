package com.sirma.sep.content;

import java.io.Serializable;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.Identity;

/**
 * Resolve identifier based on the arguments. This is used to extract id from instances and instance references without
 * explicit reference to them using the base {@link Entity} and {@link Identity} interfaces.
 *
 * @author BBonev
 */
@Singleton
public class IdResolver {

	private final TypeConverter typeConverter;

	/**
	 * Initialize a resolver using the given type converter.
	 *
	 * @param converter
	 *            the converter to use when converting from the serializable value to string if argument is neither
	 *            {@link Entity} or {@link Identity}
	 */
	@Inject
	public IdResolver(TypeConverter converter) {
		typeConverter = converter;
	}

	/**
	 * Tries to resolve instance identifier based on the argument
	 *
	 * @param source
	 *            for the resolving
	 * @return optional that may contain the resolved identifier.
	 */
	public Optional<Serializable> resolve(Serializable source) {
		Serializable id;
		if (source instanceof Entity) {
			id = ((Entity<?>) source).getId();
		} else if (source instanceof Identity) {
			id = ((Identity) source).getIdentifier();
		} else {
			id = typeConverter.tryConvert(String.class, source);
		}
		return Optional.ofNullable(id);
	}
}
