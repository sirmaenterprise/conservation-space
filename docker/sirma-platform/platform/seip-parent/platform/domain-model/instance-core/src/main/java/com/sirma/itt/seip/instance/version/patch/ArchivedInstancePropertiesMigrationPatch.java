package com.sirma.itt.seip.instance.version.patch;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.function.Consumer;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Executable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.FragmentedWork;
import com.sirma.itt.seip.db.DatabaseConfiguration;
import com.sirma.itt.seip.domain.instance.ArchivedDataAccess;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.archive.properties.ArchivedPropertiesDao;
import com.sirma.itt.seip.instance.properties.PropertiesService;
import com.sirma.itt.seip.instance.version.VersionDao;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.seip.util.CDI;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

/**
 * Migrates the properties of the all version to the new table, where the properties are stored in JSON representation.
 * <br>
 * The patch uses the old logic for properties loading to populate the versions. After that the versions are passed to
 * the new properties dao, which persists the properties for the passed version. The versions ids are retrieved with
 * pure SQL query and loaded as instances via {@link VersionDao}.
 *
 * @author A. Kunchev
 */
public class ArchivedInstancePropertiesMigrationPatch implements CustomTaskChange {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final int FRAGMENT_SIZE = 10;

	private static final String FETCH_ALL_VERSIONS_IDS = "SELECT id FROM emf_archivedentity";
	private static final String FETCH_ALL_MIGRATED_VERSIONS_IDS = "SELECT versionid FROM sep_archivedjsonproperties";

	private TransactionSupport transactionSupport;
	private DataSource dataSource;
	private VersionDao versionDao;
	private ArchivedPropertiesDao archivedPropertiesDao;
	private PropertiesService propertiesService;
	private InstanceTypes instanceTypes;

	@Override
	public void setUp() throws SetupException {
		BeanManager beanManager = CDI.getCachedBeanManager();
		AnnotationLiteral<Default> defaultLiteral = CDI.getDefaultLiteral();
		transactionSupport = CDI.instantiateBean(TransactionSupport.class, beanManager, defaultLiteral);
		dataSource = CDI.instantiateBean(DatabaseConfiguration.class, beanManager, defaultLiteral).getDataSource();
		versionDao = CDI.instantiateBean(VersionDao.class, beanManager, defaultLiteral);
		archivedPropertiesDao = CDI.instantiateBean(ArchivedPropertiesDao.class, beanManager, defaultLiteral);
		propertiesService = CDI.instantiateBean(PropertiesService.class, beanManager, archivedDataAccessLiteral());
		instanceTypes = CDI.instantiateBean(InstanceTypes.class, beanManager, defaultLiteral);
	}

	private static AnnotationLiteral<ArchivedDataAccess> archivedDataAccessLiteral() {
		return new AnnotationLiteral<ArchivedDataAccess>() {
			private static final long serialVersionUID = 1L;
		};
	}

	@Override
	public void execute(Database database) throws CustomChangeException {
		Collection<String> versionIds = getAllVersionIds();
		versionIds.removeAll(getMigratedVersionIds()); // remove already migrated
		Collection<ArchivedInstance> versions = versionDao.findVersionsById(versionIds);
		Collection<String> failed = CollectionUtils.createHashSet(versions.size() / 2);
		FragmentedWork.doWork(versions, FRAGMENT_SIZE, processFragmentInNewTx(failed));
		if (CollectionUtils.isNotEmpty(failed)) {
			throw new CustomChangeException("Failed to migrate instances " + failed
					+ ". Enable TRACE logging for more information. On server restart the patch will be re-run for those instances");
		}
	}

	private Collection<String> getAllVersionIds() throws CustomChangeException {
		return getResultsWithQuery(FETCH_ALL_VERSIONS_IDS);
	}

	private Collection<String> getResultsWithQuery(String query) throws CustomChangeException {
		try (ResultSet result = dataSource.getConnection().createStatement().executeQuery(query)) {
			Collection<String> versionIds = new HashSet<>();
			while (result.next()) {
				versionIds.add(result.getString(1));
			}
			return versionIds;
		} catch (SQLException e) {
			throw new CustomChangeException("Error while executing query: " + query, e);
		}
	}

	private Collection<String> getMigratedVersionIds() throws CustomChangeException {
		return getResultsWithQuery(FETCH_ALL_MIGRATED_VERSIONS_IDS);
	}

	private Consumer<Collection<ArchivedInstance>> processFragmentInNewTx(Collection<String> failed) {
		return fragment -> transactionSupport.invokeInNewTx(migrateProperties(fragment, failed));
	}

	private Executable migrateProperties(Collection<ArchivedInstance> fragment, Collection<String> failed) {
		return () -> fragment.forEach(version -> {
			try {
				propertiesService.loadProperties(version);
				setType(version);
				archivedPropertiesDao.persist(version);
			} catch (Exception e) {
				Serializable id = version.getId();
				LOGGER.trace("Failed to migrate instance properties for - {}", id, e);
				failed.add(id.toString());
			}
		});
	}

	private void setType(ArchivedInstance instance) {
		Optional<InstanceType> type;
		if (instance.isValueNotNull(SEMANTIC_TYPE)) {
			type = instanceTypes.from(instance);
		} else {
			// if the version does not have semantic type,
			// pass the target id without the version to prevent stack overflow
			type = instanceTypes.from(instance.getTargetId());
		}

		if (type.isPresent()) {
			InstanceType instanceType = type.get();
			instance.setType(instanceType);
			instance.add(SEMANTIC_TYPE, instanceType.getId());
		} else {
			// this case shouldn't happen, but just to be sure that everything will run correct after that
			LOGGER.warn("Failed to retrieve instance type for {}. It will be removed from the properties"
					+ " in order to be resolve on instance load.", instance.getId());
			instance.remove(SEMANTIC_TYPE);
		}
	}

	@Override
	public String getConfirmationMessage() {
		return "Archived instance properties migration run successfully!";
	}

	@Override
	public void setFileOpener(ResourceAccessor resourceAccessor) {
		// unused
	}

	@Override
	public ValidationErrors validate(Database database) {
		return null;
	}
}