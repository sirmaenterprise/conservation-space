package com.sirma.itt.seip.tenant.context;

import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SIZED;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.concurrent.collections.FixedBatchSpliterator;
import com.sirma.itt.seip.db.CoreDb;
import com.sirma.itt.seip.db.DatabaseIdManager;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.db.exceptions.DatabaseException;
import com.sirma.itt.seip.tenant.exception.TenantOperationException;
import com.sirma.itt.seip.tenant.exception.TenantValidationException;

/**
 * Default tenant manager. Provides access to the tenant info persistent storage.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefaultTenantManager implements TenantManager {

	private static final int CHARACTERISTICS = IMMUTABLE | NONNULL | ORDERED | SIZED | DISTINCT;

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final String TENANT_ID = "tenantId";

	@Inject
	@CoreDb
	private DbDao dbDao;

	private List<Consumer<TenantInfo>> addObservers = new CopyOnWriteArrayList<>();
	private List<Consumer<TenantInfo>> removeObservers = new CopyOnWriteArrayList<>();

	@Inject
	private DatabaseIdManager idManager;

	@Override
	public Stream<TenantInfo> getActiveTenantsInfo(boolean parallel) {
		List<String> tenantIds = dbDao.fetchWithNamed(TenantEntity.QUERY_ACTIVE_TENANT_IDS_KEY,
				Collections.emptyList());

		return StreamSupport.stream(createPerTenantSpliterator(tenantIds), parallel).map(TenantInfo::new);
	}

	@Override
	public Stream<TenantInfo> getAllTenantsInfo(boolean parallel) {
		List<String> tenantIds = dbDao.fetchWithNamed(TenantEntity.QUERY_ALL_TENANTS_IDS_KEY, Collections.emptyList());

		return StreamSupport.stream(createPerTenantSpliterator(tenantIds), parallel).map(TenantInfo::new);
	}

	private static Spliterator<String> createPerTenantSpliterator(List<String> tenantIds) {
		return new FixedBatchSpliterator<>(Spliterators.spliterator(tenantIds, CHARACTERISTICS), 1);
	}

	@Override
	public void addOnTenantRemoveListener(Consumer<TenantInfo> observer) {
		((CopyOnWriteArrayList<Consumer<TenantInfo>>) removeObservers).addIfAbsent(observer);
	}

	@Override
	public void addOnTenantAddedListener(Consumer<TenantInfo> observer) {
		((CopyOnWriteArrayList<Consumer<TenantInfo>>) addObservers).addIfAbsent(observer);
	}

	@Override
	public Optional<Tenant> getTenant(String tenantId) {
		Objects.requireNonNull(tenantId, "Tenant id is required");

		TenantEntity tenantEntity = loadTenantById(tenantId);

		return Optional.ofNullable(convertFromEntity(tenantEntity));
	}

	@Override
	public Collection<Tenant> getAllTenants() {
		return dbDao.fetchWithNamed(TenantEntity.QUERY_ALL_TENANTS_INFO_KEY, Collections.emptyList()).stream()
				.map(entity -> convertFromEntity((TenantEntity) entity)).collect(Collectors.toList());
	}

	private static Tenant convertFromEntity(TenantEntity tenantEntity) {
		if (tenantEntity == null) {
			return null;
		}
		Tenant tenant = new Tenant();
		tenant.setTenantId(tenantEntity.getId());
		tenant.setTenantAdmin(tenantEntity.getTenantAdmin());
		tenant.setDescription(tenantEntity.getDescription());
		tenant.setDisplayName(tenantEntity.getDisplayName());
		tenant.setStatus(tenantEntity.getStatus());
		return tenant;
	}

	@Override
	public void addNewTenant(Tenant tenant) throws TenantValidationException {
		checkRequiredData(tenant);

		TenantEntity tenantEntity = loadTenantById(tenant.getTenantId());
		if (tenantEntity != null) {
			throw new TenantValidationException("Tenant [" + tenant + "] already exists");
		}

		tenantEntity = convertToEntity(tenant);

		idManager.register(tenantEntity);
		try {
			dbDao.saveOrUpdate(tenantEntity);
		} finally {
			idManager.unregisterId(tenantEntity.getId());
		}
	}

	@Override
	public void updateTenant(Tenant tenant) throws TenantValidationException {
		checkRequiredData(tenant);

		TenantEntity tenantEntity = loadTenantById(tenant.getTenantId());
		if (tenantEntity == null) {
			throw new TenantValidationException("Tenant " + tenant.getTenantId() + "does not exist!");
		}

		tenantEntity.setDescription(tenant.getDescription());
		tenantEntity.setTenantAdmin(tenant.getTenantAdmin());
		tenantEntity.setDisplayName(tenant.getDisplayName());

		dbDao.saveOrUpdate(tenantEntity);
	}

	private static void checkRequiredData(Tenant tenant) throws TenantValidationException {
		if (tenant == null) {
			throw new TenantValidationException("Tenant object should not be null");
		}
		if (StringUtils.isBlank(tenant.getTenantId())) {
			throw new TenantValidationException("Tenant id is required");
		}
		if (StringUtils.isBlank(tenant.getTenantAdmin())) {
			throw new TenantValidationException("Tenant admin name is required");
		}
	}

	private static TenantEntity convertToEntity(Tenant tenant) {
		TenantEntity entity = new TenantEntity();
		entity.setId(tenant.getTenantId());
		entity.setStatus(tenant.getStatus());
		entity.setDescription(tenant.getDescription());
		entity.setDisplayName(tenant.getDisplayName());
		entity.setTenantAdmin(tenant.getTenantAdmin());

		if (entity.getDisplayName() == null) {
			entity.setDisplayName(tenant.getTenantId());
		}
		return entity;
	}

	private TenantEntity loadTenantById(String tenantId) {
		// this could be replaced by cache call later
		try {
			return dbDao.find(TenantEntity.class, tenantId);
		} catch (DatabaseException e) {
			LOGGER.trace("Tenant with id {} was not found", tenantId, e);
		}
		return null;
	}

	@Override
	@Transactional(TxType.REQUIRES_NEW)
	public void activeTenant(String tenantId) throws TenantValidationException {
		changeTenantState(tenantId, TenantStatus.ACTIVE);
	}

	@Override
	public void deactivateTenant(String tenantId) throws TenantValidationException {
		changeTenantState(tenantId, TenantStatus.INACTIVE);
	}

	@Override
	public void markTenantForDeletion(String tenantId) throws TenantValidationException {
		changeTenantState(tenantId, TenantStatus.DELETED);
	}

	/**
	 * Change tenant status. If the state is the same as requested nothing will be done
	 *
	 * @param tenantId
	 *            the tenant id
	 * @param status
	 *            the new status
	 */
	private void changeTenantState(String tenantId, TenantStatus status) throws TenantValidationException {
		Objects.requireNonNull(tenantId, "Tenant id should not be null");

		List<Pair<String, Object>> args = new ArrayList<>(2);
		args.add(new Pair<>(TENANT_ID, tenantId));
		args.add(new Pair<>("status", status.getValue()));

		// use update statement due to some problem with entity save where the state field does not
		// change after save
		int updated = dbDao.executeUpdate(TenantEntity.CHANGE_TENANT_STATE_KEY, args);

		if (updated != 1) {
			throw new TenantValidationException("Cannot change tenant status to " + status.name() + " tenant "
					+ tenantId + " because does not exist!");
		}
	}

	private static List<Throwable> notifyObservers(TenantInfo tenantInfo, List<Consumer<TenantInfo>> observers) {
		List<Throwable> suppressedExceptions = new LinkedList<>();
		observers.forEach(c -> {
			try {
				c.accept(tenantInfo);
			} catch (RuntimeException e) {
				suppressedExceptions.add(e);
			}
		});
		return suppressedExceptions;
	}

	@Override
	public boolean tenantExists(String tenantId) {
		// this could be replaced by cache call later
		return !dbDao
				.fetchWithNamed(TenantEntity.QUERY_CHECK_TENANT_EXISTS_BY_ID_KEY,
						Collections.singletonList(new Pair<>(TENANT_ID, tenantId)))
					.isEmpty();
	}

	@Override
	public boolean isTenantActive(String tenantId) {
		// this could be replaced by cache call later
		return !dbDao
				.fetchWithNamed(TenantEntity.QUERY_ACTIVE_TENANT_ID_BY_ID_KEY,
						Collections.singletonList(new Pair<>(TENANT_ID, tenantId)))
					.isEmpty();
	}

	@Override
	public void finishTenantActivation(String tenantId) throws TenantValidationException {
		if (!tenantExists(tenantId)) {
				throw new TenantValidationException("No such tenant " + tenantId);
			}
			List<Throwable> suppressedExceptions = notifyObservers(new TenantInfo(tenantId), addObservers);
			if (!suppressedExceptions.isEmpty()) {
				TenantOperationException exception = new TenantOperationException(
						"Some observers for tenant activation failed to process the event");
				suppressedExceptions.forEach(exception::addSuppressed);
				throw exception;
		}
	}

	@Override
	public void callTenantRemovedListeners(String tenantId) {
		List<Throwable> suppressedExceptions = notifyObservers(new TenantInfo(tenantId), removeObservers);
		if (!suppressedExceptions.isEmpty()) {
			TenantOperationException exception = new TenantOperationException(
					"Some observers for tenant deactivation failed to process the event");
			suppressedExceptions.forEach(exception::addSuppressed);
			throw exception;
		}
	}

	@Override
	public void deleteMarkedTenants() {
		dbDao.executeUpdateInNewTx(TenantEntity.DELETE_TENANT_KEY, Collections.emptyList());
	}
}
