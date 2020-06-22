package com.sirma.itt.seip.cache.lookup;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.MemoryCache;
import com.sirma.itt.seip.cache.SimpleCache;

/**
 * Test for {@link EntityLookupCache}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/03/2019
 */
public class EntityLookupCacheTest {

	private SimpleCache<Serializable, Object> cache = new MemoryCache<>();

	@Mock
	private EntityLookupCallbackDAO<String, Object, String> entityLookup;

	private EntityLookupCache<String, Object, String> lookupCache;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		cache.clear();
		lookupCache = new EntityLookupCache<>(cache, "cache_name", entityLookup);
		mockBasicData();
	}

	private void mockBasicData() {
		when(entityLookup.findByKey(anyString())).then(a -> new Pair<>(a.getArgumentAt(0, String.class), "VALUE"));
		when(entityLookup.getValueKey(any())).then(a -> {
			Object value = a.getArgumentAt(0, Object.class);
			return value == null ? null : "valueKey";
		});
		when(entityLookup.createValue(any())).then(a -> new Pair<>("key", a.getArguments()[0]));
		when(entityLookup.updateValue(any(), any())).thenReturn(1);
	}

	@Test
	public void getByKey_shouldNotAddValueKeyIfNotEnabled() throws Exception {
		Pair<String, Object> pair = lookupCache.getByKey("key");
		assertNotNull(pair);
		assertEquals(1L, cache.getKeys().count());
	}

	@Test
	public void getByKey_shouldAddValueKeyIfEnabled() throws Exception {
		enableSecondaryKey();
		Pair<String, Object> pair = lookupCache.getByKey("key");
		assertNotNull(pair);
		assertEquals(2L, cache.getKeys().count());
	}

	@Test
	public void containsKey() throws Exception {
		lookupCache.getByKey("key");
		assertTrue(lookupCache.containsKey("key"));
	}

	@Test(expected = IllegalStateException.class)
	public void getByValue_shouldFailIfSecondaryKeyIsNotEnabled() throws Exception {
		lookupCache.getByValue("valueKey");
	}

	@Test
	public void getByValue_shouldReturnValueByItsKeyWhenEnabled() throws Exception {
		enableSecondaryKey();
		lookupCache.getByKey("key");
		Pair<String, Object> pair = lookupCache.getByValue("valueKey");
		assertNotNull(pair);
		assertNotNull(pair.getSecond());
	}

	@Test(expected = IllegalStateException.class)
	public void getOrCreateByValue_shouldFailIfSecondaryKeyIsNotEnabled() throws Exception {
		lookupCache.getOrCreateByValue(new Object());
	}

	@Test
	public void getOrCreateByValue() throws Exception {
		enableSecondaryKey();
		Pair<String, Object> pair = lookupCache.getOrCreateByValue(new Object());
		assertNotNull(pair);
		assertEquals("key", pair.getFirst());
		assertNotNull(pair.getSecond());
	}


	@Test
	public void updateValue_shouldNotSetSecondaryKeyIfNotEnabled() throws Exception {
		lookupCache.getByKey("key");
		int updateCount = lookupCache.updateValue("key", "newValue");
		assertEquals(1, updateCount);
		assertEquals(1, cache.getKeys().count());
		Pair<String, Object> updated = lookupCache.getByKey("key");
		assertNotNull(updated);
		assertEquals("newValue", updated.getSecond());
	}

	@Test
	public void updateValue_shouldSetSecondaryKeyIfEnabled() throws Exception {
		enableSecondaryKey();
		lookupCache.getByKey("key");
		int updateCount = lookupCache.updateValue("key", "newValue");
		assertEquals(1, updateCount);
		assertEquals(2, cache.getKeys().count());
		Pair<String, Object> updated = lookupCache.getByKey("key");
		assertNotNull(updated);
		assertEquals("newValue", updated.getSecond());
	}

	@Test
	public void getKey_shouldDoNothingWhenSecondaryKeyIsNotEnabled() throws Exception {
		assertNotNull(lookupCache.getByKey("key"));
		assertNull(lookupCache.getKey("valueKey"));
	}

	@Test
	public void getKey_shouldReturnKeyWhenSecondaryKeyIsEnabled() throws Exception {
		enableSecondaryKey();
		assertNotNull(lookupCache.getByKey("key"));
		assertEquals("key", lookupCache.getKey("valueKey"));
	}

	@Test
	public void getValue() throws Exception {
		lookupCache.getByKey("key");
		assertNotNull(lookupCache.getValue("key"));
		assertNull(lookupCache.getValue("invalidKey"));
		cache.clear();
		assertNull(lookupCache.getValue("key"));
		assertNull(lookupCache.getValue("invalidKey"));
		lookupCache.setValue("key", null);
		assertNull(lookupCache.getValue("key"));
		// this should be once for the first call getByKey, the rest should not trigger lookups
		verify(entityLookup).findByKey(any());
	}

	@Test
	public void setValue_shouldNotAffectSecondaryKeyIfNotEnabled() throws Exception {
		lookupCache.setValue("key", null);
		assertNull(lookupCache.getValue("key"));

		lookupCache.setValue("key", "value");
		assertEquals("value", lookupCache.getValue("key"));
		assertEquals(1, cache.getKeys().count());
	}

	@Test
	public void setValue_shouldUpdateSecondaryKeyIfEnabled() throws Exception {
		enableSecondaryKey();
		lookupCache.setValue("key", null);
		assertNull(lookupCache.getValue("key"));

		lookupCache.setValue("key", "value");
		assertEquals("value", lookupCache.getValue("key"));
		// 1 the primary key
		// 2 the null value key - it not removed when value is changed from null to non null (no idea why)
		// 3 the secondary key after the second update
		assertEquals(3, cache.getKeys().count());
	}

	@Test
	public void deleteByKey() throws Exception {
		lookupCache.getByKey("key");
		lookupCache.deleteByKey("key");
		verify(entityLookup).deleteByKey("key");
	}

	@Test
	public void deleteByValue_shouldDoNothingSecondaryKeyNotEnabled() throws Exception {
		lookupCache.getByKey("key");
		assertEquals(0, lookupCache.deleteByValue("value"));
		verify(entityLookup, never()).deleteByValue(any());
	}

	@Test
	public void deleteByValue_shouldRemoveKeyAndValueIfSecondaryKeyEnabled() throws Exception {
		enableSecondaryKey();
		lookupCache.getByKey("key");
		lookupCache.deleteByValue("VALUE");
		verify(entityLookup).deleteByValue("VALUE");
		assertEquals(0, cache.getKeys().count());
	}

	@Test
	public void removeByKey() throws Exception {
		lookupCache.getByKey("key");
		lookupCache.removeByKey("key");
		verify(entityLookup, never()).deleteByKey("key");
		assertEquals(0, cache.getKeys().count());
	}

	@Test(expected = IllegalStateException.class)
	public void removeByValue_shouldFailIfSecondaryKeyIsNotEnabled() throws Exception {
		try {
			lookupCache.getByKey("key");
			lookupCache.removeByValue("key");
		} finally {
			verify(entityLookup, never()).deleteByValue("key");
			assertEquals(1, cache.getKeys().count());
		}
	}

	@Test
	public void removeByValue_shouldRemoveEntriesIfSecondaryKeyIsEnabled() throws Exception {
		enableSecondaryKey();
		lookupCache.getByKey("key");
		lookupCache.removeByValue("key");
		verify(entityLookup, never()).deleteByValue("key");
		assertEquals(0, cache.getKeys().count());
	}

	@Test
	public void clear() throws Exception {
		lookupCache.getByKey("key");
		lookupCache.clear();
		assertEquals(0, cache.getKeys().count());
	}

	@Test
	public void primaryKeys() throws Exception {
		lookupCache.getByKey("key");
		Set<String> keys = lookupCache.primaryKeys();
		assertEquals(Collections.singleton("key"), keys);
	}

	@Test
	public void secondaryKeys_shouldDoNothingIfSecondaryKeysIsNotEnabled() throws Exception {
		lookupCache.getByKey("key");
		Set<String> keys = lookupCache.secondaryKeys();
		assertEquals(Collections.emptySet(), keys);
	}

	@Test
	public void secondaryKeys_shouldReturnSecondaryKeysIIfEnabled() throws Exception {
		enableSecondaryKey();
		lookupCache.getByKey("key");
		Set<String> keys = lookupCache.secondaryKeys();
		assertEquals(Collections.singleton("valueKey"), keys);
	}

	private void enableSecondaryKey() {
		lookupCache.setSecondaryKeyEnabled(true);
	}
}