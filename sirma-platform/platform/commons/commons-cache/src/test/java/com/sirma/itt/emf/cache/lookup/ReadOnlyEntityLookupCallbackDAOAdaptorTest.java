package com.sirma.itt.emf.cache.lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Serializable;

import org.junit.Test;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.cache.lookup.EntityLookupCallbackDAO;
import com.sirma.itt.seip.cache.lookup.ReadOnlyEntityLookupCallbackDAOAdaptor;

/**
 * Test for {@link ReadOnlyEntityLookupCallbackDAOAdaptor}
 *
 * @since 2017-03-29
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 */
public class ReadOnlyEntityLookupCallbackDAOAdaptorTest {

	@Test
	public void should_fetchValueByKnownKey() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> "key".equals(key) ? "value" : null);

		assertEquals(new Pair<>("key", "value"), dao.findByKey("key"));
	}

	@Test
	public void should_returnNullForUnkownKey() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> "key".equals(key) ? "value" : null);

		assertNull(dao.findByKey(null));
		assertNull(dao.findByKey("InvalidKey"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void should_FailOnCreate() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> null);

		dao.createValue("value");
	}

	@Test()
	public void should_ReturnNullOnFindByValue() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> null);

		assertNull(dao.findByValue("value"));
	}

	@Test()
	public void should_ReturnNullOngetValueKey() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> null);

		assertNull(dao.getValueKey("value"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void should_failOnUpdateValue() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> null);

		dao.updateValue("key", "value");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void should_failOnDeleteByKey() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> null);

		dao.deleteByKey("key");
	}

	@Test(expected = UnsupportedOperationException.class)
	public void should_failOnDeleteByValue() throws Exception {
		EntityLookupCallbackDAO<Serializable, String, Serializable> dao = ReadOnlyEntityLookupCallbackDAOAdaptor
				.from(key -> null);

		dao.deleteByValue("key");
	}
}
