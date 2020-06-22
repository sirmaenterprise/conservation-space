package com.sirma.itt.emf.cls.service;

import static com.sirma.sep.cls.db.CodeEntityTestUtils.getList;
import static com.sirma.sep.cls.db.CodeEntityTestUtils.getValue;
import static com.sirma.sep.cls.db.CodeEntityTestUtils.getValueEntity;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.cls.db.CodeEntityDao;
import com.sirma.sep.cls.db.DbDaoStub;
import com.sirma.sep.cls.db.entity.CodeDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;

/**
 * Test the updating logic in {@link CodeListManagementService}.
 *
 * @author Mihail Radkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ CodeEntityDao.class })
public class CodeListManagementServiceTest {

	@Produces
	@Mock
	private DbDao dbDao;
	private DbDaoStub dbDaoStub;

	@Inject
	private CodeListManagementService service;

	@Before
	public void beforeEach() {
		dbDaoStub = new DbDaoStub(dbDao);
	}

	@Test
	public void updateExistingCodeList() {
		CodeListEntity existingEntity = dbDaoStub.withListEntity("1", "en");

		CodeList updated = getList("1", "en");
		// Update two of the extras + the EN description
		updated.setExtra1("Updated extra 1");
		updated.setExtra2("New extra 2 ");

		updated.getDescriptions().get(0).setName("New EN descr ");

		// Adding extra description + testing the trimming
		CodeDescription bgDescr = new CodeDescription();
		bgDescr.setLanguage("bg");
		bgDescr.setName(" Описание");
		updated.getDescriptions().add(bgDescr);

		service.saveCodeList(updated);

		// Should update the existing one and persist it
		verify(dbDao).saveOrUpdate(eq(existingEntity));

		// Extra 3 should not be updated
		assertExtras(existingEntity, "Updated extra 1", "New extra 2", "1_extra3");

		Assert.assertEquals(2, existingEntity.getDescriptions().size());
		assertDescription(existingEntity.getDescriptions().get(0), "lang_en", "New EN descr", "comment_en");
		assertDescription(existingEntity.getDescriptions().get(1), "bg", "Описание", null);
	}

	@Test
	public void preserveExistingCodeListDescriptions() {
		CodeListEntity existing = dbDaoStub.withListEntity("1", "en");

		// No descriptions
		CodeList updated = getList("1");
		updated.getDescriptions().clear();

		service.saveCodeList(updated);

		// Should update the existing one and persist it
		verify(dbDao).saveOrUpdate(eq(existing));

		Assert.assertEquals(1, existing.getDescriptions().size());
		assertDescription(existing.getDescriptions().get(0), "lang_en", "descr_en", "comment_en");
	}

	@Test
	public void updateValuesWhenUpdatingCodeList() {
		CodeListEntity existingListEntity = dbDaoStub.withListEntity("1");
		CodeValueEntity existingValue1 = dbDaoStub.withValueEntityForList("1", "V1");
		CodeValueEntity existingValue2 = dbDaoStub.withValueEntityForList("1", "V2");
		CodeValueEntity existingValue3 = dbDaoStub.withValueEntityForList("1", "V3");

		CodeList updated = getList("1");
		updated.setValues(Arrays.asList(getValue("1", "V1"), getValue("1", "V2")));

		service.saveCodeList(updated);

		verify(dbDao).saveOrUpdate(eq(existingListEntity));
		verify(dbDao).saveOrUpdate(eq(existingValue1));
		verify(dbDao).saveOrUpdate(eq(existingValue2));
		verify(dbDao, times(0)).saveOrUpdate(eq(existingValue3));
	}

	@Test
	public void updateExistingCodeValueWithIncomingChanges() {
		dbDaoStub.withListEntity("1");
		CodeValueEntity existingValue = dbDaoStub.withValueEntityForList("1", "V1", "en");

		CodeValue updatedValue = getValue("1", "V1", "en");
		updatedValue.setExtra1("New extra 1");
		updatedValue.setExtra3("");
		updatedValue.setActive(null);
		updatedValue.getDescriptions().get(0).setComment("New comment");

		CodeList updatedList = getList("1");
		updatedList.setValues(Collections.singletonList(updatedValue));

		service.saveCodeList(updatedList);

		verify(dbDao).saveOrUpdate(eq(existingValue));

		assertExtras(existingValue, "New extra 1", "V1_extra2", null);

		Assert.assertTrue(existingValue.isActive());
		Assert.assertEquals(1, existingValue.getDescriptions().size());
		assertDescription(existingValue.getDescriptions().get(0), "lang_en", "descr_en", "New comment");
	}

	@Test
	public void preserveValuesDescriptionsWhenUpdatingCodeList() {
		dbDaoStub.withListEntity("1");

		CodeValueEntity existingValue1 = getValueEntity("1", "V1", "en");
		existingValue1.getDescriptions().get(0).setComment("Preserve comment");
		dbDaoStub.withValueEntityForList(existingValue1);

		CodeList updated = getList("1");
		CodeValue updatedValue1 = getValue("1", "V1");
		// Clearing all descriptions for the value to see if they are preserved from the existing entity
		updatedValue1.getDescriptions().clear();
		updated.setValues(Collections.singletonList(updatedValue1));

		service.saveCodeList(updated);

		assertDescription(existingValue1.getDescriptions().get(0), "lang_en", "descr_en", "Preserve comment");
	}

	@Test
	public void saveNewCodeList() {
		CodeList newCodeList = getList("1");

		service.saveCodeList(newCodeList);

		ArgumentCaptor<CodeEntity> savedEntities = ArgumentCaptor.forClass(CodeEntity.class);

		// Should save the provided code list
		verify(dbDao).saveOrUpdate(savedEntities.capture());

		CodeListEntity savedListEntity = (CodeListEntity) savedEntities.getValue();
		Assert.assertEquals(newCodeList.getValue(), savedListEntity.getValue());
	}

	@Test
	public void saveNewCodeListWithValues() {
		CodeList newCodeList = getList("1");
		CodeValue value1 = getValue("1", "V1");
		CodeValue value2 = getValue("1", "V2");
		newCodeList.setValues(Arrays.asList(value1, value2));

		service.saveCodeList(newCodeList);

		ArgumentCaptor<CodeEntity> savedEntities = ArgumentCaptor.forClass(CodeEntity.class);
		// Should save the provided code list and its values
		verify(dbDao, times(3)).saveOrUpdate(savedEntities.capture());

		Assert.assertEquals(newCodeList.getValue(), savedEntities.getAllValues().get(0).getValue());
		Assert.assertEquals(value1.getValue(), savedEntities.getAllValues().get(1).getValue());
		Assert.assertEquals(value2.getValue(), savedEntities.getAllValues().get(2).getValue());
	}

	@Test
	public void saveNewCodeValueInExistingList() {
		dbDaoStub.withListEntity("1");

		CodeList updatedList = getList("1");
		CodeValue newValue = getValue("1", "V1");
		updatedList.setValues(Collections.singletonList(newValue));

		service.saveCodeList(updatedList);

		// Should save the provided code value
		ArgumentCaptor<CodeEntity> savedEntities = ArgumentCaptor.forClass(CodeEntity.class);
		verify(dbDao, times(2)).saveOrUpdate(savedEntities.capture());

		// Second one is the new value
		CodeValueEntity savedValueEntity = (CodeValueEntity) savedEntities.getAllValues().get(1);
		Assert.assertEquals(newValue.getValue(), savedValueEntity.getValue());
	}

	@Test
	public void shouldDeactivateExistingCodeValue() {
		dbDaoStub.withListEntity("1");
		CodeValueEntity existingValue = getValueEntity("1", "V1");
		dbDaoStub.withValueEntityForList(existingValue);

		CodeList updatedList = getList("1");
		CodeValue updatedValue = getValue("1", "V1");
		updatedValue.setActive(false);
		updatedList.setValues(Collections.singletonList(updatedValue));

		service.saveCodeList(updatedList);

		// Should have updated the value
		Assert.assertFalse(existingValue.isActive());
	}

	private static void assertExtras(CodeEntity entity, String extra1, String extra2, String extra3) {
		Assert.assertEquals(extra1, entity.getExtra1());
		Assert.assertEquals(extra2, entity.getExtra2());
		Assert.assertEquals(extra3, entity.getExtra3());
	}

	private static void assertDescription(CodeDescriptionEntity descriptionEntity, String language, String description, String comment) {
		Assert.assertEquals(language, descriptionEntity.getLanguage());
		Assert.assertEquals(description, descriptionEntity.getDescription());
		Assert.assertEquals(comment, descriptionEntity.getComment());
	}
}
