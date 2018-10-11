package com.sirma.itt.seip;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.junit.Test;

/**
 * Test for {@link PropertiesChanges}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 19/06/2018
 */
public class PropertiesChangesTest {

	@Test
	public void updateChangeShouldIgnoreSaveValues() {
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		changes.update("prop1", "value1", "value2");
		changes.update("prop2", "value1", "value1");
		changes.update("prop2", null, null);
		assertEquals(1L, changes.changes().count());
	}
	@Test
	public void trackMapChanges() throws Exception {
		Map<String, Serializable> properties = new LinkedHashMap<>();
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		Map<String, Serializable> trackedMap = changes.trackChanges(properties);
		trackedMap.put("key1", "value1");
		trackedMap.putAll(Collections.singletonMap("key2", "value2"));
		trackedMap.putIfAbsent("key3", "value3");
		trackedMap.put("key3", "value4");

		trackedMap.keySet().remove("key1");
		Iterator<Map.Entry<String, Serializable>> it = trackedMap.entrySet().iterator();
		it.next();
		it.remove();

		assertEquals(1, trackedMap.size());
		assertEquals("value4", trackedMap.get("key3"));

		trackedMap.clear();

		assertEquals(7, changes.changes().count());
		List<PropertyChange<Serializable>> changesList = changes.changes().collect(Collectors.toList());
		int index = 0;
		assertEquals(PropertyChange.add("key1", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.add("key2", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.add("key3", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.update("key3", "value4", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.remove("key1", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.remove("key2", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.remove("key3", "value4"), changesList.get(index++));
	}

	@Test
	public void trackCollectionChanges() throws Exception {
		Collection<String> collection = new LinkedBlockingDeque<>();
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		Collection<String> trackedCollection = changes.trackChanges("aCollection", collection);
		trackedCollection.add("value1");
		trackedCollection.addAll(Collections.singleton("value2"));
		trackedCollection.addAll(Arrays.asList("value3", "value4"));

		trackedCollection.remove("value2");
		Iterator<String> it = trackedCollection.iterator();
		it.next();
		it.remove();

		// here we should have 2 elements value3 and value4
		Iterator<String> lit = trackedCollection.iterator();
		assertEquals("value3", lit.next());
		// we should have again only value3 and value4
		trackedCollection.retainAll(Collections.singleton("value4"));

		assertEquals(1, trackedCollection.size());
		assertEquals(1, collection.size());

		trackedCollection.clear();

		assertEquals(8, changes.changes().count());
		List<PropertyChange<Serializable>> changesList = changes.changes().collect(Collectors.toList());
		int index = 0;
		assertEquals(PropertyChange.add("aCollection", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.add("aCollection", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.add("aCollection", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.add("aCollection", "value4"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aCollection", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aCollection", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aCollection", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aCollection", "value4"), changesList.get(index++));
	}

	@Test
	public void trackListChanges() throws Exception {
		List<String> list = new LinkedList<>();
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		List<String> trackedList = changes.trackChanges("aList", list);
		trackedList.add("value1");
		trackedList.addAll(Collections.singleton("value2"));
		trackedList.add(1, "value3");
		trackedList.addAll(2, Collections.singleton("value4"));

		trackedList.remove("value2");
		Iterator<String> it = trackedList.iterator();
		it.next();
		it.remove();

		// here we should have 2 elements value3 and value4
		ListIterator<String> lit = trackedList.listIterator();
		assertEquals("value3", lit.next());
		// add new element between value3 and value4
		lit.add("value5");
		assertEquals("value5", lit.previous());
		// override value5
		lit.set("value6");
		assertEquals("value6", trackedList.remove(1));
		// we should have again only value3 and value4
		trackedList.retainAll(Collections.singleton("value4"));

		assertEquals(1, trackedList.size());
		assertEquals("value4", trackedList.get(0));
		assertEquals(1, list.size());
		assertEquals("value4", list.get(0));

		trackedList.set(0, "value40");
		trackedList.removeAll(Collections.singletonList("value40"));

		assertEquals(12, changes.changes().count());
		List<PropertyChange<Serializable>> changesList = changes.changes().collect(Collectors.toList());
		int index = 0;
		assertEquals(PropertyChange.add("aList", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value4"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value5"), changesList.get(index++));
		assertEquals(PropertyChange.update("aList", "value6", "value5"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value6"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.update("aList", "value40","value4"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value40"), changesList.get(index++));
	}

	@Test
	public void trackSetChanges() throws Exception {
		Set<String> set = new LinkedHashSet<>();
		PropertiesChanges<Serializable> changes = new PropertiesChanges<>();
		Set<String> trackedSet = changes.trackChanges("aList", set);
		trackedSet.add("value1");
		trackedSet.addAll(Arrays.asList("value1", "value2", "value3", "value4"));

		trackedSet.remove("value2");
		Iterator<String> it = trackedSet.iterator();
		it.next();
		it.remove();
		trackedSet.retainAll(Collections.singleton("value4"));

		assertEquals(1, trackedSet.size());
		assertEquals("value4", trackedSet.iterator().next());
		assertEquals(1, set.size());
		assertEquals("value4", set.iterator().next());

		assertEquals(7, changes.changes().count());
		List<PropertyChange<Serializable>> changesList = changes.changes().collect(Collectors.toList());
		int index = 0;
		assertEquals(PropertyChange.add("aList", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value3"), changesList.get(index++));
		assertEquals(PropertyChange.add("aList", "value4"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value2"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value1"), changesList.get(index++));
		assertEquals(PropertyChange.remove("aList", "value3"), changesList.get(index++));
	}

}
