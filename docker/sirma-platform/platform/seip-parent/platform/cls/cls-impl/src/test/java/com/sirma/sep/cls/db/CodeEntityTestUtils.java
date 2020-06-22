package com.sirma.sep.cls.db;

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

import java.util.Arrays;
import java.util.Random;

/**
 * Utilities for building {@link Code} and {@link CodeEntity} with predefined test data.
 * <p>
 * Both representations will contain the same extras and descriptions built based on the provided value and language keys.
 *
 * @author Mihail Radkov
 */
public class CodeEntityTestUtils {

	public static CodeListEntity getListEntity(String value, String... descriptionKeys) {
		CodeListEntity entity = new CodeListEntity();
		populateCodeData(entity, value);

		for (String descriptionKey : descriptionKeys) {
			CodeListDescriptionEntity descriptionEntity = new CodeListDescriptionEntity();
			populateDescriptionData(descriptionEntity, descriptionKey);
			entity.getDescriptions().add(descriptionEntity);
		}

		return entity;
	}

	public static CodeValueEntity getValueEntity(String listValue, String value, String... descriptionKeys) {
		CodeValueEntity entity = new CodeValueEntity();
		populateCodeData(entity, value);

		for (String descriptionKey : descriptionKeys) {
			CodeValueDescriptionEntity descriptionEntity = new CodeValueDescriptionEntity();
			populateDescriptionData(descriptionEntity, descriptionKey);
			entity.getDescriptions().add(descriptionEntity);
		}

		entity.setCodeListId(listValue);
		entity.setValue(value);
		entity.setActive(true);

		return entity;
	}

	private static void populateCodeData(CodeEntity code, String testData) {
		code.setId(new Random().nextLong());
		code.setValue(testData);
		code.setExtra1(testData + "_extra1");
		code.setExtra2(testData + "_extra2");
		code.setExtra3(testData + "_extra3");
	}

	private static void populateDescriptionData(CodeDescriptionEntity descriptionEntity, String descriptionKey) {
		descriptionEntity.setId(new Random().nextLong());
		descriptionEntity.setLanguage("lang_" + descriptionKey);
		descriptionEntity.setDescription("descr_" + descriptionKey);
		descriptionEntity.setComment("comment_" + descriptionKey);
	}

	public static CodeList getList(String value, String... descriptions) {
		CodeList codeList = new CodeList();

		populateCodeData(codeList, value);
		buildCodeDescriptions(codeList, descriptions);

		return codeList;
	}

	public static CodeValue getValue(String listValue, String value, String... descriptionKeys) {
		CodeValue codeValue = new CodeValue();

		populateCodeData(codeValue, value);
		buildCodeDescriptions(codeValue, descriptionKeys);

		codeValue.setCodeListValue(listValue);
		codeValue.setActive(true);

		return codeValue;
	}

	private static void populateCodeData(Code code, String testData) {
		code.setValue(testData);
		code.setExtra1(testData + "_extra1");
		code.setExtra2(testData + "_extra2");
		code.setExtra3(testData + "_extra3");
	}

	private static void buildCodeDescriptions(Code code, String... descriptionKeys) {
		Arrays.stream(descriptionKeys).forEach(descriptionKey -> {
			CodeDescription codeDescription = new CodeDescription();
			codeDescription.setLanguage("lang_" + descriptionKey);
			codeDescription.setName("descr_" + descriptionKey);
			codeDescription.setComment("comment_" + descriptionKey);
			code.getDescriptions().add(codeDescription);
		});
	}
}
