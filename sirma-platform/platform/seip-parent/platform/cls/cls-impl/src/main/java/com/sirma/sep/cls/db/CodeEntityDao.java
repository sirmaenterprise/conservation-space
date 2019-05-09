package com.sirma.sep.cls.db;

import com.sirma.itt.seip.Pair;
import com.sirma.sep.cls.db.entity.CodeEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides access and mapping between the API {@link CodeList} and {@link CodeValue} and DB entities {@link CodeListEntity} and {@link
 * CodeValueEntity}.
 *
 * @author Mihail Radkov
 */
@Singleton
public class CodeEntityDao {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String CL_VALUE_QUERY_KEY = "clValue";

	private final DbDao dbDao;

	/**
	 * Instantiates code entity DAO with the provided database DAO.
	 *
	 * @param dbDao
	 * 		abstract DAO providing access to the underlying database
	 */
	@Inject
	public CodeEntityDao(DbDao dbDao) {
		this.dbDao = dbDao;
	}

	/**
	 * Loads all available {@link CodeListEntity} from the database.
	 * <p>
	 * This will not load their {@link CodeValueEntity}. For that use {@link #getCodeLists(boolean)}
	 *
	 * @return the available code lists entities without their values
	 */
	public List<CodeListEntity> getCodeLists() {
		return getCodeLists(false);
	}

	/**
	 * Loads all available {@link CodeListEntity} from the database along with their owned {@link CodeValueEntity}.
	 *
	 * @return the available code lists entities with their values
	 */
	public List<CodeListEntity> getCodeLists(boolean loadValues) {
		Stream<CodeListEntity> entityStream = loadCodeListsInternal().stream();
		if (loadValues) {
			entityStream = entityStream.map(listEntity -> {
				loadCodeValues(listEntity);
				return listEntity;
			});
		}
		return entityStream.collect(Collectors.toList());
	}

	/**
	 * Saves or updates the provided {@link CodeListEntity} depending if it exists in the database. This is determined by the unique
	 * database identifier from {@link CodeListEntity#getId()}.
	 * <p>
	 * This will not update any {@link CodeValueEntity} in the provided list as they are transient.
	 *
	 * @param codeList
	 * 		the code list entity to save or update
	 * @throws IllegalArgumentException
	 * 		if the provided code list is null
	 */
	public void saveCodeList(CodeListEntity codeList) {
		requireNonNull(codeList, "Cannot persist null code list!");
		dbDao.saveOrUpdate(codeList);
	}

	/**
	 * Saves or updates the provided {@link CodeValueEntity} depending if it exists in the database. This is determined by the unique
	 * database identifier from {@link CodeValueEntity#getId()}.
	 *
	 * @param codeValue
	 * 		the code value entity to save or update
	 */
	public void saveCodeValue(CodeValueEntity codeValue) {
		requireNonNull(codeValue, "Cannot persist null code value!");
		dbDao.saveOrUpdate(codeValue);
	}

	/**
	 * Tries to load from the database a {@link CodeListEntity} that matches the provided value.
	 * <p>
	 * If no entity matches the value, a new one will be created that can be populated and persisted later with {@link
	 * #saveCodeList(CodeListEntity)}.
	 * <p>
	 * This will not load the list's {@link CodeValueEntity}. For that use  {@link #getOrCreateCodeList(String, boolean)}.
	 *
	 * @param value
	 * 		value matching {@link CodeList#getValue()}
	 * @return the existing list entity or brand new
	 */
	public CodeListEntity getOrCreateCodeList(String value) {
		return getOrCreateCodeList(value, false);
	}

	/**
	 * Tries to load from the database a {@link CodeListEntity} that matches the provided value along with any owned {@link
	 * CodeValueEntity}.
	 * <p>
	 * If no entity matches the value, a new one will be created that can be populated and persisted later with {@link
	 * #saveCodeList(CodeListEntity)}.
	 *
	 * @param value
	 * 		value matching {@link CodeList#getValue()}
	 * @return the existing list entity or brand new
	 */
	public CodeListEntity getOrCreateCodeList(String value, boolean loadValues) {
		List<CodeListEntity> loaded = loadCodeListInternal(value);
		if (loaded.isEmpty()) {
			return new CodeListEntity();
		}

		if (loaded.size() > 1) {
			LOGGER.warn("Found more than one code list entity with value={}, using the first one!", value);
		}

		CodeListEntity listEntity = loaded.get(0);

		if (loadValues) {
			loadCodeValues(listEntity);
		}

		return listEntity;
	}

	/**
	 * Tries to load from the database a {@link CodeValueEntity} that matches the provided value and its parent {@link CodeList} value.
	 * <p>
	 * If no entity matches the values, a new one will be created that can be populated and persisted later with {@link
	 * #saveCodeValue(CodeValueEntity)}.
	 *
	 * @param codeListValue
	 * 		- parent's value matching {@link CodeValue#getCodeListValue()}
	 * @param value
	 * 		value matching {@link CodeValue#getValue()}
	 * @return existing value entity or brand new
	 */
	public CodeValueEntity getOrCreateCodeValue(String codeListValue, String value) {
		List<CodeValueEntity> loadedValues = loadCodeValueInternal(codeListValue, value);
		if (loadedValues.isEmpty()) {
			return new CodeValueEntity();
		}

		if (loadedValues.size() > 1) {
			LOGGER.warn("Found more than one code list value entity with value={} and code list={}, using the first one !", value,
						codeListValue);
		}

		return loadedValues.get(0);
	}

	/**
	 * Truncates the database removing all {@link CodeListEntity} and {@link CodeValueEntity} along with their {@link
	 * com.sirma.sep.cls.db.entity.CodeDescriptionEntity}.
	 */
	public void truncateData() {
		getCodeLists(true).forEach(listEntity -> {
			listEntity.getValues().forEach(valueEntity -> dbDao.delete(CodeValueEntity.class, valueEntity.getId()));
			dbDao.delete(CodeListEntity.class, listEntity.getId());
		});
	}

	private void loadCodeValues(CodeListEntity listEntity) {
		listEntity.setValues(loadCodeValuesInternal(listEntity.getValue()));
	}

	private List<CodeListEntity> loadCodeListsInternal() {
		return dbDao.fetchWithNamed(CodeListEntity.QUERY_ALL_CODELISTS_KEY, Collections.emptyList());
	}

	private List<CodeListEntity> loadCodeListInternal(String value) {
		return dbDao.fetchWithNamed(CodeListEntity.QUERY_CODELIST_BY_VALUE_KEY,
									Collections.singletonList(new Pair<>(CL_VALUE_QUERY_KEY, value)));
	}

	private List<CodeValueEntity> loadCodeValuesInternal(String codeListValue) {
		return dbDao.fetchWithNamed(CodeValueEntity.QUERY_VALUES_BY_CL_ID_KEY,
									Collections.singletonList(new Pair<>(CL_VALUE_QUERY_KEY, codeListValue)));
	}

	private List<CodeValueEntity> loadCodeValueInternal(String codeListValue, String value) {
		return dbDao.fetchWithNamed(CodeValueEntity.QUERY_VALUE_BY_VALUE_AND_CL_ID_KEY,
									Arrays.asList(new Pair<>(CL_VALUE_QUERY_KEY, codeListValue), new Pair<>("cvValue", value)));
	}

	private static void requireNonNull(CodeEntity entity, String errorMessage) {
		if (entity == null) {
			throw new IllegalArgumentException(errorMessage);
		}
	}
}