package com.sirma.itt.seip.instance;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.sirma.itt.seip.PropertiesChanges;
import com.sirma.itt.seip.domain.util.PropertiesUtil;

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

	@Test
	public void propertiesChangesShouldNotInterfereWithInstanceSerialization() {
		ObjectInstance instance = new ObjectInstance();
		instance.enableChangesTracking();
		instance.add("key1", "serializable value");
		instance.add("key2", (Serializable) Collections.singletonList("value in an immutable collection"));
		instance.append("key3", "value in an mutable collection");
		instance.add("key4", new PartiallySerializableValue());
		instance.append("key4", new PartiallySerializableValue());
		instance.remove("key4");

		Map<String, Serializable> cloneProperties = PropertiesUtil.cloneProperties(instance.getProperties());
		assertEquals(3, cloneProperties.size());
	}

	@Test
	public void applyChanges_shouldProduceSameChangesInTheTargetInstance() {
		ObjectInstance instance = new ObjectInstance();
		instance.add("key1", "value1");
		instance.add("key2", "value2");
		instance.add("key3", "value3");
		instance.getProperties().put("key4", 4);
		instance.append("key3", "value3.2");
		instance.append("key3", "value3.3");
		instance.enableChangesTracking();
		instance.add("key5", "value5");
		instance.add("key6", "value6");
		instance.append("key6", "value6.2");
		instance.append("key6", "value6.3");
		instance.add("key2", "value2-1");
		instance.remove("key6", "value6.2");
		instance.remove("key1");

		ObjectInstance freshCopy = new ObjectInstance();
		freshCopy.add("key1", "value1");
		freshCopy.add("key2", "value2");
		freshCopy.add("key3", "value3");
		freshCopy.getProperties().put("key4", 4);
		freshCopy.append("key3", "value3.2");
		freshCopy.append("key3", "value3.3");
		freshCopy.enableChangesTracking();

		freshCopy.applyChanges(instance.changes());

		assertEquals(instance.getProperties(), freshCopy.getProperties());
		// note that the actual generated changes mey not the the same but the result should be the same
		// the changes differ because different modification methods are done to reach the end result
	}

	private class PartiallySerializableValue implements Serializable {
		private Object nonSerializableValue = new Object();
	}
}
