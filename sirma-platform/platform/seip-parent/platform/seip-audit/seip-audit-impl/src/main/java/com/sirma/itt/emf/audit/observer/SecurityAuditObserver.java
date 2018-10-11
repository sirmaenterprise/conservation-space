package com.sirma.itt.emf.audit.observer;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;

import com.sirma.itt.emf.audit.processor.AuditProcessor;
import com.sirma.itt.emf.security.event.UserAuthenticatedEvent;
import com.sirma.itt.emf.security.event.UserLogoutEvent;
import com.sirma.itt.seip.permissions.PermissionModelChangedEvent;
import com.sirma.itt.seip.permissions.PermissionsRestored;
import com.sirma.itt.seip.resources.User;
import com.sirma.itt.seip.resources.event.UserPasswordChangeEvent;
import com.sirma.itt.seip.security.UserStore;

/**
 * The SecurityAuditObserver audits events related to authorization/authentication
 *
 * @author bbanchev
 */
@Auditable
@Singleton
public class SecurityAuditObserver {

	private static final String AUDIT_CONSTANT_EDIT_PERMISSIONS = "editPermissions";
	private static final String AUDIT_CONSTANT_RESTORE_PERMISSIONS = "restorePermissions";
	private static final String AUDIT_CONSTANT_LOGIN = "login";
	private static final String AUDIT_CONSTANT_LOGOUT = "logout";

	@Inject
	private AuditProcessor auditProcessor;
	@Inject
	private UserStore userStore;

	/**
	 * On edit permissions. If the set is empty no event is audited
	 *
	 * @param event
	 *            the event
	 */
	public void onEditPermissions(@Observes PermissionModelChangedEvent event) {
		if (event.getChangesSet().isEmpty()) {
			return;
		}
		auditProcessor.process(event.getInstance().toInstance(), AUDIT_CONSTANT_EDIT_PERMISSIONS, event);
	}

	/**
	 * On restore permissions.
	 *
	 * @param event
	 *            the event
	 */
	public void onRestorePermissions(@Observes PermissionsRestored event) {
		auditProcessor.process(event.getInstance(), AUDIT_CONSTANT_RESTORE_PERMISSIONS, event);
	}

	/**
	 * Observes login events in EMF.
	 *
	 * @param event
	 *            - the login event
	 */
	@Transactional
	public void onLogin(@Observes UserAuthenticatedEvent event) {
		if (event.isInitiatedByUser()) {
			auditProcessor.auditUserOperation((User) userStore.wrap(event.getAuthenticatedUser()), AUDIT_CONSTANT_LOGIN,
					event);
		}
	}

	/**
	 * Observes logout events in EMF.
	 *
	 * @param event
	 *            - the logout event
	 */
	@Transactional
	public void onLogout(@Observes UserLogoutEvent event) {
		auditProcessor.auditUserOperation((User) userStore.wrap(event.getAuthenticatedUser()), AUDIT_CONSTANT_LOGOUT,
				event);
	}

	/**
	 * Observes user password changes.
	 *
	 * @param event
	 *            - the password change event
	 */
	@Transactional
	public void onPasswordChange(@Observes UserPasswordChangeEvent event) {
		auditProcessor.process(null, event.getOperationId(), event);
	}

}
