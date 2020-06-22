package com.sirma.itt.seip.rest.mapper;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sirma.itt.seip.plugin.ExtensionPoint;

/**
 * The MapperProvider provides application scoped jackson mapper with applied {@link MapperExtension} extensions for
 * customized behavior.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class MapperProvider {

	private ObjectMapper objectMapper;
	@Inject
	@ExtensionPoint(value = MapperExtension.NAME)
	private Iterable<MapperExtension> extensions;

	/**
	 * Initialize and apply extensions.
	 */
	@PostConstruct
	public void initialize() {
		objectMapper = new ObjectMapper();
		for (MapperExtension mapperExtension : extensions) {
			mapperExtension.extend(objectMapper);
		}
	}

	/**
	 * Provide an {@link ApplicationScoped} object mapper already initialized by the mapper extensions.
	 *
	 * @return the object mapper
	 */
	@Produces
	public ObjectMapper provideObjectMapper() {
		return objectMapper;
	}
}
