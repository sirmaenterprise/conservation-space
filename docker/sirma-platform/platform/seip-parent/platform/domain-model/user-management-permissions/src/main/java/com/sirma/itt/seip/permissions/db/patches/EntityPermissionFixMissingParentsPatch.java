package com.sirma.itt.seip.permissions.db.patches;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.permissions.InstancePermissionsHierarchyResolver;
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
 * Fixes an issues where the parent relationship is missing in the {@link EntityPermission} data.<br/>
 * <br/>
 * Gets all {@link EntityPermission} records that have no parent, looks if there is an appropriate.
 *
 * @author Adrian Mitev
 */
public class EntityPermissionFixMissingParentsPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private InstanceTypeResolver instanceTypeResolver;
	private TransactionSupport transactionSupport;
	private InstancePermissionsHierarchyResolver hierarchyResolver;
	private DbDao dbDao;

	@Override
	public void setUp() throws SetupException {
		instanceTypeResolver = CDI.instantiateBean(InstanceTypeResolver.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());
		hierarchyResolver = CDI.instantiateBean(InstancePermissionsHierarchyResolver.class, CDI.getCachedBeanManager(),
				CDI.getDefaultLiteral());

		dbDao = CDI.instantiateBean(DbDao.class, CDI.getCachedBeanManager(), CDI.getDefaultLiteral());
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		TimeTracker tracker = TimeTracker.createAndStart();

		transactionSupport.invokeInNewTx(this::migrate);

		LOGGER.info("Fix missing entity permission parent in {} ms.", tracker.stop());
	}

	@SuppressWarnings("unchecked")
	void migrate() {
		List<Serializable> instanceIds = dbDao.fetch("select targetId from EntityPermission where parent is null",
				Collections.emptyList());

		LOGGER.debug("Found {} instances", instanceIds.size());

		Collection<InstanceReference> references = instanceTypeResolver.resolveReferences(instanceIds);

		LOGGER.debug("Resolved {} references", references.size());

		int count = 0;

		for (InstanceReference reference : references) {
			InstanceReference parent = hierarchyResolver.getPermissionInheritanceFrom(reference);

			if (parent != null) {
				List<Pair<String, Object>> args = new ArrayList<>(2);
				args.add(new Pair<>("targetId", Collections.singletonList(reference.getId())));
				args.add(new Pair<>("parentId", parent.getId()));
				int updatedCount = dbDao.executeUpdate(EntityPermission.QUERY_UPDATE_PARENT_FOR_TARGET_KEY, args);
				count = count + updatedCount;
			}
		}

		LOGGER.debug("Updated {} EntityPermission records", count);
	}

	@Override
	public String getConfirmationMessage() {
		return "Fixing missing parents for EntityPermission was successful!";
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
