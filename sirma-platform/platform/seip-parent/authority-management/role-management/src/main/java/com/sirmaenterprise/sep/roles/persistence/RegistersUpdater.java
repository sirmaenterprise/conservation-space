package com.sirmaenterprise.sep.roles.persistence;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.permissions.action.ActionRegistry;
import com.sirma.itt.seip.permissions.role.RoleRegistry;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirmaenterprise.sep.roles.events.ActionDefinitionsChangedEvent;
import com.sirmaenterprise.sep.roles.events.RoleActionMappingsChangedEvent;
import com.sirmaenterprise.sep.roles.events.RoleDefinitionsChangedEvent;

/**
 * A listener class for changes that reflect the contents of the {@link ActionRegistry} and {@link RoleRegistry} and
 * upon detection triggers a reload at the end of the successful transaction.
 *
 * @author BBonev
 */
@Singleton
public class RegistersUpdater {

	@Inject
	private ActionRegistry actionRegistry;

	@Inject
	private RoleRegistry roleRegistry;

	@Inject
	private TransactionSupport transactionSupport;

	/**
	 * Listens for action definition changes and reloads the action and role registers
	 *
	 * @param event
	 *            the event
	 */
	void actionsChanged(@Observes ActionDefinitionsChangedEvent event) {
		transactionSupport.invokeOnSuccessfulTransaction(() -> {
			actionRegistry.reload();
			roleRegistry.reload();
		});
	}

	/**
	 * Listens for role definition changes and reloads the role register
	 *
	 * @param event
	 *            the event
	 */
	void rolesChanged(@Observes RoleDefinitionsChangedEvent event) {
		transactionSupport.invokeOnSuccessfulTransaction(() -> roleRegistry.reload());
	}

	/**
	 * Listens for role action mapping changes and reloads role register
	 *
	 * @param event
	 *            the event
	 */
	void roleActionMappingChanged(@Observes RoleActionMappingsChangedEvent event) {
		transactionSupport.invokeOnSuccessfulTransaction(() -> roleRegistry.reload());
	}
}
