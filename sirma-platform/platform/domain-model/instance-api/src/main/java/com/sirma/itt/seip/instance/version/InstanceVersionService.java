package com.sirma.itt.seip.instance.version;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.VERSION;
import static org.apache.commons.lang.StringUtils.isBlank;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.version.revert.RevertContext;
import com.sirma.itt.seip.instance.version.revert.RevertStep;

/**
 * Defines methods for instances versioning.
 *
 * @author A. Kunchev
 */
public interface InstanceVersionService {

	/** Suffix for the ids of the version instances. Used to mark that specific id is a version instance id. */
	String VERSION_SUFFIX_PART = "-v";

	/** Used to check if specific id is a version id or not. */
	Pattern ENDS_WITH_VERSION_NUMBER = Pattern.compile("-v\\d+.\\d+$");

	/** Used to retrieve original instance id from version id. */
	Pattern GET_ID_WITHOUT_SUFFIX = Pattern.compile("(.*)(?=-v\\d+.\\d+)");

	/** Used for server operation id for revert version action. */
	String REVERT_VERSION_SERVER_OPERATION = "revertVersion";

	/**
	 * Creates version for the passed instance. The created version represents snapshot of the current instance data. To
	 * addition for the version are executed search queries, which results are stored and will be used in version
	 * restoring.
	 *
	 * @param context
	 *            {@link VersionContext} object, containing all of the required information for creating a version of
	 *            the target instance
	 */
	void createVersion(VersionContext context);

	/**
	 * Loads version instance by its id. The version is extracted and all of its properties are loaded. Permission check
	 * is executed, before the actual instance extraction.
	 *
	 * @param id
	 *            the id of the version that should be loaded
	 * @return specific version of instance or null if there is no such version
	 */
	Instance loadVersion(Serializable id);

	/**
	 * Loads version instances by their ids. The instances are returned fully loaded.
	 *
	 * @param ids
	 *            the ids of the instances that should be loaded
	 * @return collection of found version instances
	 */
	Collection<Instance> loadVersionsById(Collection<Serializable> ids);

	/**
	 * Checks if the id is for version instance. The method checks the suffix of the passed id. <br>
	 * Right version id format(example) - emf:755c9e2d-f91d-4d91-b128-f4ddd3c3f893-v1.6
	 *
	 * @param id
	 *            the id that should be checked
	 * @return <code>true</code> if the passed id have correct format for version id, <code>false</code> otherwise
	 * @throws IllegalArgumentException
	 *             when the passed id is blank
	 */
	static boolean isVersion(Serializable id) {
		String stringId = Objects.toString(id, null);
		if (isBlank(stringId)) {
			throw new IllegalArgumentException("The passed argument is blank!");
		}

		return ENDS_WITH_VERSION_NUMBER.matcher(stringId).find();
	}

	/**
	 * Builds version instance id from passed instance. The id is build from the passed instance id plus version suffix
	 * and version. Example of version instance id - emf:755c9e2d-f91d-4d91-b128-f4ddd3c3f893-v1.7
	 *
	 * @param <I>
	 *            instance type
	 * @param instance
	 *            the instance from which will be build version id
	 * @return version instance id
	 * @throws IllegalArgumentException
	 *             when the instance is <code>null</code> or its id is missing or the instance has no version property
	 */
	static <I extends Instance> Serializable buildVersionId(I instance) {
		if (instance == null || instance.getId() == null || instance.isValueNull(VERSION)) {
			throw new IllegalArgumentException();
		}

		Serializable id = instance.getId();
		if (isVersion(id)) {
			return id;
		}

		return id + VERSION_SUFFIX_PART + instance.getString(VERSION);
	}

	/**
	 * Retrieves original instance id from passed version id. The version ids are based on original instance id plus
	 * version suffix. This method should return only the id without the suffix. If the id is not version id the passed
	 * parameter is returned.
	 *
	 * @param versionId
	 *            the version id from which will be extracted the original one
	 * @return instance id without version suffix
	 */
	static Serializable getIdFromVersionId(Serializable versionId) {
		if (!isVersion(versionId)) {
			return versionId;
		}

		Matcher matcher = GET_ID_WITHOUT_SUFFIX.matcher(versionId.toString());
		if (matcher.find()) {
			return matcher.group();
		}

		return versionId;
	}

	/**
	 * Sets initial value for the property {@link DefaultProperties#VERSION} for the passed instance. The initial value
	 * will be set only if the instance property is blank. The initial value for the version is retrieved from
	 * configuration.
	 *
	 * @param <I>
	 *            instance type
	 * @param instance
	 *            the instance which version property should be set, if missing
	 */
	<I extends Instance> void setInitialVersion(I instance);

	/**
	 * Retrieves all versions for the given instance id. This method only returns the versions of the instance, it will
	 * not return the current instance(current version). The versions will be returned as instances with loaded
	 * properties. Supports results paging.
	 *
	 * @param targetId
	 *            the id of the instance which versions will be retrieved
	 * @param offset
	 *            the number of the results that should be skipped in the returned collection. Used for results paging
	 * @param limit
	 *            the number of the results that should be returned. Used for results paging. Pass <code>-1</code> to
	 *            get all found results
	 * @return {@link VersionsResponse} object or empty response if there are no versions
	 */
	VersionsResponse getInstanceVersions(String targetId, int offset, int limit);

	/**
	 * Gets the value of the configuration for the instance initial version.
	 *
	 * @return string value representation for instance initial version
	 */
	String getInitialInstanceVersion();

	/**
	 * Deletes version instance by its id. This is used in cases, where recovery from errors is needed. Versions should
	 * not be deleted in any other case.
	 *
	 * @param versionId
	 *            the id of the versions instance
	 */
	void deleteVersion(Serializable versionId);

	/**
	 * Revert specific version of instance. The process represents replacing the current instance data with that from
	 * the version instance that is reverted. The produced instance represents the current instance with data and
	 * content from the version.<br>
	 * The method executes save as a part of the revert process.
	 * </p>
	 * Note that the current instance will be locked while the revert is executed. The lock will be released, when the
	 * method finishes its execution, not matter if it was successful or not.
	 *
	 * @param context
	 *            stores data required for successful revert
	 * @return reverted instance
	 * @see RevertStep
	 * @see DomainInstanceService#save(com.sirma.itt.seip.instance.InstanceSaveContext)
	 */
	Instance revertVersion(RevertContext context);

	/**
	 * Check if operation revert is allowed for specific version.
	 *
	 * @param target
	 *            the version which should be checked
	 * @return <code>true</code> if the operation is allowed, <code>false</code> otherwise
	 */
	boolean isRevertOperationAllowed(Instance target);

}
