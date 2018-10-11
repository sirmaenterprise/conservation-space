package com.sirma.itt.seip.permissions.role;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.configuration.Options;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.permissions.PermissionService;
import com.sirma.itt.seip.permissions.role.PermissionsChange.PermissionsChangeBuilder;
import com.sirma.itt.seip.tx.util.TxUtils;

/**
 * EJB implementation of the transactional permissions changes. The changes are stored in a thread local variable and
 * before the current transaction is commited, the permissions are saved. If the builder method is invoked with no
 * active transaction a new one will be started. If an exception occurs during saving the permissions the transaction
 * will be rollbacked.
 *
 * @author smustafov
 */
@TransactionScoped
public class TransactionalPermissionChangesImpl
		implements TransactionalPermissionChanges, Synchronization, Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private ConcurrentHashMap<InstanceReference, PermissionsChangeBuilder> changes = new ConcurrentHashMap<>();
	private boolean allowedAutomaticFlush = true;

	@Inject
	private transient PermissionService permissionService;

	@Inject
	private transient TransactionManager transactionManager;

	private volatile boolean flushed = false;

	@PostConstruct
	void register() {
		try {
			Transaction transaction = transactionManager.getTransaction();
			if (transaction != null) {
				int status = transaction.getStatus();
				if (TxUtils.isActive(status)) {
					transaction.registerSynchronization(this);
				} else {
					throw new IllegalStateException("Cannot register permission changes in transaction with status "
							+ TxUtils.getStatusString(status));
				}
			} else {
				throw new IllegalStateException("Cannot register permission changes without transaction");
			}
		} catch (SystemException | RollbackException e) {
			throw new IllegalStateException("Cannot resolve current transaction", e);
		}
	}

	@Override
	public PermissionsChangeBuilder builder(InstanceReference reference) {
		Objects.requireNonNull(reference, "The instance reference is required!");
		checkIsModificationAllowed();
		return changes.computeIfAbsent(reference, k -> PermissionsChange.builder());
	}

	@Override
	public void disableAutomaticFlush() {
		checkIsModificationAllowed();
		allowedAutomaticFlush = false;
	}

	private boolean isAutomaticFlushAllowed() {
		return allowedAutomaticFlush;
	}

	@Override
	public void drainChanges(BiConsumer<InstanceReference, List<PermissionsChange>> onChange) {
		LOGGER.trace("Draining {} changes. Automatic flush enabled: {}", changes.size(),
				isAutomaticFlushAllowed());
		changes.forEach((ref, builder) -> onChange.accept(ref, builder.buildAndReset()));
	}

	@Override
	public void beforeCompletion() {
		LOGGER.trace("Processing {} changes. Automatic flush enabled: {}", changes.size(),
				isAutomaticFlushAllowed());

		if (isAutomaticFlushAllowed() && isCommitAllowed()) {
			Options.DISABLE_AUDIT_LOG.enable();
			try {
				drainChanges(permissionService::setPermissions);
			} finally {
				Options.DISABLE_AUDIT_LOG.disable();
			}
		}
		flushed = true;
	}

	private boolean isCommitAllowed() {
		try {
			Transaction transaction = transactionManager.getTransaction();
			int status = transaction.getStatus();
			return !TxUtils.isRollback(status);
		} catch (SystemException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void afterCompletion(int status) {
		// this method is not relevant to the work of this code
		LOGGER.trace("Processed {} changes. Automatic flush enabled: {}", changes.size(), isAutomaticFlushAllowed());
		changes.clear();
		allowedAutomaticFlush = true;
	}

	private void checkIsModificationAllowed() {
		if (flushed) {
			throw new IllegalStateException(
					"Cannot add more permission changes as the current transaction is committed");
		}
	}
}
