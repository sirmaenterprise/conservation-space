package com.sirma.itt.seip.domain.codelist;

import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.MemoryCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCache;
import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAOAdaptor;
import com.sirma.itt.seip.domain.codelist.adapter.CodelistAdapter;
import com.sirma.itt.seip.domain.codelist.event.CodelistFiltered;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import com.sirma.itt.seip.domain.filter.FilterService;

/**
 * Tests {@link CodelistServiceImpl} class.
 *
 * @author smustafov
 */
@Test
public class CodelistServiceImplTest {

	@Mock
	private EntityLookupCacheContext cacheContext;

	@Mock
	private Instance<CodelistAdapter> codelistAdapter;

	@Mock
	private Event<CodelistFiltered> clFilterEvent;

	@Mock
	private FilterService filterService;

	@InjectMocks
	private CodelistServiceImpl codelistService;

	private EntityLookupCache<Serializable, Object, Serializable> lookupCache = new EntityLookupCache<>(
			new MemoryCache<>(), new EntityLookupCallbackDAOAdaptor<Serializable, Object, Serializable>() {

				@Override
				public Pair<Serializable, Object> findByKey(Serializable key) {
					return null;
				}

				@Override
				public Pair<Serializable, Object> createValue(Object value) {
					return null;
				}
			});

	/**
	 * Before method.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
		when(cacheContext.getCache(CodelistServiceImpl.CODELIST_CACHE)).thenReturn(lookupCache);
		prepareData();
	}

	/**
	 * Prepare data.
	 */
	private void prepareData() {
		CodeValue codeValue1 = new CodeValue();
		codeValue1.setCodelist(1);
		codeValue1.setValue("value1");
		codeValue1.setProperties(new HashMap<String, Serializable>());
		codeValue1.getProperties().put("extra1", "val1,val2");
		codeValue1.getProperties().put("descBg", "bgDesc");
		codeValue1.getProperties().put("descEn", "enDesc");

		CodeValue codeValue2 = new CodeValue();
		codeValue2.setCodelist(2);
		codeValue2.setValue("value2");
		codeValue2.setProperties(new HashMap<String, Serializable>());
		codeValue2.getProperties().put("extra1", "someValue");
		codeValue2.getProperties().put("extra2", "val1,val2");

		CodeValue codeValue3 = new CodeValue();
		codeValue3.setCodelist(3);
		codeValue3.setValue("value3");
		codeValue3.setProperties(new HashMap<String, Serializable>());
		codeValue3.getProperties().put("extra1", "val1,val2,val3,val4,val5,val6");
		codeValue3.getProperties().put("extra2", "val1");

		Map<String, CodeValue> map = new HashMap<>();
		map.put("1", codeValue1);
		map.put("2", codeValue2);
		map.put("3", codeValue3);

		lookupCache.setValue(1, map);
	}

	/**
	 * Tests {@link CodelistServiceImpl#filterCodeValues(Integer, boolean, String, String...)} with filter value that
	 * does not exist in the codelist field.
	 */
	@Test
	public void testFilterCodeValuesWithNotExistingValue() {
		Map<String, CodeValue> codeValues = codelistService.filterCodeValues(1, false, "extra1", "notExistingValue");

		assertEquals(0, codeValues.size());
	}

	/**
	 * Tests {@link CodelistServiceImpl#filterCodeValues(Integer, boolean, String, String...)} with no filter values.
	 */
	@Test
	public void testFilterCodeValuesWitheEmptyFilterValue() {
		Map<String, CodeValue> codeValues = codelistService.filterCodeValues(1, false, "extra1", new String[] {});

		assertEquals(3, codeValues.size());
	}

	/**
	 * Tests {@link CodelistServiceImpl#filterCodeValues(Integer, boolean, String, String...)} with one filter value
	 * that exist in the codelist field with exclusive filtering.
	 */
	@Test
	public void testFilterCodeValuesWithOneFilterExclusive() {
		Map<String, CodeValue> codeValues = codelistService.filterCodeValues(1, false, "extra1", "someValue");

		assertEquals(1, codeValues.size());
		assertTrue(codeValues.containsKey("2"));
	}

	/**
	 * Tests {@link CodelistServiceImpl#filterCodeValues(Integer, boolean, String, String...)} with two filter values
	 * that exist in the codelist field with inclusive filtering.
	 */
	@Test
	public void testFilterCodeValuesWithTwoFiltersInclusiveExtra2Field() {
		Map<String, CodeValue> codeValues = codelistService.filterCodeValues(1, true, "extra2", "val1", "val2");

		assertEquals(1, codeValues.size());
		assertTrue(codeValues.containsKey("2"));
	}

	/**
	 * Tests {@link CodelistServiceImpl#filterCodeValues(Integer, boolean, String, String...)} with one filter value
	 * that exist in the codelist field with exclusive filtering.
	 */
	@Test
	public void testFilterCodeValuesWithTwoFiltersExclusive() {
		Map<String, CodeValue> codeValues = codelistService.filterCodeValues(1, false, "extra2", "val1", "val2");

		assertEquals(2, codeValues.size());
		assertTrue(codeValues.containsKey("2"));
		assertTrue(codeValues.containsKey("3"));
	}

	/**
	 * Tests {@link CodelistServiceImpl#filterCodeValues(Integer, boolean, String, String...)} with two filter values
	 * that exist in the codelist field with inclusive filtering on field extra1.
	 */
	@Test
	public void testFilterCodeValuesWithTwoFilterInclusiveExtra1Field() {
		Map<String, CodeValue> codeValues = codelistService.filterCodeValues(1, true, "extra1", "val1", "val2");

		assertEquals(2, codeValues.size());
		assertTrue(codeValues.containsKey("1"));
		assertTrue(codeValues.containsKey("3"));
	}

}
