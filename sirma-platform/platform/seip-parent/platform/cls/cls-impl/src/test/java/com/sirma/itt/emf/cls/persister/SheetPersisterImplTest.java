package com.sirma.itt.emf.cls.persister;

import static com.sirma.sep.cls.db.CodeEntityTestUtils.getListEntity;
import static com.sirma.sep.cls.db.CodeEntityTestUtils.getValueEntity;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
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
 * Tests for the {@link SheetPersisterImpl}.
 *
 * @author nvelkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ CodeListManagementService.class, CodeEntityDao.class })
@AdditionalClasspaths({ EventService.class })
public class SheetPersisterImplTest {

	@Produces
	@Mock
	private DbDao dbDao;
	private DbDaoStub dbDaoStub;

	@Produces
	private TransactionSupport support = new TransactionSupportFake();

	@Inject
	private SheetPersisterImpl processor;

	@Before
	public void init() {
		dbDaoStub = new DbDaoStub(dbDao);
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

		processor.persist(sheet);

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
	public void shouldTruncateDatabaseBeforeProcessing() {
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

		processor.persist(sheet);

		verify(dbDao).delete(Matchers.eq(CodeListEntity.class), Matchers.eq(listEntity.getId()));
		verify(dbDao).delete(Matchers.eq(CodeListEntity.class), Matchers.eq(listEntity2.getId()));
		verify(dbDao).delete(Matchers.eq(CodeListEntity.class), Matchers.eq(listEntity3.getId()));

		verify(dbDao).delete(Matchers.eq(CodeValueEntity.class), Matchers.eq(valueEntity.getId()));
		verify(dbDao).delete(Matchers.eq(CodeValueEntity.class), Matchers.eq(valueEntity2.getId()));
		verify(dbDao).delete(Matchers.eq(CodeValueEntity.class), Matchers.eq(valueEntity3.getId()));
	}

	private CodeListTruncateEvent truncateEvent = null;

	void onTruncate(@Observes CodeListTruncateEvent event) {
		truncateEvent = event;
	}

	private void verifyTruncateEvent() {
		assertNotNull(truncateEvent);
	}

	private CodeListUploadEvent uploadEvent = null;

	void onTruncate(@Observes CodeListUploadEvent event) {
		uploadEvent = event;
	}

	private void verifyFiredUploadEvent() {
		assertNotNull(uploadEvent);
	}

	private static CodeDescription buildDescription(String lang, String name, String comment) {
		CodeDescription description = new CodeDescription();
		description.setLanguage(lang);
		description.setName(name);
		description.setComment(comment);
		return description;
	}
}
