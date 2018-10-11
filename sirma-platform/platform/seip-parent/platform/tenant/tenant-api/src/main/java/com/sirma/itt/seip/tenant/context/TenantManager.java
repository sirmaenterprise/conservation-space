/*
 *
 */
package com.sirma.itt.seip.tenant.context;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.sirma.itt.seip.tenant.exception.TenantValidationException;

/**
 * Tenant manager is responsible to persisting and loading tenant information. It's not responsible
 * for the tenant creation process.
 *
 * @author BBonev
 */
public interface TenantManager {

	/**
	 * Checks if tenant exists with the given tenant id. The tenant may exists but it may be
	 * inactive.
	 *
	 * @param tenantId
	 *            the tenant id to check
	 * @return true, if is tenant exists
	 */
	boolean tenantExists(String tenantId);

	/**
	 * Checks if tenant exists and is active.
	 *
	 * @param tenantId
	 *            the tenant id to check
	 * @return true, if is tenant exists and is active.
	 */
	boolean isTenantActive(String tenantId);

	/**
	 * Returns a full tenant info
	 * <p>
	 * <b> Note that this method could be called only by users registered in the requested tenant.
	 * </b>
	 *
	 * @param tenantId
	 *            the tenant id
	 * @return the optional object that contains the tenant info.
	 */
	Optional<Tenant> getTenant(String tenantId);

	/**
	 * Gets the all tenants.
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator and will fail
	 * if not such.</b>
	 *
	 * @return the all tenants
	 */
	Collection<Tenant> getAllTenants();

	/**
	 * Adds the new tenant. If the added tenant is activated then observers registered via
	 * {@link #addOnTenantAddedListener(Consumer)} will be notified otherwise will be notified upon
	 * it's activation.
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator and will fail
	 * if not such.</b>
	 *
	 * @param tenant
	 *            the tenant
	 * @exception TenantValidationException
	 *                if tenant with such id already exists or given configuration has missing
	 *                required fields
	 */
	void addNewTenant(Tenant tenant) throws TenantValidationException;

	/**
	 * Update tenant info. This method could not update the tenant status and tenant id!
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator or the old
	 * tenant administrator (if changed with the call) and will fail if not such.</b>
	 *
	 * @param tenant
	 *            the tenant
	 * @throws TenantValidationException
	 *             the tenant validation exception if there is no such tenant or has missing
	 *             required data
	 */
	void updateTenant(Tenant tenant) throws TenantValidationException;

	/**
	 * Active tenant. If tenant does not exists a {@link TenantValidationException} will be thrown.
	 * If the tenant is active nothing will be changed. <br>
	 * After successful activation the method # all observers registered via
	 * {@link #addOnTenantAddedListener(Consumer)} will be notified
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator and will fail
	 * if not such.</b>
	 *
	 * @param tenantId
	 *            the tenant id to activate
	 * @throws TenantValidationException
	 *             the tenant validation exception if tenant does not exists
	 */
	void activeTenant(String tenantId) throws TenantValidationException;

	/**
	 * Deactivate tenant. If tenant does not exists a {@link TenantValidationException} will be
	 * thrown. If the tenant is not active nothing will be changed. <br>
	 * After successful deactivation the method {@link #callTenantRemovedListeners(String)} should be
	 * called.
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator and will fail
	 * if not such.</b>
	 *
	 * @param tenantId
	 *            the tenant id to deactivate
	 * @throws TenantValidationException
	 *             the tenant validation exception if tenant does not exists
	 */
	void deactivateTenant(String tenantId) throws TenantValidationException;

	/**
	 * Mark the tenant for deletion from the db. The tenant id will be deleted from the tenants
	 * table on next server reload and that tenant id will be available again.
	 * 
	 * @param tenantId
	 *            the tenant id
	 * @throws TenantValidationException
	 *             the tenant validation exception if tenant does not exists
	 */
	void markTenantForDeletion(String tenantId) throws TenantValidationException;

	/**
	 * Gets the active tenants stream.
	 * <p>
	 * <b> This method could be called only when in system context. If there is an active tenant
	 * context will result in security exception.</b>
	 *
	 * @param parallel
	 *            <code>true</code> if the returned stream should be parallel
	 * @return the active tenants stream
	 */
	Stream<TenantInfo> getActiveTenantsInfo(boolean parallel);

	/**
	 * Gets all tenants stream. The stream will include active and inactive tenants.
	 * <p>
	 * <b> This method could be called only when in system context. If there is an active tenant
	 * context will result in security exception.</b>
	 *
	 * @param parallel
	 *            <code>true</code> if the returned stream should be parallel
	 * @return the tenants stream
	 */
	Stream<TenantInfo> getAllTenantsInfo(boolean parallel);

	/**
	 * Finish tenant activation. The method should be called after a tenant has been added in the
	 * system successfully or enabled. It should be called after all initializations are done so
	 * that all registered observers via {@link #addOnTenantAddedListener(Consumer)} to be notified.
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator and will fail
	 * if not such.</b>
	 *
	 * @param tenantId
	 *            the tenant id that was added/activated
	 * @throws TenantValidationException
	 *             if tenant with such id does not exists or is not active.
	 */
	void finishTenantActivation(String tenantId) throws TenantValidationException;

	/**
	 * The method should be called after tenant has been deleted or disabled. It should be called
	 * before the tenant to be removed so that all registered observers via
	 * {@link #addOnTenantRemoveListener(Consumer)} to be notified.
	 * <p>
	 * <b> Note that this method could be called only by system tenant administrator and will fail
	 * if not such.</b>
	 *
	 * @param tenantId
	 *            the tenant id
	 */
	void callTenantRemovedListeners(String tenantId);

	/**
	 * Adds the on tenant remove listener.
	 *
	 * @param observer
	 *            the observer
	 */
	void addOnTenantRemoveListener(Consumer<TenantInfo> observer);

	/**
	 * Adds the on tenant added listener.
	 *
	 * @param observer
	 *            the observer
	 */
	void addOnTenantAddedListener(Consumer<TenantInfo> observer);

	/**
	 * Delete all marked for deletion tenants from the tenants table. This should be executed only
	 * on server startup.
	 */
	void deleteMarkedTenants();
}