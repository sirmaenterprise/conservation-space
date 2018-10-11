package com.sirma.itt.seip.security.context;

import java.util.Objects;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.security.User;
import com.sirma.itt.seip.security.authentication.AuthenticationContext;
import com.sirma.itt.seip.security.exception.ContextNotActiveException;
import com.sirma.itt.seip.security.exception.SecurityException;

/**
 * Main entry point for managing {@link SecurityContext}.
 * <p>
 * In order to initialize the current security context one of the methods should be called:
 * <ul>
 * <li>{@link #initializeExecution(AuthenticationContext)} - initialize security context using the authentication chain
 * provided by {@link com.sirma.itt.seip.security.authentication.Authenticator} extension
 * <li>{@link #initializeExecutionAsSystemAdmin()}
 * <li>{@link #initializeTenantContext(String)}
 * <li>In addition the method {@link #initializeFromContext(SecurityContext)} could be used for initialization of
 * security context in asynchronous execution where the context is produces from {@link #createTransferableContext()}
 * </ul>
 * Should be noted that if one of the methods above is used a corresponding method that cleans the context should be
 * used. Methods that clean the context are:
 * <ul>
 * <li>{@link #endExecution()} - cleans all context initializations for the current thread.
 * <li>{@link #endContextExecution()} - cleans one context initialization for the current thread
 * </ul>
 * The manager provides wide variety of high order methods that can be used for executing different functions in
 * different security context where context initialization and cleaning is handled in the method. <br>
 * Asynchronous invocations where the security context should be preserved may be used one of the
 * {@link ContextualWrapper} methods like {@link ContextualWrapper#executable(Executable) executable(Executable)}.
 * <p>
 * Typical implementation of the manager may store the security context in a thread local so that injected
 * {@link SecurityContext} objects could be active for the current security session without the need to inject new
 * instances.
 * <p>
 * If the authenticated user should be changed during the session the methods {@link #executeWithPermissionsOf(User)}} could be used.
 *
 * @author BBonev
 */
public interface SecurityContextManager {

	/*
	 * Context initializations
	 */

	/**
	 * Method called to initialize secure context execution. The method could be called only if there is no active
	 * security context. The method should initialize the security context so anything invoked before calling
	 * {@link #endExecution()} will be executed in the security context of the given identity.
	 * <p>
	 * <b>NOTE:</b> that the method {@link #endExecution()} should be called to clean the execution context. <br>
	 * Typical use is:
	 * <p>
	 * <pre>
	 * <code>@Inject
	 * private SecurityContextManager contextManager;
	 *
	 * try {
	 * 	AuthenticationContext authenticationContext = AuthenticationContext.create(args);
	 * 	contextManager.initializeExecution(authenticationContext);
	 * 	// do some stuff
	 * } finally {
	 * 	contextManager.endExecution();
	 * }</code>
	 * </pre>
	 *
	 * @param authenticationContext the authentication context used to initialize the execution
	 * @return {@code true} if security context initialization was successful.
	 */
	boolean initializeExecution(AuthenticationContext authenticationContext);

	/**
	 * Initialize execution as system user. Alternative method to the
	 * {@link #initializeExecution(AuthenticationContext)} to initialize special system execution. The method could not
	 * be called if there is active security context.
	 */
	void initializeExecutionAsSystemAdmin();

	/**
	 * End the secure execution and deactivates the {@link SecurityContext}. After this method the
	 * {@link SecurityContext#isActive()} and {@link SecurityContext#isAuthenticated()} should return <code>false</code>
	 * .<br>
	 * This method is complimentary to {@link #initializeExecution(AuthenticationContext)} and
	 * {@link #initializeExecutionAsSystemAdmin()} methods. If this method is not called at the end of the execution and
	 * another call to initialize the context is attempted a
	 * {@link com.sirma.itt.seip.security.exception.SecurityException} will be thrown.
	 */
	void endExecution();

	/*
	 * User access methods
	 */

	/**
	 * Gets the system user. This user is used to auditing but not for authentication. This user represents the system
	 * itself. In order to execute functionality as the system correct authenticated admin user should be provided.
	 *
	 * @return the system user
	 * @see #getAdminUser()
	 */
	User getSystemUser();

	/**
	 * Gets the system admin user. This user has the system authentication credentials and could perform security
	 * operations based on the application mode. <br>
	 * If tenant mode is not active or tenant modules are not deployed then the admin user represents the system admin.
	 * <br>
	 * If tenant mode is active then the returned user is the tenant administrator. and could perform operations in the
	 * tenant context only.
	 *
	 * @return the admin user
	 */
	User getAdminUser();

	/**
	 * Gets the super admin user - admin user for all tenants.
	 *
	 * @return the super admin user
	 */
	User getSuperAdminUser();

	/*
	 * Context access methods
	 */

	/**
	 * Gets the current context. The method should not return <code>null</code> {@link SecurityContext} if no context is
	 * active but rather object that returns <code>false</code> when invoked the method
	 * {@link SecurityContext#isActive()}
	 *
	 * @return the current context
	 */
	SecurityContext getCurrentContext();

	/**
	 * Creates the transferable context. The context returned from this method is not bound to the current thread and
	 * can be transfered to other threads. If no context is active then the method returns <code>null</code>.
	 *
	 * @return transferable security context or <code>null</code>.
	 */
	SecurityContext createTransferableContext();

	/**
	 * Creates the temporary security context that can be passed to anyone that could require custom security context.
	 *
	 * @param principal the principal
	 * @return the security context
	 */
	SecurityContext createTemporaryContext(User principal);

	/*
	 * Methods for changing/initializing the current context
	 */

	/**
	 * Begin context execution. The method overrides the effective authentication for the current security context in
	 * order for the currently logged in user to perform more actions in the system but keeps the audit context.
	 *
	 * @param user the user permissions to use for executions
	 */
	void beginContextExecution(User user);

	/**
	 * Begins contextual execution using the current context as base and overriding the request id with the one passed.
	 * If no context is active an {@link ContextNotActiveException} will be thrown. If the given request is {@code
	 * null} a {@link SecurityException} will be thrown.
	 * Note that the context should be closed using {@link #endContextExecution()}
	 *
	 * @param requestId the user permissions to use for executions
	 */
	void beginContextExecution(String requestId);

	/**
	 * Initialize the current security context using the specified context. This method should be called only using the
	 * value returned from {@link #createTransferableContext()}. If not a
	 * {@link com.sirma.itt.seip.security.exception.SecurityException} will be thrown. This method should be called in
	 * pair with {@link #endContextExecution()}.
	 * <p>
	 * This method is intended to be used when creating logic that needs to transfer the current security context to
	 * other threads.
	 *
	 * @param securityContext the security context
	 */
	void initializeFromContext(SecurityContext securityContext);

	/**
	 * Initialize tenant context for the given tenant. If there is authenticated user with set tenant id this method
	 * does nothing. Otherwise initialize System security context with authenticated admin user for the given tenant.
	 * This is system functionality in order to perform per tenant operations without authenticated user. This method
	 * should be used with {@link #endContextExecution()}.
	 *
	 * @param tenantId the tenant id
	 */
	default void initializeTenantContext(String tenantId) {
		initializeTenantContext(tenantId, null);
	}

	/**
	 * Initialize tenant context for the given tenant. If there is authenticated user with set tenant id this method
	 * does nothing. Otherwise initialize System security context with authenticated admin user for the given tenant.
	 * This is system functionality in order to perform per tenant operations without authenticated user. This method
	 * should be used with {@link #endContextExecution()}.
	 *
	 * @param tenantId the tenant id
	 * @param requestId a custom requestId to set in the permission context, if null new random id will be generated
	 */
	void initializeTenantContext(String tenantId, String requestId);

	/**
	 * Clears the temporary execution context. Should be called after calling the method
	 * {@link #beginContextExecution(User)} or {@link #initializeTenantContext(String)} to clear the execution context
	 */
	void endContextExecution();

	/*
	 * Context check methods
	 */

	/**
	 * Checks if the current security context points to user authenticated with administrator permissions. If the
	 * context is initialized with {@link #initializeExecutionAsSystemAdmin()} then this method should return
	 * <code>true</code>. The method can be used in the application to control the application in order to minimize the
	 * security checks if administrator is performing an operation. The resolving could be extended using the
	 * {@link AdminResolver} extension.
	 *
	 * @return true, if is authenticated as administrator or system.
	 */
	boolean isAuthenticatedAsAdmin();

	/**
	 * Checks if the current user is system user. This means that if audit operation is performed in the log will be
	 * written the system display name.
	 *
	 * @return true, if is current user system
	 */
	default boolean isCurrentUserSystem() {
		return getSystemUser().equals(getCurrentContext().getAuthenticated());
	}

	/**
	 * Checks if the current user is system super user. This means that if audit operation is performed in the log will
	 * be written the super system display name.
	 *
	 * @return true, if is current user system
	 */
	default boolean isCurrentUserSuperAdmin() {
		return getSuperAdminUser().equals(getCurrentContext().getAuthenticated());
	}

	/**
	 * Checks if the given user is the system user.
	 *
	 * @param user the user
	 * @return true, if is system user
	 */
	default boolean isSystemUser(User user) {
		return user != null && getSystemUser().getSystemId().equals(user.getSystemId());
	}

	/*
	 * Contextual executors/wrappers
	 */

	/**
	 * Initialize {@link ContextualExecutor} that is initialized in the context of the given tenant. This method could
	 * be invoked when in system tenant scope.
	 *
	 * @param tenantId the tenant to initialize
	 * @return a contextual executor instance that invokes the functions in the specified tenant.
	 * @see #initializeTenantContext(String)
	 */
	ContextualExecutor executeAsTenant(String tenantId);

	/**
	 * Creates an executor that will work with the current user but with the permissions of the given user.
	 *
	 * @param user to use to change the current security context
	 * @return a contextual executor instance that invokes the functions in the specified user context.
	 * @see SecurityContextManager#beginContextExecution(User)
	 */
	default ContextualExecutor executeWithPermissionsOf(User user) {
		Objects.requireNonNull(user, "User is required argument");

		beginContextExecution(user);
		try {
			return new SecurityContextualExecutor(this, createTransferableContext());
		} finally {
			endContextExecution();
		}
	}

	/**
	 * Creates an executor that can perform actions in the current tenant scope with from the name of the current user
	 * with the permissions of the admin user for the current tenant
	 *
	 * @return a contextual executor instance that invokes the functions in the admin user context.
	 * @see #getAdminUser()
	 */
	default ContextualExecutor executeAsAdmin() {
		return executeWithPermissionsOf(getAdminUser());
	}

	/**
	 * Creates an executor that could perform actions from the name of the System with the permissions of the Admin user
	 * for the current tenant. <br>
	 * This method will throw {@link ContextNotActiveException} if there is not active security context. <br>
	 * This method will throw {@link SecurityException} if the current tenant is the system tenant as the system tenant
	 * does not have a admin and system users and does not operate on with data. For this purpose is the method
	 * {@link #executeAsSystemAdmin()}
	 *
	 * @return a contextual executor instance that invokes the functions in the system user context.
	 * @see #executeAsSystemAdmin()
	 */
	ContextualExecutor executeAsSystem();

	/**
	 * Creates a transferable {@link ContextualExecutor} that invokes in the system tenant context as system admin. This
	 * method cannot be used to provide execution when there is active tenant different than the system tenant.
	 *
	 * @return a contextual executor instance that invokes the functions in the system admin user context.
	 * @see #initializeExecutionAsSystemAdmin()
	 */
	ContextualExecutor executeAsSystemAdmin();

	/**
	 * Create transferable {@link ContextualExecutor} that can be passed for asynchronous execution that carry the
	 * current security context as is. If no security context is present the returned executor will also have no context
	 *
	 * @return executor that has the same context as the current one
	 * @see ContextualExecutor.NoContextualExecutor
	 */
	default ContextualExecutor executeAs() {
		if (!getCurrentContext().isActive()) {
			return ContextualExecutor.NoContextualExecutor.INSTANCE;
		}
		return new SecurityContextualExecutor(this, createTransferableContext());
	}

	/**
	 * Returns executor that uses the current security context but overrides the request id
	 *
	 * @param requestId the request id to override, cannot be {@code null}
	 * @return a executor that initialize the given request id
	 */
	default ContextualExecutor withRequestId(String requestId) {
		beginContextExecution(requestId);
		try {
			return new SecurityContextualExecutor(this, createTransferableContext());
		} finally {
			endContextExecution();
		}
	}

	/**
	 * Creates a {@link ContextualWrapper} instance that can provide functional wrapping that will carry the current
	 * security context in asynchronous invocation. If no context is present then the return wrapper will also have no
	 * context
	 *
	 * @return {@link ContextualWrapper} that carry the current context
	 * @see ContextualWrapper.NoConextualWrapper
	 */
	default ContextualWrapper wrap() {
		if (!getCurrentContext().isActive()) {
			return ContextualWrapper.NoConextualWrapper.INSTANCE;
		}
		return new SecurityContextualWrapper(this, createTransferableContext());
	}

}
