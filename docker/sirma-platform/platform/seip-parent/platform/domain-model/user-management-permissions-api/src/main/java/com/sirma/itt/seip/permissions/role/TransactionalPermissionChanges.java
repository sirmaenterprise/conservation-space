package com.sirma.itt.seip.permissions.role;

import java.util.List;
import java.util.function.BiConsumer;

import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;

/**
 * Permission changes store that can be used to collect permission changes over instance in a single transaction from
 * multiple places and to insert them at the end of the transaction. For example changes triggered from multiple events
 * could be collected and stored and then written at the end of the transaction.
 * <p>
 * Suggested manual use:
 * <p>
 * <pre>
 * <code>
 * &#64;Inject
 * private TransactionalPermissionChanges permissionChanges;
 * &#64;Inject
 * private TransactionSupport transactionSupport;
 * &#64;Inject
 * private PermissionService permissionService;
 * .....
 * permissionChanges.disableAutomaticFlush();
 * permissionChanges.builder(affectedInstanceReference).parentChange(newParent);
 * .....
 * transactionSupport.invokeBeforeTransactionCompletionInTx(
 * () -> permissionsChangeBuilder.drainChanges(permissionService::setPermissions));
 * </code>
 * </pre>
 *
 * @author BBonev
 */
public interface TransactionalPermissionChanges {

	/**
	 * Fetch a {@link PermissionsChangeBuilder} that is associated with the given instance reference.
	 *
	 * @param reference the reference of the instance that needs permission changes
	 * @return a non null builder instance that can record the permission changes
	 */
	PermissionsChangeBuilder builder(InstanceReference reference);

	/**
	 * Change the automatic flush mode to {@code false}. With this the automatic flush mode will be disabled and should
	 * be done manually
	 */
	void disableAutomaticFlush();

	/**
	 * One time method that will remove all registered permission entries at the end of the invocation
	 *
	 * @param onChange change consumer. The first argument is the affected instance and the second is the list of
	 *            changes
	 */
	void drainChanges(BiConsumer<InstanceReference, List<PermissionsChange>> onChange);

}
