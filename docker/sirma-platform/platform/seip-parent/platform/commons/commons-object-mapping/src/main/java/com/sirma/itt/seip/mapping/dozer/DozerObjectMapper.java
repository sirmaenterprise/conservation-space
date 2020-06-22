package com.sirma.itt.seip.mapping.dozer;

import java.util.function.Supplier;

import org.dozer.Mapper;
import org.dozer.MappingException;

import com.sirma.itt.seip.Resettable;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.mapping.ObjectMappingException;

/**
 * Simple proxy of dozer mapper to allow lazy initialization and resetting of the injected values
 *
 * @author BBonev
 */
public class DozerObjectMapper implements ObjectMapper, Resettable {

	private final Supplier<Mapper> supplier;

	/**
	 * Instantiates a new dozer object mapper from dozer {@link Mapper} supplier
	 *
	 * @param mapper
	 *            the mapper
	 */
	public DozerObjectMapper(Supplier<Mapper> mapper) {
		supplier = mapper;
	}

	/**
	 * Instantiates a new dozer object mapper with the given mapper instance
	 *
	 * @param mapper
	 *            the mapper
	 */
	public DozerObjectMapper(Mapper mapper) {
		supplier = () -> mapper;
	}

	@Override
	public <T> T map(Object source, Class<T> destinationClass) {
		try {
			return supplier.get().map(source, destinationClass);
		} catch (MappingException e) {
			throw new ObjectMappingException(e);
		}
	}

	@Override
	public void map(Object source, Object destination) {
		try {
			supplier.get().map(source, destination);
		} catch (MappingException e) {
			throw new ObjectMappingException(e);
		}
	}

	@Override
	public <T> T map(Object source, Class<T> destinationClass, String mapId) {
		try {
			return supplier.get().map(source, destinationClass, mapId);
		} catch (MappingException e) {
			throw new ObjectMappingException(e);
		}
	}

	@Override
	public void map(Object source, Object destination, String mapId) {
		try {
			supplier.get().map(source, destination, mapId);
		} catch (MappingException e) {
			throw new ObjectMappingException(e);
		}
	}

	@Override
	public void reset() {
		Resettable.reset(supplier);
	}

}