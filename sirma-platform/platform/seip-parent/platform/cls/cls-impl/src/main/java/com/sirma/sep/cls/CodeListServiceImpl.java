package com.sirma.sep.cls;

import com.sirma.sep.cls.db.CodeEntityDao;
import com.sirma.sep.cls.db.entity.CodeDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import com.sirma.sep.cls.model.Code;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link CodeListService} for retrieving {@link CodeListEntity} & {@link CodeValueEntity} and converting them into their
 * corresponding API classes {@link CodeList} & {@link CodeValue}.
 * <p>
 * It uses {@link CodeEntityDao} as its data access but does not provide means for modifying existing entities or creating new ones. For
 * that see {@link com.sirma.itt.emf.cls.service.CodeListManagementService}.
 *
 * @author Mihail Radkov
 */
@Singleton
public class CodeListServiceImpl implements CodeListService {

	private final CodeEntityDao dao;

	@Inject
	public CodeListServiceImpl(CodeEntityDao dao) {
		this.dao = dao;
	}

	@Override
	public List<CodeList> getCodeLists() {
		return getCodeLists(false);
	}

	@Override
	public List<CodeList> getCodeLists(boolean loadValues) {
		return dao.getCodeLists(loadValues).stream().map(listEntity -> {
			CodeList convertedList = toList(listEntity);
			List<CodeValue> convertedValues = toValues(listEntity.getValues());
			convertedList.setValues(convertedValues);
			return convertedList;
		}).collect(Collectors.toList());
	}

	@Override
	public List<CodeValue> getCodeValues(String codeListValue) {
		CodeListEntity listEntity = dao.getOrCreateCodeList(codeListValue, true);
		if (listEntity.exists()) {
			return toValues(listEntity.getValues());
		}
		return Collections.emptyList();
	}

	// DB -> API

	private static CodeList toList(CodeListEntity listEntity) {
		CodeList codeList = new CodeList();
		transferCommonFields(codeList, listEntity);
		transferListDescriptions(codeList, listEntity);
		return codeList;
	}

	private static List<CodeValue> toValues(List<CodeValueEntity> valueEntities) {
		return valueEntities.stream().map(CodeListServiceImpl::toValue).collect(Collectors.toList());
	}

	private static CodeValue toValue(CodeValueEntity valueEntity) {
		CodeValue codeValue = new CodeValue();
		transferCommonFields(codeValue, valueEntity);
		transferValueDescriptions(codeValue, valueEntity);
		codeValue.setCodeListValue(valueEntity.getCodeListId());
		codeValue.setActive(valueEntity.isActive());
		return codeValue;
	}

	private static void transferCommonFields(Code code, CodeEntity entity) {
		code.setValue(entity.getValue());
		code.setExtra1(entity.getExtra1());
		code.setExtra2(entity.getExtra2());
		code.setExtra3(entity.getExtra3());
	}

	private static void transferListDescriptions(Code code, CodeListEntity listEntity) {
		code.setDescriptions(listEntity.getDescriptions().stream().map(CodeListServiceImpl::toDescription).collect(Collectors.toList()));
	}

	private static void transferValueDescriptions(Code code, CodeValueEntity valueEntity) {
		code.setDescriptions(valueEntity.getDescriptions().stream().map(CodeListServiceImpl::toDescription).collect(Collectors.toList()));
	}

	private static CodeDescription toDescription(CodeDescriptionEntity descriptionEntity) {
		CodeDescription description = new CodeDescription();
		description.setLanguage(descriptionEntity.getLanguage());
		description.setName(descriptionEntity.getDescription());
		description.setComment(descriptionEntity.getComment());
		return description;
	}
}
