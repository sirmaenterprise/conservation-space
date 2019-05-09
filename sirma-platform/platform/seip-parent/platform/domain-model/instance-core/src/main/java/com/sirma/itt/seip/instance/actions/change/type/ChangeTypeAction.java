package com.sirma.itt.seip.instance.actions.change.type;

import java.util.Collection;

import javax.inject.Inject;

import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.security.ActionTypeConstants;
import com.sirma.itt.seip.instance.DomainInstanceService;
import com.sirma.itt.seip.instance.InstanceSaveContext;
import com.sirma.itt.seip.instance.actions.Action;
import com.sirma.itt.seip.instance.actions.Actions;
import com.sirma.itt.seip.instance.actions.save.SaveRequest;
import com.sirma.itt.seip.instance.state.Operation;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Action that allows changing the type and/or sub type of an instance. The action performs a save of the given instance
 * and will also save any related instance that is not directly affected by the operation. These are instances that have
 * one way relations from them to the target instance.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 13/02/2019
 */
@Extension(target = Action.TARGET_NAME, order = 456)
public class ChangeTypeAction implements Action<ChangeTypeRequest> {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "instance.actions.changeType.maxReferringInstances",
			defaultValue = "50", type = Integer.class, subSystem = "actions",
			label = "Defines the maximum referring instance that the operation should try to save with the current "
					+ "instance that have it's type changed. These are instances that have relations to the current "
					+ "instance bot not in other other direction.")
	private ConfigurationProperty<Integer> maxReferringInstances;

	@Inject
	private InstanceTypeMigrationCoordinator migrationCoordinator;
	@Inject
	private DomainInstanceService domainInstanceService;
	@Inject
	private javax.enterprise.inject.Instance<Actions> actions;

	@Override
	public void validate(ChangeTypeRequest request) {
		Instance unmodifiedInstance = domainInstanceService.loadInstance(request.getTargetId().toString());
		Instance updatedInstance = request.getInstance();
		if (EqualsHelper.nullSafeEquals(unmodifiedInstance.getIdentifier(), updatedInstance.getIdentifier())) {
			throw new BadRequestException("The instance type is not modified. Cannot execute operation changeType");
		}
		if (!unmodifiedInstance.type().equals(updatedInstance.type())) {
			int expectedModifyCount = migrationCoordinator.countAffectedInstanceOfTypeChangeOf(request.getTargetId(),
					updatedInstance.type());
			if (expectedModifyCount > maxReferringInstances.get()) {
				throw new BadRequestException(
						String.format("Found %s > %s then allowed referring instances. Cannot perform action!",
								expectedModifyCount, maxReferringInstances.get()));
			}
		}
	}

	@Override
	public Object perform(ChangeTypeRequest request) {
		Instance unmodifiedInstance = domainInstanceService.loadInstance(request.getTargetId().toString());
		Instance instance = request.getInstance();
		Instance updatedInstance = domainInstanceService.save(InstanceSaveContext.create(instance,
				new Operation(ActionTypeConstants.CREATE, ChangeTypeRequest.OPERATION_NAME, true)));

		// if we have chosen to change the type to a sibling of the current we check
		// for instances with properties that do not match the new class structure
		if (!unmodifiedInstance.type().equals(instance.type())) {
			// process instances that point to the current instance but there is no inverse relation from the current to the other instance
			// in this case we need to update and save the other instance to remove the incompatible relation
			Collection<Instance> affectedInstances = migrationCoordinator.getAffectedInstanceOfTypeChangeOf(
					instance.getId(), instance.type());
			// use Actions to save the other instances so that they have their audit log
			// and scripts executed. If this is not needed simple save will be fine
			Actions actionsInstance = actions.get();

			// we should do something about this if the affected instances is more than 20-30..
			for (Instance affectedInstance : affectedInstances) {
				SaveRequest saveRequest = SaveRequest.buildUpdateRequest(affectedInstance);
				// join in the current transaction
				actionsInstance.callSlowAction(saveRequest);
			}
		}
		return updatedInstance;
	}

	@Override
	public String getName() {
		return ChangeTypeRequest.OPERATION_NAME;
	}

	@Override
	public boolean shouldLockInstanceBeforeAction(ChangeTypeRequest request) {
		return true;
	}
}
