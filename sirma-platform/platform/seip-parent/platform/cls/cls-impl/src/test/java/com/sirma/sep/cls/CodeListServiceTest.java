package com.sirma.sep.cls;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.cls.db.CodeEntityDao;
import com.sirma.sep.cls.db.DbDaoStub;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import com.sirma.sep.cls.model.Code;
import com.sirma.sep.cls.model.CodeDescription;
import com.sirma.sep.cls.model.CodeList;
import com.sirma.sep.cls.model.CodeValue;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;

import static com.sirma.sep.cls.db.CodeEntityTestUtils.getListEntity;
import static com.sirma.sep.cls.db.CodeEntityTestUtils.getValueEntity;

/**
 * Tests {@link CodeListEntity} & {@link com.sirma.sep.cls.db.entity.CodeValueEntity} retrieveal from stubbed database and their
 * transformation into the API {@link CodeList} & {@link CodeValue}.
 *
 * @author Mihail Radkov
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ CodeListServiceImpl.class, CodeEntityDao.class })
public class CodeListServiceTest {

	@Produces
	@Mock
	private DbDao dbDao;

	@Inject
	private CodeListService codeListService;

	@Before
	public void beforeEach() {
		DbDaoStub dbDaoStub = new DbDaoStub(dbDao);

		CodeListEntity listEntity = getListEntity("1", "EN", "BG");
		CodeListEntity listEntity2 = getListEntity("2", "FR");
		CodeListEntity listEntity3 = getListEntity("3");
		dbDaoStub.withListEntity(listEntity, listEntity2, listEntity3);

		CodeValueEntity valueEntity = getValueEntity("1", "V1-1", "EN", "BG");
		CodeValueEntity valueEntity2 = getValueEntity("1", "V1-2", "BG");
		CodeValueEntity valueEntity3 = getValueEntity("2", "V2-1", "FR");
		valueEntity3.setActive(false);
		dbDaoStub.withValueEntityForList(valueEntity, valueEntity2, valueEntity3);
	}

	@Test
	public void shouldFetchAllCodeListsWithoutValues() {
		List<CodeList> codeLists = codeListService.getCodeLists();
		Assert.assertEquals(3, codeLists.size());

		CodeList codeList = codeLists.get(0);
		Assert.assertEquals("1", codeList.getValue());
		assertExtras(codeList, "1_extra1", "1_extra2", "1_extra3");
		Assert.assertEquals(2, codeList.getDescriptions().size());
		assertDescription(codeList.getDescriptions().get(0), "lang_EN", "descr_EN", "comment_EN");
		assertDescription(codeList.getDescriptions().get(1), "lang_BG", "descr_BG", "comment_BG");
		Assert.assertTrue(codeList.getValues().isEmpty());

		CodeList codeList2 = codeLists.get(1);
		Assert.assertEquals("2", codeList2.getValue());
		assertExtras(codeList2, "2_extra1", "2_extra2", "2_extra3");
		Assert.assertEquals(1, codeList2.getDescriptions().size());
		assertDescription(codeList2.getDescriptions().get(0), "lang_FR", "descr_FR", "comment_FR");
		Assert.assertTrue(codeList2.getValues().isEmpty());

		CodeList codeList3 = codeLists.get(2);
		Assert.assertEquals("3", codeList3.getValue());
		assertExtras(codeList3, "3_extra1", "3_extra2", "3_extra3");
		Assert.assertTrue(codeList3.getDescriptions().isEmpty());
		Assert.assertTrue(codeList3.getValues().isEmpty());
	}

	@Test
	public void shouldFetchAllCodeListsWithTheirValues() {
		List<CodeList> codeLists = codeListService.getCodeLists(true);
		Assert.assertEquals(3, codeLists.size());

		CodeList codeList1 = codeLists.get(0);
		Assert.assertEquals("1", codeList1.getValue());
		Assert.assertEquals(2, codeList1.getValues().size());

		CodeValue codeList1CodeValue1 = codeList1.getValues().get(0);
		Assert.assertEquals("V1-1", codeList1CodeValue1.getValue());
		assertExtras(codeList1CodeValue1, "V1-1_extra1", "V1-1_extra2", "V1-1_extra3");
		Assert.assertEquals(2, codeList1CodeValue1.getDescriptions().size());
		assertDescription(codeList1CodeValue1.getDescriptions().get(0), "lang_EN", "descr_EN", "comment_EN");
		assertDescription(codeList1CodeValue1.getDescriptions().get(1), "lang_BG", "descr_BG", "comment_BG");
		Assert.assertEquals("1", codeList1CodeValue1.getCodeListValue());
		Assert.assertTrue(codeList1CodeValue1.isActive());

		CodeValue codeList1CodeValue2 = codeList1.getValues().get(1);
		Assert.assertEquals("V1-2", codeList1CodeValue2.getValue());
		assertExtras(codeList1CodeValue2, "V1-2_extra1", "V1-2_extra2", "V1-2_extra3");
		Assert.assertEquals(1, codeList1CodeValue2.getDescriptions().size());
		assertDescription(codeList1CodeValue2.getDescriptions().get(0), "lang_BG", "descr_BG", "comment_BG");
		Assert.assertEquals("1", codeList1CodeValue2.getCodeListValue());
		Assert.assertTrue(codeList1CodeValue2.isActive());

		CodeList codeList2 = codeLists.get(1);
		Assert.assertEquals("2", codeList2.getValue());
		Assert.assertEquals(1, codeList2.getValues().size());

		CodeValue codeList2CodeValue1 = codeList2.getValues().get(0);
		Assert.assertEquals("V2-1", codeList2CodeValue1.getValue());
		// Skipping other checks, it should be properly transformed
		Assert.assertEquals("2", codeList2CodeValue1.getCodeListValue());
		Assert.assertFalse(codeList2CodeValue1.isActive());

		CodeList codeList3 = codeLists.get(2);
		Assert.assertTrue(codeList3.getValues().isEmpty());
	}

	@Test
	public void shouldFetchAllValuesForSpecificList() {
		List<CodeValue> codeList1Values = codeListService.getCodeValues("1");
		Assert.assertEquals(2, codeList1Values.size());
		Assert.assertEquals("V1-1", codeList1Values.get(0).getValue());
		Assert.assertEquals("V1-2", codeList1Values.get(1).getValue());

		List<CodeValue> codeList2Values = codeListService.getCodeValues("2");
		Assert.assertEquals(1, codeList2Values.size());
		Assert.assertEquals("V2-1", codeList2Values.get(0).getValue());

		List<CodeValue> codeList3Values = codeListService.getCodeValues("3");
		Assert.assertTrue(codeList3Values.isEmpty());

		// Missing cl
		List<CodeValue> codeList4Values = codeListService.getCodeValues("4");
		Assert.assertTrue(codeList4Values.isEmpty());
	}

	private static void assertExtras(Code code, String extra1, String extra2, String extra3) {
		Assert.assertEquals(extra1, code.getExtra1());
		Assert.assertEquals(extra2, code.getExtra2());
		Assert.assertEquals(extra3, code.getExtra3());
	}

	private static void assertDescription(CodeDescription codeDescription, String language, String name, String comment) {
		Assert.assertEquals(language, codeDescription.getLanguage());
		Assert.assertEquals(name, codeDescription.getName());
		Assert.assertEquals(comment, codeDescription.getComment());
	}
}
