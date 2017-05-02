package com.sirma.itt.seip.instance.actions.save;

import static com.sirma.itt.seip.domain.security.ActionTypeConstants.CREATE;
import static com.sirma.itt.seip.domain.security.ActionTypeConstants.EDIT_DETAILS;

import java.util.Date;
import java.util.function.Predicate;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.instance.actions.ActionRequest;
import com.sirma.itt.seip.instance.actions.Actions;

/**
 * Contains the necessary information about instance create or update.
 *
 * <pre>
 * See com.sirma.itt.seip.instance.actions.save.CreateOrUpdateAction
 * </pre>
 * 
 * @see Actions
 * @author A. Kunchev
 */
public class CreateOrUpdateRequest extends ActionRequest {

	private static final long serialVersionUID = 8659026928953225574L;

	public static final String OPERATION_NAME = "createOrUpdate";

	private Instance target;

	private Date versionCreatedOn;

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
	 * @param target
	 *            the instance to set
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
	 * @param versionCreatedOn
	 *            the date when the version is created
	 */
	public void setVersionCreatedOn(Date versionCreatedOn) {
		this.versionCreatedOn = versionCreatedOn;
	}

	/**
	 * Builds create/update instance request.
	 *
	 * @param instance
	 *            the instance that will be created or updated
	 * @param versionCreatedOn
	 *            the date when the version for the instance is created
	 * @param isIdPersisted predicate to verify is the instance with already saved
	 *
	 * @return {@link CreateOrUpdateRequest}
	 */
	public static CreateOrUpdateRequest buildCreateOrUpdateRequest(Instance instance, Date versionCreatedOn, Predicate<Instance> isIdPersisted) {
		CreateOrUpdateRequest request = new CreateOrUpdateRequest();
		request.setUserOperation(EDIT_DETAILS);
		if (instance.getId() == null || !isIdPersisted.test(instance)) {
			request.setUserOperation(CREATE);
		}

		// we set the id, because it is required for operation executed event firing
		request.setTargetId(instance.getId());
		request.setTarget(instance);
		request.setTargetReference(instance.toReference());
		request.setVersionCreatedOn(versionCreatedOn);
		return request;
	}


}
