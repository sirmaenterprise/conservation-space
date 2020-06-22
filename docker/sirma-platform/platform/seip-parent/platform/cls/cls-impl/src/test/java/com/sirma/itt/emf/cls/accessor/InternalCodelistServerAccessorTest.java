package com.sirma.itt.emf.cls.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.sirma.itt.seip.db.DbDao;
import com.sirma.sep.cls.CodeListServiceImpl;
import com.sirma.sep.cls.db.CodeEntityDao;
import com.sirma.sep.cls.db.DbDaoStub;
import com.sirma.sep.cls.db.entity.CodeListDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeListEntity;
import com.sirma.sep.cls.db.entity.CodeValueDescriptionEntity;
import com.sirma.sep.cls.db.entity.CodeValueEntity;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.domain.codelist.CodelistPropertiesConstants;
import com.sirma.itt.seip.domain.codelist.model.CodeValue;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * Tests the functionality of {@link InternalCodelistServerAccessor}.
 *
 * @author Vilizar Tsonev
 */
@RunWith(CdiRunner.class)
@AdditionalClasses({ CodeListServiceImpl.class, CodeEntityDao.class })
public class InternalCodelistServerAccessorTest {

	@Produces
	@Mock
	private DbDao dbDao;
	private DbDaoStub dbDaoStub;

	@Inject
	private InternalCodelistServerAccessor codelistAccessor;

	@Before
	public void beforeEach() {
		dbDaoStub = new DbDaoStub(dbDao);
	}

	/**
	 * Tests the {@link InternalCodelistServerAccessor#getCodeValues(Integer, String)} method.
	 */
	@Test
	public void testGetCodeValues() {
		// init the expected code value
		CodeValue expectedValue1 = new CodeValue();
		expectedValue1.setCodelist(1);
		expectedValue1.setValue("INIT");
		Map<String, Serializable> map = new HashMap<>(7);
		map.put("bg", "описание на български");
		map.put("en", "english description");
		map.put("cz", "něco v české");
		map.put("fr", "description en français");
		map.put(CodelistPropertiesConstants.COMMENT, "english comment");
		map.put(CodelistPropertiesConstants.EXTRA1, "INIT_extra1");
		map.put(CodelistPropertiesConstants.EXTRA2, "INIT_extra2");
		map.put(CodelistPropertiesConstants.EXTRA3, "INIT_extra3");
		expectedValue1.setProperties(Collections.unmodifiableMap(map));
		// init the expected map of code values
		Map<String, CodeValue> expectedCodeValuesMap = new HashMap<>();
		expectedCodeValuesMap.put("INIT", expectedValue1);

		dbDaoStub.withListEntity("1");
		CodeValueEntity codeValueEntity = dbDaoStub.withValueEntityForList("1", "INIT");
		// english description
		CodeValueDescriptionEntity englishDescription = getEnglishDescription();
		// bulgarian description
		CodeValueDescriptionEntity bulgarianDescription = new CodeValueDescriptionEntity();
		bulgarianDescription.setDescription("описание на български");
		bulgarianDescription.setLanguage("bg");
		// czech description
		CodeValueDescriptionEntity czechDescription = new CodeValueDescriptionEntity();
		czechDescription.setDescription("něco v české");
		czechDescription.setLanguage("cz");
		// french description
		CodeValueDescriptionEntity frenchDecription = new CodeValueDescriptionEntity();
		frenchDecription.setDescription("description en français");
		frenchDecription.setLanguage("fr");
		codeValueEntity.setDescriptions(Arrays.asList(englishDescription, frenchDecription, bulgarianDescription, czechDescription));

		assertEquals(expectedCodeValuesMap, codelistAccessor.getCodeValues(1, "EN"));
	}

	/**
	 * Tests the {@link InternalCodelistServerAccessor#getAllCodelists(String)} method.
	 */
	@Test
	public void testGetAllCodeLists() {
		// first codelist
		CodeListEntity cl1 = dbDaoStub.withListEntity("1");
		// add BG description
		CodeListDescriptionEntity bulgarianDescription1 = new CodeListDescriptionEntity();
		bulgarianDescription1.setDescription("Първа код листа");
		bulgarianDescription1.setLanguage("bg");
		// add EN description
		CodeListDescriptionEntity englishDescription1 = new CodeListDescriptionEntity();
		englishDescription1.setDescription("First code list");
		englishDescription1.setLanguage("en");
		// Add CZ description
		CodeListDescriptionEntity czechDescription1 = new CodeListDescriptionEntity();
		czechDescription1.setDescription("první kód list");
		czechDescription1.setLanguage("cz");
		cl1.setDescriptions(Arrays.asList(bulgarianDescription1, englishDescription1, czechDescription1));

		// second codelist
		CodeListEntity cl2 = dbDaoStub.withListEntity("2");
		CodeListDescriptionEntity englishDescription2 = new CodeListDescriptionEntity();
		englishDescription2.setDescription("Second code list");
		englishDescription2.setLanguage("en");
		cl2.setDescriptions(Collections.singletonList(englishDescription2));

		// Expected mapping
		Map<BigInteger, String> expected = new HashMap<>();
		expected.put(new BigInteger("1"), "First code list");
		expected.put(new BigInteger("2"), "Second code list");

		Map<BigInteger, String> actual = codelistAccessor.getAllCodelists("EN");
		assertEquals(expected, actual);
	}

	@Test
	public void shouldFilterInactiveCodeValues() {
		dbDaoStub.withListEntity("1");

		CodeValueEntity codeValue1 = dbDaoStub.withValueEntityForList("1", "INIT");
		codeValue1.setDescriptions(Collections.singletonList(getEnglishDescription()));
		codeValue1.setActive(true);

		CodeValueEntity codeValue2 = dbDaoStub.withValueEntityForList("1", "APPROVED");
		codeValue2.setDescriptions(Collections.singletonList(getEnglishDescription()));
		codeValue2.setActive(false);

		Map<String, CodeValue> codeListOneValuesMap = codelistAccessor.getCodeValues(1, "EN");
		assertEquals(1, codeListOneValuesMap.size());
		assertTrue(codeListOneValuesMap.containsKey("INIT"));
	}

	private static CodeValueDescriptionEntity getEnglishDescription() {
		CodeValueDescriptionEntity englishDescription = new CodeValueDescriptionEntity();
		englishDescription.setDescription("english description");
		englishDescription.setLanguage("en");
		englishDescription.setComment("english comment");
		return englishDescription;
	}
}
