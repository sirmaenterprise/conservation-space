package com.sirma.sep.cls.db;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.cls.db.entity.CodeEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static com.sirma.sep.cls.db.CodeEntityTestUtils.getListEntity;
import static com.sirma.sep.cls.db.CodeEntityTestUtils.getValueEntity;

/**
 * Tests the data access in {@link CodeEntityDao}.
 *
 * @author Mihail Radkov
 */
public class CodeEntityDaoTest {

	@Mock
	private DbDao dbDao;
	private DbDaoStub dbDaoStub;

	@InjectMocks
	private CodeEntityDao codeEntityDao;

	@Before
	public void beforeEach() {
		MockitoAnnotations.initMocks(this);
		// Helper for stubbing the mock
		dbDaoStub = new DbDaoStub(dbDao);
	}

	@Test
	public void shouldFetchAllCodeLists() {
		dbDaoStub.withListEntity("1");
		dbDaoStub.withListEntity("2");

		List<CodeListEntity> codeLists = codeEntityDao.getCodeLists();
		assertValues(codeLists, "1", "2");
	}

	@Test
	public void shouldFetchAllCodeListsWithTheirValues() {
		dbDaoStub.withListEntity("1");
		dbDaoStub.withValueEntityForList("1", "V1");
		dbDaoStub.withValueEntityForList("1", "V2");
		dbDaoStub.withListEntity("2");

		List<CodeListEntity> codeLists = codeEntityDao.getCodeLists(true);
		assertValues(codeLists, "1", "2");
		assertValues(codeLists.get(0).getValues(), "V1", "V2");
		assertValues(codeLists.get(1).getValues());
	}

	@Test
	public void shouldSaveCodeList() {
		CodeListEntity listEntity = getListEntity("1");
		codeEntityDao.saveCodeList(listEntity);
		Mockito.verify(dbDao).saveOrUpdate(Matchers.eq(listEntity));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotSaveNullCodeList() {
		codeEntityDao.saveCodeList(null);
	}

	@Test
	public void shouldSaveCodeValue() {
		CodeValueEntity valueEntity = getValueEntity("1", "V1");
		codeEntityDao.saveCodeValue(valueEntity);
		Mockito.verify(dbDao).saveOrUpdate(Matchers.eq(valueEntity));
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotSaveNullCodeValue() {
		codeEntityDao.saveCodeValue(null);
	}

	@Test
	public void shouldGetExistingCodeList() {
		CodeListEntity existing = dbDaoStub.withListEntity("1");
		CodeListEntity fetched = codeEntityDao.getOrCreateCodeList("1");
		Assert.assertTrue(fetched.exists());
		Assert.assertEquals(existing, fetched);
	}

	@Test
	public void shouldGetExistingCodeListWithValues() {
		CodeListEntity existing = dbDaoStub.withListEntity("1");
		CodeValueEntity valueEntity = dbDaoStub.withValueEntityForList("1", "V1");
		CodeListEntity fetched = codeEntityDao.getOrCreateCodeList("1", true);
		Assert.assertEquals(existing, fetched);
		Assert.assertEquals(valueEntity, fetched.getValues().get(0));
	}

	@Test
	public void shouldGetNewCodeList() {
		CodeListEntity fetched = codeEntityDao.getOrCreateCodeList("1");
		Assert.assertFalse(fetched.exists());
	}

	@Test
	public void shouldGetExistingCodeValue() {
		dbDaoStub.withListEntity("1");
		CodeValueEntity existing = dbDaoStub.withValueEntityForList("1", "V1");
		CodeValueEntity fetched = codeEntityDao.getOrCreateCodeValue("1", "V1");
		Assert.assertTrue(fetched.exists());
		Assert.assertEquals(existing, fetched);
	}

	@Test
	public void shouldGetNewCodeValue() {
		CodeValueEntity fetched = codeEntityDao.getOrCreateCodeValue("1", "V1");
		Assert.assertFalse(fetched.exists());
	}

	private static void assertValues(List<? extends CodeEntity> lists, String... values) {
		Assert.assertEquals(values.length, lists.size());
		for (int i = 0; i < values.length; i++) {
			Assert.assertEquals(values[i], lists.get(i).getValue());
		}
	}

}
