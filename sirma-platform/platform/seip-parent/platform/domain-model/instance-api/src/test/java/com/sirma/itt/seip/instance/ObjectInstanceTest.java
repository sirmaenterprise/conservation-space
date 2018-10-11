package com.sirma.itt.seip.instance;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.sirma.itt.seip.PropertiesChanges;

/**
 * {@link ObjectInstance} test
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 31/05/2018
 */
public class ObjectInstanceTest {

	@Test
	public void enableChangesTracking() throws Exception {
		ObjectInstance instance = new ObjectInstance();
		instance.enableChangesTracking();
		instance.add("key1", "value1");
		instance.add("key2", "value2");
		instance.add("key3", "value3");
		instance.getProperties().put("key4", 4);
		instance.remove("key2");

		assertEquals(5, instance.changes().count());

		instance.disableChangesTracking();
		instance.add("key6", "value6");

		assertEquals(5, instance.changes().count());
		instance.clearChanges();
		assertEquals(0, instance.changes().count());
	}

	@Test
	public void clearChanges_shouldRemoveAllChanges() throws Exception {
		ObjectInstance instance = new ObjectInstance();
		instance.enableChangesTracking();
		instance.add("key1", "value1");
		instance.add("key2", "value2");

		assertEquals(2, instance.changes().count());
		instance.clearChanges();
		assertEquals(0, instance.changes().count());
	}

	@Test(expected = IllegalStateException.class)
	public void enableChangesTracking_shouldFailedIfCalledTwice() {
		ObjectInstance instance = new ObjectInstance();
		instance.enableChangesTracking();
		instance.enableChangesTracking();
	}

	@Test(expected = IllegalStateException.class)
	public void disableChangesTracking_shouldFailedIfCalledTwice() {
		ObjectInstance instance = new ObjectInstance();
		instance.enableChangesTracking();
		instance.disableChangesTracking();
		instance.disableChangesTracking();
	}

	@Test
	public void setProperties_shouldRemoveTrackingFromIncomingMap() {
		ObjectInstance instance = new ObjectInstance();
		instance.enableChangesTracking();

		Map<String, Serializable> map = new HashMap<>();
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		instance.setProperties(changes.trackChanges(map));
		instance.add("key1", "value1");
		assertEquals("value1", instance.get("key1"));
		assertEquals(0, changes.changes().count());
		assertEquals(1, instance.changes().count());
	}

	@Test
	public void setProperties_shouldRemoveTrackingIfNotCurrentlyTracking() {
		ObjectInstance instance = new ObjectInstance();

		Map<String, Serializable> map = new HashMap<>();
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		instance.setProperties(changes.trackChanges(map));
		instance.add("key1", "value1");
		assertEquals("value1", instance.get("key1"));
		assertFalse(instance.isTracked());
	}
}
