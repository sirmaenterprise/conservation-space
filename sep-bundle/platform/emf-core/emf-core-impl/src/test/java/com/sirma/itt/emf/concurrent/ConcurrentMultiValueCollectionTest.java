package com.sirma.itt.emf.concurrent;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.emf.util.EmfTest;

/**
 * Tests for {@link ConcurrentMultiValueCollection}
 * 
 * @author nvelkov
 */
public class ConcurrentMultiValueCollectionTest extends EmfTest {

	/** The multi value collection. */
	private ConcurrentMultiValueCollection multiValueCollection;

	/**
	 * Setup before every method execution.
	 */
	@BeforeMethod
	public void setup() {
		multiValueCollection = new ConcurrentMultiValueCollection();
	}

	/**
	 * Test key addition.
	 */
	@Test
	public void testKeyAddition() {
		multiValueCollection.addToKey("testkey", "testval");
		Assert.assertTrue(multiValueCollection.getMultiValueCollection().containsKey("testkey"));
	}

	/**
	 * Test value addition.
	 */
	@Test
	public void testValueAddition() {

		multiValueCollection.addToKey("testkey", "testval");
		multiValueCollection.addToKey("testkey", "testval2");
		Assert.assertTrue(multiValueCollection.getMultiValueCollection().get("testkey")
				.contains("testval2"));
	}

	/**
	 * Test key contains.
	 */
	@Test
	public void testKeyContains() {
		multiValueCollection.addToKey("testkey", "testval");
		Assert.assertTrue(multiValueCollection.keyContains("testkey", "testval"));
	}

	/**
	 * Test key removal.
	 */
	@Test
	public void testKeyRemoval() {
		multiValueCollection.addToKey("testkey", "testval");
		multiValueCollection.removeFromKey(new ConcurrentMultiValueCollectionEvent("testkey",
				"testval"));
		Assert.assertFalse(multiValueCollection.getMultiValueCollection().containsKey("testkey"));
	}

	/**
	 * Test value removal.
	 */
	@Test
	public void testValueRemoval() {
		multiValueCollection.addToKey("testkey", "testval");
		multiValueCollection.addToKey("testkey", "testval2");
		multiValueCollection.removeFromKey(new ConcurrentMultiValueCollectionEvent("testkey",
				"testval"));
		Assert.assertFalse(multiValueCollection.getMultiValueCollection().get("testkey")
				.contains("testval"));
	}

}
