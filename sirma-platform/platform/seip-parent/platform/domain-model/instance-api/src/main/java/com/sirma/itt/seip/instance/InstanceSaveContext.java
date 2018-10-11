package com.sirma.itt.seip.instance;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.save.event.InstanceSaveEvent;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionContext;

/**
 * Contains all of the information that is needed to complete instance save operation. Used to hold and share
 * information between the different phases of the instance save process.<br>
 * Also contains method for building {@link VersionContext} used for creating the version for the current target
 * instance.
 *
 * @author A. Kunchev
 */
public class InstanceSaveContext extends Context<String, Object> {

	private static final long serialVersionUID = 5575886229467220002L;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int INITIAL_CONTEXT_MAP_SIZE = 8;

	private static final String TARGET_INSTANCE = "targetInstance";

	private static final String OPERATION = "operation";

	private static final String VERSION_CREATION_DATE = "versionDate";

	private static final String INSTANCE_VIEW_ID = "targetViewId";

	private static final String VERSION_CONTEXT = "versionContext";

	private static final String SAVE_EVENT = "saveEvent";

	private static final String VALIDATION_ENABLED = "validationEnabled";

	private static final String VALIDATION_DISABLE_REASON = "validationDisableReason";

	private InstanceSaveContext() {
		super(INITIAL_CONTEXT_MAP_SIZE);
	}

	/**
	 * Creates new {@link InstanceSaveContext} object with target instance and operation. Version creation date will be
	 * initialized with the current date.
	 *
	 * @param target
	 *            the instance that should be saved
	 * @param operation
	 *            the operation with which was triggered save
	 * @return new {@link InstanceSaveContext}
	 */
	public static InstanceSaveContext create(Instance target, Operation operation) {
		return create(target, operation, new Date());
	}

	/**
	 * Creates new {@link InstanceSaveContext}.
	 *
	 * @param target
	 *            the instance that should be saved
	 * @param operation
	 *            the operation with which was triggered save
	 * @param versionDate
	 *            date when the version of the instance is saved. This parameter is required. This is different date
	 *            then {@link DefaultProperties#CREATED_ON}. If the method is used for multiple instances saving this
	 *            date should be the same for all of them, in order to save the correct data for the instances and their
	 *            versions
	 * @return new {@link InstanceSaveContext}
	 */
	public static InstanceSaveContext create(Instance target, Operation operation, Date versionDate) {
		requireNonNull(target, "Target instance is required!");
		requireNonNull(operation, "Operation is required!");
		requireNonNull(versionDate, "Version creation date is required!");
		InstanceSaveContext context = new InstanceSaveContext();
		context.put(TARGET_INSTANCE, target);
		context.put(OPERATION, operation);
		context.put(VERSION_CREATION_DATE, versionDate);
		return context;
	}

	/**
	 * Builds {@link VersionContext} object using the data stored in the current context. <br>
	 *
	 * @see {@link VersionContext#create(Instance, Date, Boolean)}
	 * @return new {@link VersionContext} object used for creating version for the current target instance
	 */
	public VersionContext getVersionContext() {
		return (VersionContext) computeIfAbsent(VERSION_CONTEXT,
				context -> VersionContext.create(getInstance(), getVersionCreationDate()));
	}

	/**
	 * Sets the id of the instance view content, when it is saved. Used for rollback operations for cleaning up the
	 * saved content, if the instance save is not completed correctly.
	 *
	 * @param viewId
	 *            the id of the instance view content
	 * @return current {@link InstanceSaveContext}
	 */
	public InstanceSaveContext setViewId(Optional<String> viewId) {
		put(INSTANCE_VIEW_ID, viewId);
		return this;
	}

	/**
	 * Convenient method for setting properties to the current context.
	 *
	 * @param key
	 *            the key associated with this property
	 * @param value
	 *            the value that should be added to the context
	 * @return current {@link InstanceSaveContext}
	 */
	public InstanceSaveContext setPropertyIfNotNull(String key, Object value) {
		if (StringUtils.isBlank(key)) {
			throw new IllegalArgumentException();
		}

		if (value == null) {
			LOGGER.trace("Invalid value for property - [{}].", key);
			return this;
		}

		put(key, value);
		return this;
	}

	public Instance getInstance() {
		return getIfSameType(TARGET_INSTANCE, Instance.class);
	}

	public String getInstanceId() {
		return (String) getInstance().getId();
	}

	public Operation getOperation() {
		return getIfSameType(OPERATION, Operation.class);
	}

	public Date getVersionCreationDate() {
		return getIfSameType(VERSION_CREATION_DATE, Date.class);
	}

	public Optional<String> getViewId() {
		return getIfSameType(INSTANCE_VIEW_ID, Optional.class, Optional.empty());
	}

	public InstanceSaveContext setSaveEvent(InstanceSaveEvent event) {
		put(SAVE_EVENT, event);
		return this;
	}

	public InstanceSaveEvent getSaveEvent() {
		return getIfSameType(SAVE_EVENT, InstanceSaveEvent.class);
	}

	/**
	 * Checks if validation on save should be performed or not. If validation is disabled then the method
	 * {@link #getDisableValidationReason()} should return the reason why the validation is disabled
	 *
	 * @return true if the validation is enabled
	 */
	public boolean isValidationEnabled() {
		// by default the validation is enabled even if not explicitly set
		return getIfSameType(VALIDATION_ENABLED, Boolean.class, Boolean.TRUE);
	}

	/**
	 * Disable instance validation on save. The caller should provide a meaningful information message why the
	 * validation is disabled in order to track problems introduced by such operation. The reason could be obtained
	 * later from the method {@link #getDisableValidationReason()}
	 *
	 * @param disableReason the disable reason to set. Required argument.
	 * @return the same instance to allow method chaining
	 */
	public InstanceSaveContext disableValidation(String disableReason) {
		Objects.requireNonNull(StringUtils.trimToNull(disableReason), "Validation disable reason is mandatory");
		put(VALIDATION_DISABLE_REASON, disableReason);
		put(VALIDATION_ENABLED, Boolean.FALSE);
		return this;
	}

	/**
	 * Fetches the validation disable reason if the validation is disabled.
	 *
	 * @return the disabled reason if the validation is not enabled.
	 */
	public String getDisableValidationReason() {
		return getIfSameType(VALIDATION_DISABLE_REASON, String.class);
	}

}
