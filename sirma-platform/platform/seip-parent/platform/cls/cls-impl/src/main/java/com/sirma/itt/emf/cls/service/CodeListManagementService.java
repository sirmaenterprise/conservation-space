package com.sirma.itt.emf.cls.service;

import static com.sirma.itt.emf.cls.util.ClsUtils.formatCodeAttributesAndDescriptions;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.sep.cls.db.CodeEntityDao;
import com.sirma.sep.cls.db.entity.CodeDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeEntity;
import com.sirma.sep.cls.db.entity.CodeListDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import com.sirma.sep.cls.model.Code;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Encapsulates logic related to creating and updating {@link CodeList} and {@link CodeValue}.
 * <p>
 * For simply loading use {@link com.sirma.sep.cls.CodeListService}.
 *
 * @author Mihail Radkov
 */
@Singleton
public class CodeListManagementService {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private final CodeEntityDao dao;

	/**
	 * Instantiates the management service with the provided {@link CodeEntityDao}.
	 *
	 * @param dao
	 *            the DAO to use to access the underlying database
	 */
	@Inject
	public CodeListManagementService(CodeEntityDao dao) {
		this.dao = dao;
	}

	/**
	 * Saves or updates the provided code list.
	 * <p>
	 * If a code list with the same identifier exists then it's updated with the changes from the provided code list.
	 * Otherwise it will be treated as new code list.
	 * <p>
	 * This method will update existing code values if the code list exists. If not then they will be created along with
	 * the code list.
	 *
	 * @param codeList
	 *            - the code list to create or update
	 */
	public void saveCodeList(CodeList codeList) {
		CodeListEntity listEntity = dao.getOrCreateCodeList(codeList.getValue());
		if (listEntity.exists()) {
			updateExistingCodeList(listEntity, codeList);
		} else {
			saveNewCodeList(listEntity, codeList);
		}
	}

	/**
	 * Saves or updates the provided code value.
	 * <p>
	 * If a code value with the same identifier and same code list exists then it's updated with the changes from the
	 * provided code value. Otherwise it will be treated as new code value for the specified code list.
	 * <p>
	 *
	 * @param codeValue
	 *            - the code value to create or update
	 */
	public void saveCodeValue(CodeValue codeValue) {
		CodeValueEntity valueEntity = dao.getOrCreateCodeValue(codeValue.getCodeListValue(), codeValue.getValue());
		if (valueEntity.exists()) {
			updateExistingCodeValue(valueEntity, codeValue);
		} else {
			saveNewCodeValue(valueEntity, codeValue);
		}
	}

	private void updateExistingCodeList(CodeListEntity listEntity, CodeList updatedCodeList) {
		transferCodeListData(listEntity, updatedCodeList);

		dao.saveCodeList(listEntity);

		LOGGER.info("Code list [id={}] was updated.", listEntity.getValue());

		updatedCodeList.getValues().forEach(this::saveCodeValue);
	}

	private void saveNewCodeList(CodeListEntity listEntity, CodeList newCodeList) {
		transferCodeListData(listEntity, newCodeList);

		dao.saveCodeList(listEntity);

		LOGGER.info("Code list [id={}] was created.", listEntity.getValue());

		newCodeList.getValues().forEach(newValue -> saveNewCodeValue(new CodeValueEntity(), newValue));
	}

	private void updateExistingCodeValue(CodeValueEntity valueEntity, CodeValue updatedCodeValue) {
		transferCodeValueData(valueEntity, updatedCodeValue);

		dao.saveCodeValue(valueEntity);

		LOGGER.trace("Code value [id={}, codelist={}] was updated.", valueEntity.getValue(), valueEntity.getCodeListId());
	}

	private void saveNewCodeValue(CodeValueEntity valueEntity, CodeValue newCodeValue) {
		transferCodeValueData(valueEntity, newCodeValue);

		dao.saveCodeValue(valueEntity);

		LOGGER.trace("Code value [id={}, codelist={}] was created.", newCodeValue.getValue(), newCodeValue.getCodeListValue());
	}

	/**
	 * Deletes all available records of {@link CodeList}, {@link CodeValue} and {@link CodeDescription} from the
	 * database.
	 */
	public void deleteAvailableCodes() {
		dao.truncateData();
		LOGGER.debug("Deleted the available code lists, values and descriptions");
	}

	// API -> DB

	private static void transferCodeListData(CodeListEntity codeEntity, CodeList codeList) {
		formatCodeAttributesAndDescriptions(codeList);
		transferCodeAttributes(codeEntity, codeList);
		transferCodeListDescriptions(codeEntity, codeList);
	}

	private static void transferCodeValueData(CodeValueEntity valueEntity, CodeValue codeValue) {
		formatCodeAttributesAndDescriptions(codeValue);
		transferCodeAttributes(valueEntity, codeValue);
		transferCodeValueDescriptions(valueEntity, codeValue);

		valueEntity.setCodeListId(codeValue.getCodeListValue());
		valueEntity.setActive(getValue(codeValue.isActive(), valueEntity.isActive()));
	}

	private static void transferCodeAttributes(CodeEntity codeEntity, Code code) {
		codeEntity.setValue(code.getValue());
		codeEntity.setExtra1(code.getExtra1());
		codeEntity.setExtra2(code.getExtra2());
		codeEntity.setExtra3(code.getExtra3());
	}

	private static void transferCodeListDescriptions(CodeListEntity listEntity, CodeList updatedCodeList) {
		List<CodeListDescriptionEntity> finalDescriptions = listEntity.getDescriptions();
		updatedCodeList.getDescriptions().forEach(description -> {
			Optional<CodeListDescriptionEntity> found = finalDescriptions.stream()
					.filter(d -> d.getLanguage().equals(description.getLanguage()))
					.findFirst();
			if (found.isPresent()) {
				copyDescription(found.get(), description);
			} else {
				CodeListDescriptionEntity descriptionEntity = new CodeListDescriptionEntity();
				copyDescription(descriptionEntity, description);
				finalDescriptions.add(descriptionEntity);
			}
		});
	}

	private static void transferCodeValueDescriptions(CodeValueEntity valueEntity, CodeValue updatedCodeValue) {
		List<CodeValueDescriptionEntity> finalDescriptions = valueEntity.getDescriptions();
		updatedCodeValue.getDescriptions().forEach(description -> {
			Optional<CodeValueDescriptionEntity> found = finalDescriptions.stream()
					.filter(d -> d.getLanguage().equals(description.getLanguage()))
					.findFirst();
			if (found.isPresent()) {
				copyDescription(found.get(), description);
			} else {
				CodeValueDescriptionEntity descriptionEntity = new CodeValueDescriptionEntity();
				copyDescription(descriptionEntity, description);
				finalDescriptions.add(descriptionEntity);
			}
		});
	}

	private static void copyDescription(CodeDescriptionEntity target, CodeDescription source) {
		target.setComment(source.getComment());
		target.setDescription(source.getName());
		target.setLanguage(source.getLanguage());
	}

	private static <T> T getValue(T newValue, T oldValue) {
		return isValueValid(newValue) && !isValueEmpty(newValue) ? newValue : oldValue;
	}

	private static <T> boolean isValueValid(T value) {
		return value != null;
	}

	private static <T> boolean isValueEmpty(T value) {
		if (value instanceof String) {
			return ((String) value).isEmpty();
		}
		return false;
	}
}
