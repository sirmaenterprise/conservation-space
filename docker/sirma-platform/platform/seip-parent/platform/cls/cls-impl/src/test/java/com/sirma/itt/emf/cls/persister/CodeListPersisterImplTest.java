package com.sirma.itt.emf.cls.persister;

import static com.sirma.sep.cls.db.CodeEntityTestUtils.getListEntity;
import static com.sirma.sep.cls.db.CodeEntityTestUtils.getValueEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.AdditionalClasspaths;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;

import com.sirma.itt.emf.cls.event.CodeListTruncateEvent;
import com.sirma.itt.emf.cls.event.CodeListUploadEvent;
import com.sirma.itt.emf.cls.service.CodeListManagementService;
import com.sirma.itt.emf.cls.validator.CodeValidatorImpl;
import com.sirma.itt.emf.cls.validator.exception.CodeValidatorException;
import com.sirma.itt.seip.db.DbDao;
import com.sirma.itt.seip.event.EventService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.sep.cls.db.CodeEntityDao;
import com.sirma.sep.cls.db.DbDaoStub;
import com.sirma.sep.cls.db.entity.CodeEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import com.sirma.sep.cls.parser.CodeListSheet;

/**
 * Tests for the {@link CodeListPersisterImpl}.
 *
 * @author nvelkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ CodeListManagementService.class, CodeEntityDao.class, CodeValidatorImpl.class })
@AdditionalClasspaths({ EventService.class })
public class CodeListPersisterImplTest {

	@Produces
	@Mock
	private DbDao dbDao;
	private DbDaoStub dbDaoStub;

	@Produces
	private TransactionSupport support = new TransactionSupportFake();

	@Inject
	private CodeListPersisterImpl persister;

	@Before
	public void init() {
		dbDaoStub = new DbDaoStub(dbDao);
	}

	@Test
	public void shouldOverrideDbWithCodeListsSheet() {
		CodeList codelist1 = new CodeList();
		codelist1.setValue("CL1");

		CodeList codelist2 = new CodeList();
		codelist2.setValue("CL2");

		CodeListSheet sheet = new CodeListSheet();
		sheet.addCodeList(codelist1);
		sheet.addCodeList(codelist2);

		persister.override(sheet);

		verifyTruncateEvent();
		verifyFiredUploadEvent();

		List<CodeEntity> savedEntities = getSavedEntities(2);
		assertEquals(2, savedEntities.size());
		assertEquals("CL1", savedEntities.get(0).getValue());
		assertEquals("CL2", savedEntities.get(1).getValue());
	}

	@Test(expected = CodeValidatorException.class)
	public void shouldNotPersistInvalidCodeListSheet() {
		CodeListSheet sheet = getInvalidCodeListSheet();
		persister.override(sheet);
	}

	@Test
	public void should_SupportDescriptionsInMultipleLanguages() {
		CodeListSheet sheet = new CodeListSheet();

		CodeList codelist = new CodeList();
		codelist.setValue("CL1");

		List<CodeDescription> clDescriptions = new ArrayList<>();
		clDescriptions.add(buildDescription("DE", "Welt", "Kommentar"));
		clDescriptions.add(buildDescription("EN", "World", "Comment"));
		codelist.setDescriptions(clDescriptions);

		CodeValue codeValue = new CodeValue();
		// Tests trimming
		codeValue.setValue("V1 ");
		codeValue.setExtra1("E1 ");
		codeValue.setExtra2("   E2 ");
		codeValue.setExtra3(" E3 ");

		List<CodeDescription> cvDescriptions = new ArrayList<>();
		// Tests trimming
		cvDescriptions.add(buildDescription("DE", "Welt ", " Kommentar"));
		cvDescriptions.add(buildDescription("EN", "World ", "Comment"));
		codeValue.setDescriptions(cvDescriptions);

		codelist.setValues(Collections.singletonList(codeValue));

		sheet.addCodeList(codelist);

		persister.override(sheet);

		verifyTruncateEvent();
		verifyFiredUploadEvent();

		ArgumentCaptor<CodeEntity> argument = ArgumentCaptor.forClass(CodeEntity.class);
		verify(dbDao, times(2)).saveOrUpdate(argument.capture());

		List<CodeEntity> savedEntities = argument.getAllValues();
		// List + value
		assertEquals(2, savedEntities.size());

		// First should be the code list
		CodeListEntity codeListEntity = (CodeListEntity) savedEntities.get(0);
		assertEquals("CL1", codeListEntity.getValue());

		// And second the value
		CodeValueEntity codeValueEntity = (CodeValueEntity) savedEntities.get(1);

		assertEquals("V1", codeValueEntity.getValue());
		assertEquals("E1", codeValueEntity.getExtra1());
		assertEquals("E2", codeValueEntity.getExtra2());
		assertEquals("E3", codeValueEntity.getExtra3());
		assertEquals("Welt", codeValueEntity.getDescriptions().get(0).getDescription());
		assertEquals("World", codeValueEntity.getDescriptions().get(1).getDescription());
	}

	@Test
	public void shouldTruncateDatabaseBeforeOverriding() {
		CodeListEntity listEntity = getListEntity("1");
		CodeListEntity listEntity2 = getListEntity("2");
		CodeListEntity listEntity3 = getListEntity("3");
		dbDaoStub.withListEntity(listEntity, listEntity2, listEntity3);

		CodeValueEntity valueEntity = getValueEntity("1", "V1-1");
		CodeValueEntity valueEntity2 = getValueEntity("1", "V1-2");
		CodeValueEntity valueEntity3 = getValueEntity("2", "V2-1");
		dbDaoStub.withValueEntityForList(valueEntity, valueEntity2, valueEntity3);

		CodeList codelist = new CodeList();
		codelist.setValue("CL1");
		CodeListSheet sheet = new CodeListSheet();
		sheet.addCodeList(codelist);

		persister.override(sheet);

		verify(dbDao).delete(Matchers.eq(CodeListEntity.class), Matchers.eq(listEntity.getId()));
		verify(dbDao).delete(Matchers.eq(CodeListEntity.class), Matchers.eq(listEntity2.getId()));
		verify(dbDao).delete(Matchers.eq(CodeListEntity.class), Matchers.eq(listEntity3.getId()));

		verify(dbDao).delete(Matchers.eq(CodeValueEntity.class), Matchers.eq(valueEntity.getId()));
		verify(dbDao).delete(Matchers.eq(CodeValueEntity.class), Matchers.eq(valueEntity2.getId()));
		verify(dbDao).delete(Matchers.eq(CodeValueEntity.class), Matchers.eq(valueEntity3.getId()));
	}

	@Test
	public void shouldPersistSingleCodeList() {
		CodeList codelist = new CodeList();
		codelist.setValue("CL1");

		persister.persist(codelist);

		verifyNoTruncation();
		verifyNoUploadEvent();

		List<CodeEntity> savedEntities = getSavedEntities(1);
		assertEquals(1, savedEntities.size());
		assertEquals("CL1", savedEntities.get(0).getValue());
	}

	@Test(expected = CodeValidatorException.class)
	public void shouldNotPersistInvalidCodeList() {
		CodeList codelist = new CodeList();
		persister.persist(codelist);
	}

	@Test
	public void shouldPersistMultipleCodeLists() {
		CodeList codelist1 = new CodeList();
		codelist1.setValue("CL1");

		CodeList codelist2 = new CodeList();
		codelist2.setValue("CL2");

		persister.persist(Arrays.asList(codelist1, codelist2));

		verifyNoTruncation();
		verifyNoUploadEvent();

		List<CodeEntity> savedEntities = getSavedEntities(2);
		assertEquals(2, savedEntities.size());
	}

	@Test
	public void shouldPersistSingleCodeValue() {
		CodeValue codeValue = new CodeValue();
		codeValue.setValue("single-code-value");
		persister.persist(codeValue);

		verifyNoTruncation();
		verifyNoUploadEvent();

		List<CodeEntity> savedEntities = getSavedEntities(1);
		assertEquals(1, savedEntities.size());
		assertEquals("single-code-value", savedEntities.get(0).getValue());
	}

	@Test(expected = CodeValidatorException.class)
	public void shouldNotPersistInvalidCodeValue() {
		CodeValue valueWithoutValue = new CodeValue();
		persister.persist(valueWithoutValue);
	}

	private List<CodeEntity> getSavedEntities(int expected) {
		ArgumentCaptor<CodeEntity> argument = ArgumentCaptor.forClass(CodeEntity.class);
		verify(dbDao, times(expected)).saveOrUpdate(argument.capture());
		return argument.getAllValues();
	}

	private CodeListTruncateEvent truncateEvent = null;

	void onTruncate(@Observes CodeListTruncateEvent event) {
		truncateEvent = event;
	}

	private void verifyTruncateEvent() {
		assertNotNull(truncateEvent);
	}

	private void verifyNoTruncation() {
		assertNull(truncateEvent);
		verify(dbDao, never()).delete(Matchers.any(), Matchers.anyString());
	}

	private CodeListUploadEvent uploadEvent = null;

	void onTruncate(@Observes CodeListUploadEvent event) {
		uploadEvent = event;
	}

	private void verifyFiredUploadEvent() {
		assertNotNull(uploadEvent);
	}

	private void verifyNoUploadEvent() {
		assertNull(uploadEvent);
	}

	private static CodeDescription buildDescription(String lang, String name, String comment) {
		CodeDescription description = new CodeDescription();
		description.setLanguage(lang);
		description.setName(name);
		description.setComment(comment);
		return description;
	}

	private static CodeListSheet getInvalidCodeListSheet() {
		CodeList codelist1 = new CodeList();
		codelist1.setValue("CL1");

		// Another code list with duplicated value
		CodeList codelist2 = new CodeList();
		codelist2.setValue("CL1");

		CodeListSheet sheet = new CodeListSheet();
		sheet.addCodeList(codelist1);
		sheet.addCodeList(codelist2);

		return sheet;
	}
}
