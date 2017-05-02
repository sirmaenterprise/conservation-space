package com.sirma.itt.seip.concurrent.collections;

import java.io.Serializable;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sirma.itt.seip.collections.ContextualMap;

/**
 * Tests for {@link ConcurrentMultiValueCollection}
 *
 * @author nvelkov
 */
public class ConcurrentMultiValueCollectionTest {

	@Spy
	ContextualMap<Serializable, Set<Serializable>> multiValueMap = ContextualMap.create();

	/** The multi value collection. */
	@InjectMocks
	private ConcurrentMultiValueCollection multiValueCollection;

	/**
	 * Setup before every method execution.
	 */
	@BeforeMethod
	public void beforeMethod() {
		MockitoAnnotations.initMocks(this);
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
		Assert.assertTrue(multiValueCollection.getMultiValueCollection().get("testkey").contains("testval2"));
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
		multiValueCollection.removeFromKey("testkey", "testval");
		Assert.assertFalse(multiValueCollection.getMultiValueCollection().containsKey("testkey"));
	}

	/**
	 * Test value removal.
	 */
	@Test
	public void testValueRemoval() {
		multiValueCollection.addToKey("testkey", "testval");
		multiValueCollection.addToKey("testkey", "testval2");
		multiValueCollection.removeFromKey("testkey", "testval");
		Assert.assertFalse(multiValueCollection.getMultiValueCollection().get("testkey").contains("testval"));
	}

}
