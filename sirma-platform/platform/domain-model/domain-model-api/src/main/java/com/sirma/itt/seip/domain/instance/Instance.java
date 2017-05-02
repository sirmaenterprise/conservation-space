package com.sirma.itt.seip.domain.instance;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.LOCKED_BY;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.PRIMARY_CONTENT_ID;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.READ_ALLOWED;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.TITLE;
import static com.sirma.itt.seip.domain.instance.DefaultProperties.WRITE_ALLOWED;

import java.io.Serializable;
import java.util.Map;

import org.json.JSONObject;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.StringPair;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Interface that combines the common interfaces implemented by the different instance objects.
 *
 * @author BBonev
 */
public interface Instance extends Serializable, PropertyModel, Entity<Serializable>, OwnedModel {

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
	 * {@link InstanceReference#getIdentifier()} will be <code>null</code> also.
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
