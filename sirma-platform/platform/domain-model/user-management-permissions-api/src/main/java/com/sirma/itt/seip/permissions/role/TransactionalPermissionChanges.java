package com.sirma.itt.seip.permissions.role;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.TransactionScoped;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.tx.TransactionSupport;

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
@TransactionScoped
public class TransactionalPermissionChanges implements Serializable {

	private Map<InstanceReference, PermissionsChangeBuilder> changes = new ConcurrentHashMap<>();

	private PermissionService permissionService;
	// Instance because the implementation is not passivation capable
	private Instance<TransactionSupport> transactionSupport;

	private boolean allowedAutomaticFlush = true;

	/**
	 * Default constructor for serialization purposes
	 */
	public TransactionalPermissionChanges() {
		// to allow serialization
	}

	/**
	 * Initialize this changes store.
	 *
	 * @param permissionService the service to be used for permission store
	 * @param transactionSupport a transactional support instance to resolve
	 */
	@Inject
	public TransactionalPermissionChanges(PermissionService permissionService,
			Instance<TransactionSupport> transactionSupport) {
		this.permissionService = permissionService;
		this.transactionSupport = transactionSupport;
	}

	/**
	 * Register the current instance to be automatically flushed at the end of the transaction.
	 */
	@PostConstruct
	public void registerOnTransactionEnd() {
		// write the changes at the end of the transaction that triggered this instance
		transactionSupport.get().invokeBeforeTransactionCompletionInTx(() -> {
			if (isAutomaticFlushEnabled()) {
				Options.DISABLE_AUDIT_LOG.enable();
				try {
					drainChanges(permissionService::setPermissions);
				} finally {
					Options.DISABLE_AUDIT_LOG.disable();
				}
			}
		});
	}

	/**
	 * Fetch a {@link PermissionsChangeBuilder} that is associated with the given instance reference.
	 *
	 * @param reference the reference of the instance that needs permission changes
	 * @return a non null builder instance that can record the permission changes
	 */
	public PermissionsChangeBuilder builder(InstanceReference reference) {
		Objects.requireNonNull(reference, "The instance reference is required!");
		return changes.computeIfAbsent(reference, k -> PermissionsChange.builder());
	}

	/**
	 * List all recorded permission changes. The method does not modify the store in any way
	 *
	 * @param onChange change consumer. The first argument is the affected instance and the second is the list of changes
	 */
	public void forEach(BiConsumer<InstanceReference, List<PermissionsChange>> onChange) {
		changes.forEach((ref, builder) -> onChange.accept(ref, builder.build()));
	}

	/**
	 * One time method that will remove all registered permission entries at the end of the invocation
	 *
	 * @param onChange change consumer. The first argument is the affected instance and the second is the list of changes
	 */
	public void drainChanges(BiConsumer<InstanceReference, List<PermissionsChange>> onChange) {
		changes.forEach((ref, builder) -> onChange.accept(ref, builder.buildAndReset()));
		changes.clear();
	}

	/**
	 * Checks if automatic changes flush will happen at the end of the transaction
	 *
	 * @return true if all recorded changes will be flushed automatically. Default value is {@code true}
	 */
	public boolean isAutomaticFlushEnabled() {
		return allowedAutomaticFlush;
	}

	/**
	 * Change the automatic flush mode to {@code false}. With this the automatic flush mode will be disabled and should
	 * be done manually
	 */
	public void disableAutomaticFlush() {
		allowedAutomaticFlush = true;
	}
}
