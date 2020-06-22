package com.sirma.itt.seip.permissions.db.patches;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.ShortUri;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.role.EntityPermission;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * For each EntityPermission assigns a reference to its library (the {@link EntityPermission} that corresponds to the
 * entity library).
 *
 * @author Adrian Mitev
 */
public class EntityPermissionLibrariesPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private InstanceTypeResolver instanceTypeResolver;
	private TypeConverter typeConverter;
	private TransactionSupport transactionSupport;
	private DbDao dbDao;

	@Override
	public void setUp() throws SetupException {
		instanceTypeResolver = CDI.instantiateBean(InstanceTypeResolver.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		typeConverter = CDI.instantiateBean(TypeConverter.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());

		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		transactionSupport.invokeInNewTx(() -> this.migrate());

		LOGGER.info("Migrated entity permission libraries in {} ms.", tracker.stop());
	}

	/**
	 * Fetches all {@link EntityPermission} records, removes the records that correspond to libraries and set their
	 * library reference.
	 */
	@SuppressWarnings("unchecked")
	void migrate() {
		List<Serializable> instanceIds = dbDao.fetch("select targetId from EntityPermission where library is null",
				Collections.emptyList());

		LOGGER.debug("Found {} instances", instanceIds.size());

		Map<Serializable, InstanceType> instanceTypes = instanceTypeResolver.resolve(instanceIds);

		LOGGER.debug("Resolved libraries for {} instances", instanceTypes.size());

		long count = instanceTypes
				.entrySet()
					.stream()
					.filter((entry) -> !isLibrary(entry.getValue().getId()))
					.mapToInt(entry -> {
						String libraryId = toShortUri(entry.getValue().getId());
						List<Pair<String, Object>> args = new ArrayList<>(2);
						args.add(new Pair<>("targetId", entry.getKey()));
						args.add(new Pair<>("libraryId", libraryId));
						return dbDao.executeUpdate(EntityPermission.QUERY_UPDATE_LIBRARY_FOR_TARGET_KEY, args);
					})
					.sum();

		LOGGER.debug("Processed {} instances", count);
	}

	private boolean isLibrary(Serializable type) {
		return ((String) type).endsWith("ClassDescription");
	}

	private String toShortUri(Serializable fullURI) {
		return typeConverter.convert(ShortUri.class, fullURI).toString();
	}

	@Override
	public String getConfirmationMessage() {
		return "Setting libraries for EntityPermission was successful!";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// Not used
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}

}
