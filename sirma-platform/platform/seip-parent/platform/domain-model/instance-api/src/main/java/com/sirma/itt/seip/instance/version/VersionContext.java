package com.sirma.itt.seip.instance.version;

import static java.util.Objects.requireNonNull;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.InstanceSaveContext;

/**
 * Context object used to store information for instance versioning. Contains method for setting and retrieving this
 * information.
 *
 * @see Context
 * @author A. Kunchev
 */
public class VersionContext extends Context<String, Object> {

	/** The initial size of the context map. */
	private static final int INITIAL_VERSION_CONTEXT_DATA_SIZE = 6;

	private static final long serialVersionUID = 7740721337606779343L;

	private static final String TARGET_INSTANCE = "target";

	private static final String VERSION_CREATE_DATE = "versionCreatedOn";

	private static final String VERSION_INSTANCE = "versionInstance";

	private static final String VERSION_MODE = "versionMode";

	private static final String PROCESS_WIDGETS = "processWidgets";

	private static final String OBJECT_PROPERTIES_VERSIONING_ENABLED = "objectPropertiesVersioningEnabled";

	private VersionContext() {
		super(INITIAL_VERSION_CONTEXT_DATA_SIZE);
	}

	/**
	 * Creates new version context for the passed instance. The date and widget processing flag are initialized with
	 * default values.
	 *
	 * @param targetInstance the instance for versioning
	 * @return new instance of version context with initial data
	 */
	public static VersionContext create(Instance targetInstance) {
		return create(targetInstance, new Date());
	}

	/**
	 * Creates new version context and sets data of creation of the version in addition to the instance. The instance
	 * and the create date are required and cannot be null. Sets widget processing to {@link Boolean#TRUE} by default.
	 *
	 * @param targetInstance the instance for versioning
	 * @param createdOn date when the instance version is created. It will be used as a marker in the retrieving of the
	 *        version
	 * @return new instance of version context with initial data
	 */
	public static VersionContext create(Instance targetInstance, Date createdOn) {
		return create(targetInstance, createdOn, Boolean.TRUE);
	}

	/**
	 * Creates new version context with data of creation and flag that shows if the widget should be processed when the
	 * version view content is created in addition to the target instance for which is created version. The instance and
	 * create date are required and cannot be null.
	 *
	 * @param targetInstance the instance for versioning
	 * @param createdOn date when the instance version is created. It will be used as a marker in the retrieving of the
	 *        version
	 * @param processWidgets flag that shows if the widgets should be processed when the version view content is
	 *        created. {@link Boolean#TRUE} if they should, {@link Boolean#FALSE} if they should not
	 * @return new instance of version context
	 */
	public static VersionContext create(Instance targetInstance, Date createdOn, Boolean processWidgets) {
		Objects.requireNonNull(targetInstance, "The instance that will be versioned is required!");
		Objects.requireNonNull(createdOn, "The date when the version is created is required!");
		VersionContext context = new VersionContext();
		context.put(TARGET_INSTANCE, targetInstance);
		context.put(VERSION_CREATE_DATE, createdOn);
		context.put(VERSION_MODE, VersionMode.MINOR);
		context.put(PROCESS_WIDGETS, processWidgets);
		return context;
	}

	/**
	 * Gets the target instance id.
	 *
	 * @return target instance id or <code>null</code> if the instance is missing
	 */
	public String getTargetInstanceId() {
		return (String) getTargetInstance().getId();
	}

	/**
	 * Getter for the target instance.
	 *
	 * @return target instance
	 */
	public Instance getTargetInstance() {
		return getIfSameType(TARGET_INSTANCE, Instance.class);
	}

	/**
	 * Setter for the target instance.
	 *
	 * @param target the target instance
	 * @return current context object to allow methods chaining
	 * @throws NullPointerException when the passed instance is null
	 */
	public VersionContext setTargetInstance(Instance target) {
		put(TARGET_INSTANCE, Objects.requireNonNull(target));
		return this;
	}

	/**
	 * Getter for the date when the version will be created. It will be used as marker in version retrieving later.
	 *
	 * @return the {@link Date} when the version is created
	 */
	public Date getCreationDate() {
		return getIfSameType(VERSION_CREATE_DATE, Date.class);
	}

	/**
	 * Setter for the date when the version is created. This date is used as marker for the all version for the
	 * instances. It is used when the version is retrieved and the queries results are loaded.
	 *
	 * @param createdOn the date when the version is created
	 * @return current context object to allow methods chaining
	 * @throws NullPointerException when the passed date is null
	 */
	public VersionContext setCreationDate(Date createdOn) {
		put(VERSION_CREATE_DATE, Objects.requireNonNull(createdOn));
		return this;
	}

	/**
	 * Getter for version instance id. It will be retrieved after the version instance is persisted and will be used to
	 * assign contents the the versions.
	 *
	 * @return the persisted version instance id or <code>null</code> if the version instance is not available
	 */
	public String getVersionInstanceId() {
		return (String) getVersionInstance().map(Instance::getId).orElse(null);
	}

	/**
	 * Getter for the version instance.
	 *
	 * @return {@link Optional} containing version instance or {@link Optional#empty()} if the instance is not available
	 */
	public Optional<Instance> getVersionInstance() {
		return Optional.ofNullable(getIfSameType(VERSION_INSTANCE, Instance.class));
	}

	/**
	 * Setter for the version instance.
	 *
	 * @param version the version instance
	 * @return current context object to allow methods chaining
	 * @throws NullPointerException when the passed instance is null
	 */
	public VersionContext setVersionInstance(Instance version) {
		put(VERSION_INSTANCE, Objects.requireNonNull(version));
		return this;
	}

	/**
	 * Sets the version mode for the instance save. This property provides a way to control instance version
	 * incrementing and instance version creation. By default the version will be incremented by [0.1], if this property
	 * is not set to another value.
	 * <p>
	 * Possible values: <br>
	 * <b>MINOR</b> - minor part of the instance version is incremented by 1 [<b>default value</b>]<br>
	 * <b>MAJOR</b> - major part of the instance version is incremented by 1 and minor version is reset to 0 <br>
	 * <b>NONE</b> - instance version will be unchanged and new version for the target instance will not be created
	 *
	 * @param mode the version mode with which the instance is saved. Used to control instance version incrementing
	 * @return current {@link InstanceSaveContext}
	 */
	public VersionContext setVersionMode(VersionMode mode) {
		put(VERSION_MODE, requireNonNull(mode, "Version mode should not be null!"));
		return this;
	}

	/**
	 * Gets the version mode for the instance save. It is used to control instance version property incrementing or if
	 * we should create new version with this save or not.
	 *
	 * @return {@link VersionMode} value
	 */
	public VersionMode getVersionMode() {
		return getIfSameType(VERSION_MODE, VersionMode.class);
	}

	/**
	 * Setter for the flag that will be used to control widget processing when the version view content is created. If
	 * {@link Boolean#TRUE} the widget will be processed, before storing the content, if {@link Boolean#FALSE} the
	 * widget processing will be skipped.
	 *
	 * @param shouldProcess <code>true</code> if the widgets should be processed, <code>false</code> otherwise
	 * @return current context object to allow methods chaining
	 */
	public VersionContext setWidgetsProcessing(Boolean shouldProcess) {
		put(PROCESS_WIDGETS, shouldProcess);
		return this;
	}

	/**
	 * Getter for the flag that will be used to control widget processing when the version view content is created. By
	 * default will return <code>true</code>.
	 *
	 * @return <code>true</code> if the widgets should be processed, <code>false</code> otherwise
	 */
	public boolean shouldProcessWidgets() {
		return getIfSameType(PROCESS_WIDGETS, Boolean.class, Boolean.TRUE).booleanValue();
	}

	/**
	 * Disables object properties versioning. If this method is called the logic that is responsible for instance object
	 * properties versioning will be skipped and the properties will stay unchanged.
	 *
	 * @return current context object to allow methods chaining
	 */
	public VersionContext disableObjectPropertiesVersioning() {
		put(OBJECT_PROPERTIES_VERSIONING_ENABLED, Boolean.FALSE);
		return this;
	}

	/**
	 * Checks if object properties should be versioned, when the instance version is created. By default the logic is
	 * enabled. The method will return {@code true} even if it is not explicitly set.
	 *
	 * @return {@code true} if the object properties should be versioned, {@code false} otherwise
	 */
	public boolean isObjectPropertiesVersioningEnabled() {
		return getIfSameType(OBJECT_PROPERTIES_VERSIONING_ENABLED, Boolean.class, Boolean.TRUE).booleanValue();
	}
}