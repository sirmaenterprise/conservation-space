package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.READ_ALLOWED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.WRITE_ALLOWED;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Interface that combines the common interfaces implemented by the different instance objects.
 *
 * @author BBonev
 */
public interface Instance extends Serializable, PropertyModel, Entity<Serializable> {

	/**
	 * Sets the revision.
	 *
	 * @param revision
	 *            the new revision
	 */
	void setRevision(Long revision);

	/**
	 * Gets a instance reference that represents the given instance. The method could return <code>null</code> if not
	 * supported. Note: if the {@link Instance#getId()} is <code>null</code> then the
	 * {@link InstanceReference#getId()} will be <code>null</code> also.
	 * <p>
	 * Multiple calls to the same method on the same instance will result in the same object to be returned. The method
	 * of the {@link InstanceReference#toInstance()} should return the initial instance only if the reference is created
	 * via the current method.
	 *
	 * @return the instance reference
	 */
	InstanceReference toReference();

	/**
	 * Gets the instance type for the current instance.
	 *
	 * @return the type
	 */
	default InstanceType type() {
		return null;
	}

	/**
	 * Sets the instance type for the current instance
	 *
	 * @param type
	 *            the new type
	 */
	default void setType(InstanceType type) {
		// nothing to do
	}

	/**
	 * Checks if is the current instance has been marked as deleted or no.
	 *
	 * @return true, if is deleted
	 */
	boolean isDeleted();

	/**
	 * Marks the current instance as deleted.
	 */
	default void markAsDeleted() {
		// Default behavior is not to mark the instance for deleted. Override this if necessary.
	}

	/**
	 * Checks if the instance is locked or not
	 *
	 * @return true, if is locked
	 */
	default boolean isLocked() {
		return isPropertyPresent(LOCKED_BY);
	}

	/**
	 * Gets the locked by or <code>null</code>
	 *
	 * @return the locked by
	 */
	default String getLockedBy() {
		return getAsString(LOCKED_BY);
	}

	/**
	 * Transform the current instance to {@link JSONObject} by adding the given properties to the result if present.
	 *
	 * @param properties
	 *            the properties
	 * @return the JSON object
	 * @see #transformInstance(Instance, String...)
	 */
	default JSONObject toJson(String... properties) {
		return transformInstance(this, properties);
	}

	/**
	 * Transform the current instance to {@link JSONObject} by adding the given properties to the result and converting
	 * their name.
	 *
	 * @param properties
	 *            the properties
	 * @return the JSON object
	 * @see #transformInstance(Instance, StringPair...)
	 */
	default JSONObject toJson(StringPair... properties) {
		return transformInstance(this, properties);
	}

	/**
	 * Retrieve instance value. The name of the value is first resolved using the given property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the retrieved value if any or null
	 * @see #get(String)
	 */
	default Serializable get(String name, InstancePropertyNameResolver fieldResolver) {
		return get(fieldResolver.resolve(this, name));
	}

	/**
	 * Retrieve instance value or default value. The name of the value is first resolved using the given property name
	 * resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param defaultValue to return if the value is not found
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the found value or the default value
	 * @see #get(String, Serializable)
	 */
	default Serializable get(String name, Serializable defaultValue, InstancePropertyNameResolver fieldResolver) {
		return get(fieldResolver.resolve(this, name), defaultValue);
	}

	/**
	 * Retrieve instance values in the form of collection. The name of the value is first resolved using the given
	 * property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param defaultCollectionInit supplier for the default collection value if no value is found or no collection
	 * @param fieldResolver the property name resolver if the name is URI
	 * @param <E> collection element types
	 * @param <C> the created collection type
	 * @return a collection with all values for the given name
	 * @see #getAsCollection(String, Supplier)
	 */
	default <E extends Serializable, C extends Collection<E>> Collection<E> getAsCollection(String name,
			Supplier<C> defaultCollectionInit, InstancePropertyNameResolver fieldResolver) {
		return getAsCollection(fieldResolver.resolve(this, name), defaultCollectionInit);
	}

	/**
	 * Retrieves integer valued property from an instance. The name of the value is first resolved using the given
	 * property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the found int value or zero
	 * @see #getInt(String)
	 */
	default int getInt(String name, InstancePropertyNameResolver fieldResolver) {
		return getInt(fieldResolver.resolve(this, name));
	}

	/**
	 * Retrieves long valued property from an instance. The name of the value is first resolved using the given
	 * property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the found long value or zero
	 * @see #getLong(String)
	 */
	default long getLong(String name, InstancePropertyNameResolver fieldResolver) {
		return getLong(fieldResolver.resolve(this, name));
	}

	/**
	 * Retrieves String valued property from an instance. The name of the value is first resolved using the given
	 * property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the String value or null
	 * @see #getString(String)
	 */
	default String getString(String name, InstancePropertyNameResolver fieldResolver) {
		return getString(fieldResolver.resolve(this, name));
	}

	/**
	 * Retrieves String valued property from an instance or the given default value if not found. The name of the
	 * value is first resolved using the given property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param defaultValue the default value to return if not found
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the found value or the given default value
	 * @see #getString(String, String)
	 */
	default String getString(String name, String defaultValue, InstancePropertyNameResolver fieldResolver) {
		return getString(fieldResolver.resolve(this, name), defaultValue);
	}

	/**
	 * Retrieve instance value as String. The name of the value is first resolved using the given property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the retrieved value if any or null
	 * @see #getAsString(String)
	 */
	default String getAsString(String name, InstancePropertyNameResolver fieldResolver) {
		return getAsString(fieldResolver.resolve(this, name));
	}

	/**
	 * Retrieve instance boolean value. The name of the value is first resolved using the given property name resolver
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the retrieved value if any or {@code false}
	 * @see #getBoolean(String)
	 */
	default boolean getBoolean(String name, InstancePropertyNameResolver fieldResolver) {
		return getBoolean(fieldResolver.resolve(this, name));
	}

	/**
	 * Add or replaces a instance value. The name of the value is first resolved using the given property name resolver.
	 * @param name the property name or URI of the needed field
	 * @param value the value to set
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the previous instance value
	 * @see #add(String, Serializable)
	 */
	default Serializable add(String name, Serializable value, InstancePropertyNameResolver fieldResolver) {
		return add(fieldResolver.resolve(this, name), value);
	}

	/**
	 * Adds all of the given properties but before that their names will be converted using the given field resolver.
	 * @param newProperties the properties to be added
	 * @param fieldResolver the property name resolver if the name is URI
	 * @see #addAllProperties(Map)
	 */
	default void addAllProperties(Map<String, ? extends Serializable> newProperties, InstancePropertyNameResolver fieldResolver) {
		Function<String, String> resolver = fieldResolver.resolverFor(this);
		// the transfer bellow could be done without additional map building, but it may hide data removal as the
		// original map could have keys that after conversion are the same as some other key. for example:
		// input map: key1->value1, emf:key1->value2
		// after conversion: emf:key1->value1, emf:key1->value2
		// the map is no longer valid and without this only value2 will be added and value1 will be lost
		newProperties.entrySet()
				.stream()
				.collect(Collectors.toMap(entry -> resolver.apply(entry.getKey()), Map.Entry::getValue))
				.forEach(this::add);
	}

	/**
	 * Add the given value only if not null. The name of the property will be resolved first using the given property
	 * name resolver.
	 *
	 * @param name the property name or URI of the added field
	 * @param value the value to add
	 * @param fieldResolver the property name resolver if the name is URI
	 * @see #addIfNotNull(String, Serializable)
	 */
	default void addIfNotNull(String name, Serializable value, InstancePropertyNameResolver fieldResolver) {
		addIfNotNull(fieldResolver.resolve(this, name), value);
	}

	/**
	 * Append a value to a collection. The name of the value is first resolved using the given property name resolver.
	 *
	 * @param name the property name or URI of the modified field
	 * @param value the value to append
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return if value was added successfully
	 * @see #append(String, Serializable)
	 */
	default boolean append(String name, Serializable value, InstancePropertyNameResolver fieldResolver) {
		return append(fieldResolver.resolve(this, name), value);
	}

	/**
	 * Append all of the given values to the given field. The name of the value is first resolved using the given property name resolver.
	 *
	 * @param name the property name or URI of the needed field
	 * @param values the values to add
	 * @param fieldResolver the property name resolver if the name is URI
	 * @param <T> the values type
	 * @return true if any of the values is added and false if none of them are
	 * @see #appendAll(String, Collection)
	 */
	default <T extends Serializable> boolean appendAll(String name, Collection<? extends T> values, InstancePropertyNameResolver fieldResolver) {
		return appendAll(fieldResolver.resolve(this, name), values);
	}

	/**
	 * Removes a property from an instance. The name of the value is first resolved using the given property name resolver.
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return the removed value
	 * @see #remove(String)
	 */
	default Serializable remove(String name, InstancePropertyNameResolver fieldResolver) {
		return remove(fieldResolver.resolve(this, name));
	}

	/**
	 * Removes a value from a property of the current instance. The name of the value is first resolved using the given
	 * property name resolver.
	 *
	 * @param name the property name or URI of the needed field
	 * @param value the value to remove
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return if the value was removed successfully
	 * @see #remove(String, Serializable)
	 */
	default boolean remove(String name, Serializable value, InstancePropertyNameResolver fieldResolver) {
		return remove(fieldResolver.resolve(this, name), value);
	}

	/**
	 * Removes all of the given properties but before that converts them using the given field resolver.
	 *
	 * @param propertyKeysToRemove the properties to remove
	 * @param fieldResolver the property name resolver if the name is URI
	 * @see #removeProperties(Collection)
	 */
	default void removeProperties(Collection<String> propertyKeysToRemove, InstancePropertyNameResolver fieldResolver) {
		propertyKeysToRemove.stream()
				.map(fieldResolver.resolverFor(this))
				.forEach(this::remove);
	}

	/**
	 * Checks if the given property is present in the current instance. The name of the property is first resolved using
	 * the given property name resolver.
	 *
	 * @param name the property name or URI of the needed field
	 * @param fieldResolver the property name resolver if the name is URI
	 * @return true if there is a value for the given name
	 * @see #isPropertyPresent(String)
	 */
	default boolean isPropertyPresent(String name, InstancePropertyNameResolver fieldResolver) {
		return isPropertyPresent(fieldResolver.resolve(this, name));
	}

	/**
	 * Transforms an emf instance to json object by copying properties.
	 *
	 * @param instance
	 *            instance to transform
	 * @param properties
	 *            properties to map.
	 * @return new json object containing the mapped properties or null if the provided instance is null.
	 */
	static JSONObject transformInstance(Instance instance, String... properties) {
		JSONObject result = null;
		if (instance != null && properties != null) {
			result = new JSONObject();
			Map<String, Serializable> instanceProperties = instance.getProperties();
			for (String propertyName : properties) {
				JsonUtil.addToJson(result, propertyName, instanceProperties.get(propertyName));
			}

			if (instance.getId() != null) {
				JsonUtil.addToJson(result, "id", instance.getId().toString());
			}
		}
		return result;
	}

	/**
	 * Transforms an emf instance to json object by copying properties.
	 *
	 * @param instance
	 *            instance to transform
	 * @param properties
	 *            properties to map. The first element in the StringPair is the name of the json (destination) property,
	 *            the second element is the name of the {@link Instance} (source) property.
	 * @return new json object containing the mapped properties or null if the provided instance is null.
	 */
	public static JSONObject transformInstance(Instance instance, StringPair... properties) {
		JSONObject result = null;
		if (instance != null && properties != null) {
			result = new JSONObject();
			Map<String, Serializable> instanceProperties = instance.getProperties();
			for (StringPair property : properties) {
				JsonUtil.addToJson(result, property.getFirst(), instanceProperties.get(property.getSecond()));
			}

			if (instance.getId() != null) {
				JsonUtil.addToJson(result, "id", instance.getId().toString());
			}
		}
		return result;
	}

	/**
	 * Checks if the instance contains value for {@link DefaultProperties#PRIMARY_CONTENT_ID}.
	 *
	 * @return <code>true</code>, if the value is not null, <code>false</code> otherwise
	 */
	default boolean isUploaded() {
		return isValueNotNull(PRIMARY_CONTENT_ID);
	}

	/**
	 * Gets the label.
	 *
	 * @return the label
	 */
	default String getLabel() {
		return getString(TITLE);
	}

	/**
	 * Gets flag that shows, if the current user is allowed to read the current instance.
	 *
	 * @return <code>true</code> if the current user has read permissions, <code>false</code> otherwise
	 */
	default boolean isReadAllowed() {
		return getBoolean(READ_ALLOWED);
	}

	/**
	 * Gets flag that shows, if the current user is allowed to write(change) the current instance.
	 *
	 * @return <code>true</code> if the current user has write permissions, <code>false</code> otherwise
	 */
	default boolean isWriteAllowed() {
		return getBoolean(WRITE_ALLOWED);
	}
}
