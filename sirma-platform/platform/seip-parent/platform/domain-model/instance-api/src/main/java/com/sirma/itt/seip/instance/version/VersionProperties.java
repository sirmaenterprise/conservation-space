package com.sirma.itt.seip.instance.version;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Instance properties primary used in the instance versioning.
 *
 * @author A. Kunchev
 */
public final class VersionProperties {

	/**
	 * Key for storing the content id of the queries results. After the queries are executed, the result of them is
	 * stored as content (json file) of the instance.
	 */
	public static final String QUERIES_RESULT_CONTENT_ID = "emf:queriesResultContentId";

	/**
	 * Key for the date when version is created. Needed as marker when loading specific version. Not persisted with the
	 * instance. The instance is used as temporary store for it.
	 */
	public static final String VERSION_CREATED_ON = "$versionCreatedOn$";

	/**
	 * Used as key for the date when the version is created, when serializing version.
	 */
	public static final String VERSION_CREATION_DATE = "versionCreationDate";

	/**
	 * Used to mark version instances, when they are returned for the instance version service. This properties will not
	 * be persisted.
	 */
	public static final String IS_VERSION = "isVersion";

	/**
	 * Shows if the version instance has view content or not. <br>
	 * <b>Will be removed when the patch for version content restoring is done.</b>
	 */
	public static final String HAS_VIEW_CONTENT = "hasViewContent";

	/**
	 * Shows the the mimetype of the primary content assigned to the current version instance.<br>
	 * Used for filtering the versions, which should be compared.
	 */
	public static final String PRIMARY_CONTENT_MIMETYPE = "primaryContentMimetype";

	/**
	 * Key for storing original instance id, when the version instance is loaded.
	 */
	public static final String ORIGINAL_INSTANCE_ID = "originalInstanceId";

	/**
	 * Used to store the results from the search queries for the version instances. After the correct version ids are
	 * found for the results they are returned as instance property under this key.
	 */
	// TODO remove when queries results contents are migrated
	public static final String QUERIES_RESULTS = "queriesResults";

	/**
	 * Used to store transformed instance ids for selected instances in widgets. The ids are get and then transformed to
	 * version instances ids, so that the correct instance version is loaded, when specific version is opened.
	 */
	// TODO remove when queries results contents are migrated
	public static final String MANUALLY_SELECTED = "manuallySelected";

	/**
	 * Key for the {@link java.util.Date}, when the version is created, used in handlers context for Idoc content node
	 * handlers.
	 */
	public static final String HANDLERS_CONTEXT_VERSION_DATE_KEY = VERSION_CREATION_DATE;

	/**
	 * Key for the property that shows what definition is mapped to the given version instance. Primary used when
	 * reverting versions because the loaded versions does not contain this property and when the version properties
	 * replace the properties from the original one this property is removed from the model, which causes problems
	 * afterwards.<br>
	 * <b>NOTE!</b> Probably will be removed or resolved by other means in the newer version of the application so do
	 * not relay on it.
	 */
	public static final String DEFINITION_ID = "emf:definitionId";

	/**
	 * Key for the temporary instance property that will be used to define the version mode, when the target instance is
	 * saved.
	 */
	public static final String VERSION_MODE = "$versionMode$";

	/**
	 * Contains all of the declared version properties in this class. Uses reflection to collect them.
	 */
	static final Set<String> ALL = Stream.of(VersionProperties.class.getDeclaredFields())
			.filter(field -> !"ALL".equalsIgnoreCase(field.getName()) && !field.isSynthetic())
			.map(field -> ReflectionUtils.getFieldValue(field, (Object) null))
			.map(String.class::cast)
			.collect(toSet());

	private VersionProperties() {
	}

	/**
	 * Collect all of the declared version properties in this class.
	 *
	 * @return the collected version properties in this class.
	 */
	public static Set<String> getAll() {
		return ALL;
	}

	/**
	 * Collect all of the declared version properties in this class without <code>skipProperties</code>.
	 *
	 * @param skipProperties - collection with properties which have to be skipped.
	 * @return the collected version properties.
	 */
	public static Set<String> getVersionProperties(Collection<String> skipProperties) {
		Set<String> versionProperties = new HashSet<>(ALL);
		versionProperties.removeAll(skipProperties);
		return versionProperties;
	}
}