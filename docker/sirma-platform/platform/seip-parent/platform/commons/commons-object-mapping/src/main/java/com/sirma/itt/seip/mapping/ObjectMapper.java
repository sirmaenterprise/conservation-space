/**
 *
 */
package com.sirma.itt.seip.mapping;

/**
 * Entry point for performing object mappings and transformations.
 *
 * @author BBonev
 */
public interface ObjectMapper {

	/**
	 * Constructs new instance of destinationClass and performs mapping between from source.
	 *
	 * @param <T>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param destinationClass
	 *            the destination class
	 * @return the instantiated and populated object
	 * @throws ObjectMappingException
	 *             the object mapping exception
	 */
	<T> T map(Object source, Class<T> destinationClass) throws ObjectMappingException;

	/**
	 * Performs mapping between source and destination objects.
	 *
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @throws ObjectMappingException
	 *             the object mapping exception
	 */
	void map(Object source, Object destination) throws ObjectMappingException;

	/**
	 * Constructs new instance of destinationClass and performs mapping between from source.
	 *
	 * @param <T>
	 *            the generic type
	 * @param source
	 *            the source
	 * @param destinationClass
	 *            the destination class
	 * @param mapId
	 *            the custom mapping id
	 * @return the instantiated and populated object
	 * @throws ObjectMappingException
	 *             the object mapping exception
	 */
	<T> T map(Object source, Class<T> destinationClass, String mapId) throws ObjectMappingException;

	/**
	 * Performs mapping between source and destination objects.
	 *
	 * @param source
	 *            the source
	 * @param destination
	 *            the destination
	 * @param mapId
	 *            the custom mapping id
	 * @throws ObjectMappingException
	 *             the object mapping exception
	 */
	void map(Object source, Object destination, String mapId) throws ObjectMappingException;
}
