package com.sirma.itt.emf.hash;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.emf.util.EmfTest;
import com.sirma.itt.emf.util.InstanceProxyMock;

/**
 * Test for {@link HashCalculator} implementation class.
 * 
 * @author BBonev
 */
@Test
public class HashCalculatorImplTest extends EmfTest {

	/** The calculator. */
	HashCalculator calculator;

	/**
	 * Inits the.
	 */
	@BeforeMethod
	public void init() {
		HashCalculatorImpl impl = new HashCalculatorImpl();

		List<HashCalculatorExtension> extensions = getExtensions();
		ReflectionUtils.setField(impl, "extensions", new InstanceProxyMock<>(extensions));
		impl.initializeMapping();

		calculator = impl;
	}

	/**
	 * Test collection hash.
	 */
	public void testCollectionHash() {
		Integer hash1 = calculator.computeHash(Arrays.asList("1", "2", "3"));
		Assert.assertNotEquals(hash1, Integer.valueOf(0));
		Integer hash2 = calculator.computeHash(Arrays.asList("2", "3", "1"));
		Assert.assertNotEquals(hash2, Integer.valueOf(0));
		Assert.assertEquals(hash1, hash2);
		Integer hash3 = calculator.computeHash(Arrays.asList("2", "3", "1", "4"));
		Assert.assertNotEquals(hash3, Integer.valueOf(0));
		Assert.assertNotEquals(hash3, hash1);
		Assert.assertNotEquals(hash3, hash2);
	}

	/**
	 * Test map hash.
	 */
	public void testMapHash() {
		Map<String, Serializable> map1 = new LinkedHashMap<String, Serializable>();
		map1.put("key1", 1);
		map1.put("key2", "2");
		map1.put("key3", 3L);
		Integer hash1 = calculator.computeHash(map1);
		Assert.assertNotEquals(hash1, Integer.valueOf(0));

		Map<String, Serializable> map2 = new LinkedHashMap<String, Serializable>();
		map2.put("key3", 3L);
		map2.put("key2", "2");
		map2.put("key1", 1);
		Integer hash2 = calculator.computeHash(map2);
		Assert.assertNotEquals(hash2, Integer.valueOf(0));

		Assert.assertEquals(hash1, hash2);

		Map<String, Serializable> map3 = new LinkedHashMap<String, Serializable>();
		map3.put("key1", 1);
		map3.put("key2", "2");
		Integer hash3 = calculator.computeHash(map3);
		Assert.assertNotEquals(hash3, Integer.valueOf(0));
		Assert.assertNotEquals(hash3, hash2);

		map3.put("key3", 3L);
		map3.put("key4", "4");
		Integer hash4 = calculator.computeHash(map3);
		Assert.assertNotEquals(hash4, Integer.valueOf(0));
		Assert.assertNotEquals(hash4, hash2);
		Assert.assertNotEquals(hash4, hash3);
	}

	/**
	 * Test equals by hash.
	 */
	public void testEqualsByHash() {
		List<String> list1 = Arrays.asList("1", "2", "3");
		List<String> list2 = Arrays.asList("2", "3", "1");

		Assert.assertFalse(list1.equals(list2));
		Assert.assertTrue(calculator.equalsByHash(list1, list2));
	}

	/**
	 * Test instance hash.
	 */
	public void testInstanceHash() {
		Instance full = createInstance("emf:id", "identifier", "dmsId", "cmId", "container",
				CollectionUtils.addToMap(null, new Pair<String, Serializable>("key1",
						"value1"), new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)));
		Integer baseHash = calculator.computeHash(full);
		Assert.assertNotEquals(baseHash, Integer.valueOf(0));

		// if the hash is reproducible
		Assert.assertEquals(calculator.computeHash(createInstance("emf:id", "identifier", "dmsId",
				"cmId", "container", CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance(null, "identifier", "dmsId",
				"cmId", "container", CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance("emf:id", null, "dmsId",
				"cmId", "container", CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance("emf:id", "identifier", null,
				"cmId", "container", CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance("emf:id", "identifier",
				"dmsId", null, "container", CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance("emf:id", "identifier",
				"dmsId", "cmId", null, CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2"),
						new Pair<String, Serializable>("key3", 3)))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance("emf:id", "identifier",
				"dmsId", "cmId", "container", CollectionUtils.addToMap(null,
						new Pair<String, Serializable>("key1", "value1"),
						new Pair<String, Serializable>("key2", "value2")))), baseHash);

		Assert.assertNotEquals(calculator.computeHash(createInstance("emf:id", "identifier",
				"dmsId", "cmId", "container", null)), baseHash);
	}

	/**
	 * Creates the instance by the given arguments.
	 * 
	 * @param id
	 *            the id
	 * @param definition
	 *            the definition
	 * @param dmsId
	 *            the dms id
	 * @param contentManagementId
	 *            the content management id
	 * @param container
	 *            the container
	 * @param properties
	 *            the properties
	 * @return the instance
	 */
	private Instance createInstance(String id, String definition, String dmsId,
			String contentManagementId, String container, Map<String, Serializable> properties) {
		EmfInstance instance = new EmfInstance();
		instance.setId(id);
		instance.setContainer(container);
		instance.setDmsId(dmsId);
		instance.setIdentifier(definition);
		instance.setProperties(properties);
		instance.setContentManagementId(contentManagementId);
		return instance;
	}

	/**
	 * Gets the extensions.
	 * 
	 * @return the extensions
	 */
	protected List<HashCalculatorExtension> getExtensions() {
		return Arrays.<HashCalculatorExtension> asList(new EmfHashCalculatorExtension());
	}
}
