package com.sirma.itt.seip.instance.actions.save;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;

import java.util.Date;
import java.util.function.Predicate;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.instance.version.VersionContext;
import com.sirma.itt.seip.instance.version.VersionMode;
import com.sirma.itt.seip.instance.version.VersionProperties;

/**
 * Contains the necessary information about instance create or update.
 *
 * <pre>
 * See com.sirma.itt.seip.instance.actions.save.SaveAction
 * </pre>
 *
 * @see Actions
 * @author A. Kunchev
 */
public class SaveRequest extends ActionRequest {

	private static final long serialVersionUID = 8659026928953225574L;

	public static final String OPERATION_NAME = "save";

	private Instance target;

	private Date versionCreatedOn;

	private SaveRequest() {
	}

	@Override
	public String getOperation() {
		return OPERATION_NAME;
	}

	/**
	 * Getter for the instance that will be updated or created.
	 *
	 * @return the target instance
	 */
	public Instance getTarget() {
		return target;
	}

	/**
	 * Setter for the instance that will be updated or created.
	 *
	 * @param target the instance to set
	 */
	public void setTarget(Instance target) {
		this.target = target;
	}

	/**
	 * Getter for the date when the version of the instance is created. This date is used later for version loading.
	 *
	 * @return the date when version is created
	 */
	public Date getVersionCreatedOn() {
		return versionCreatedOn;
	}

	/**
	 * Setter for the date when the version of the instance is created. This date is used later for version loading.
	 *
	 * @param versionCreatedOn the date when the version is created
	 */
	public void setVersionCreatedOn(Date versionCreatedOn) {
		this.versionCreatedOn = versionCreatedOn;
	}

	/**
	 * Builds create instance request. The version date is set as new {@link Date}.
	 *
	 * @param instance the instance that will be created or updated
	 * @return new {@link SaveRequest}
	 * @see SaveRequest#buildRequest(Instance, Date, String)
	 */
	public static SaveRequest buildCreateRequest(Instance instance) {
		return buildSaveRequest(instance, new Date(), CREATE);
	}

	/**
	 * Builds update instance request. The version date is set as new {@link Date}.
	 *
	 * @param instance the instance that will be created or updated
	 * @return new {@link SaveRequest}
	 * @see SaveRequest#buildRequest(Instance, Date, String)
	 */
	public static SaveRequest buildUpdateRequest(Instance instance) {
		return buildSaveRequest(instance, new Date(), EDIT_DETAILS);
	}

	/**
	 * Builds update instance request. The version date is set as new {@link Date}.
	 *
	 * @param instance the instance that will be created or updated
	 * @param versionCreatedOn the date when the version for the instance is created
	 * @param userOperationId the operation that should be used in the request
	 * @return new {@link SaveRequest}
	 * @see SaveRequest#buildRequest(Instance, Date, String)
	 * @see ActionTypeConstants#CREATE
	 * @see ActionTypeConstants#EDIT_DETAILS
	 */
	public static SaveRequest buildSaveRequest(Instance instance, Date versionCreatedOn, String userOperationId) {
		SaveRequest request = new SaveRequest();
		request.setUserOperation(userOperationId);
		request.setTargetId(instance.getId());
		request.setTarget(instance);
		request.setTargetReference(instance.toReference());
		request.setVersionCreatedOn(versionCreatedOn);
		return request;
	}

	/**
	 * Builds create/update instance request. The version date is set as new {@link Date}.
	 *
	 * @param instance the instance that will be created or updated
	 * @param isIdPersisted predicate to verify is the instance with already saved
	 * @return new {@link SaveRequest}
	 * @see SaveRequest#buildSaveRequest(Instance, Date, Predicate)
	 */
	public static SaveRequest buildSaveRequest(Instance instance, Predicate<Instance> isIdPersisted) {
		return buildSaveRequest(instance, new Date(), isIdPersisted);
	}

	/**
	 * Builds create/update instance request. The operation that will be set (create or edit), depends on the passed
	 * predicate.
	 *
	 * @param instance the instance that will be created or updated
	 * @param versionCreatedOn the date when the version for the instance is created
	 * @param isIdPersisted predicate to verify is the instance with already saved
	 * @return {@link SaveRequest}
	 */
	public static SaveRequest buildSaveRequest(Instance instance, Date versionCreatedOn,
			Predicate<Instance> isIdPersisted) {
		String userOperation = EDIT_DETAILS;
		if (instance.getId() == null || !isIdPersisted.test(instance)) {
			userOperation = CREATE;
		}

		return buildSaveRequest(instance, versionCreatedOn, userOperation);
	}

	/**
	 * Builds new {@link InstanceSaveContext} from the data that the current request contains. Also tries to extract the
	 * version mode from the target instance properties, if it is set, otherwise sets the mode to
	 * {@link VersionMode#MINOR} as default. <br />
	 * The key used in the instance properties for the version mode is {@link VersionProperties#VERSION_MODE}.
	 *
	 * @return new {@link InstanceSaveContext} with the available data in the current request
	 * @see InstanceSaveContext#create(Instance, Operation, Date)
	 * @see VersionContext#setVersionMode(VersionMode)
	 */
	public InstanceSaveContext toSaveContext() {
		Instance instance = getTarget();
		// we are building new operation, because #getOperation() returns dummy operation id
		Operation operation = new Operation(getUserOperation(), true);
		InstanceSaveContext saveContext = InstanceSaveContext.create(instance, operation, getVersionCreatedOn());
		VersionMode mode = VersionMode.getMode(instance.getString(VersionProperties.VERSION_MODE), VersionMode.MINOR);
		saveContext.getVersionContext().setVersionMode(mode);
		return saveContext;
	}
}