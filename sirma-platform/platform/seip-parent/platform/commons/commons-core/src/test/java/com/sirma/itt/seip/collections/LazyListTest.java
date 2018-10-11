/**
 *
 */
package com.sirma.itt.seip.collections;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.testng.annotations.Test;

import com.sirma.itt.seip.CachingSupplier;
import com.sirma.itt.seip.collections.LazyList;

/**
 * @author BBonev
 *
 */
public class LazyListTest {

	@Test
	public void test_LazyCollection() {
		Item initial = new Item(3);

		Supplier<List<Item>> expandedSource = () -> generateItems(5);
		Supplier<List<Item>> supplier = spy(CachingSupplier.of(expandedSource));
		LazyList<Item> items = new LazyList<>(initial, supplier);
		assertTrue(items.contains(initial));
		verify(supplier, never()).get();
		items.iterator();
		verify(supplier).get();

		int indexOf = items.indexOf(initial);
		assertTrue(indexOf > 0);
		Item item = items.get(indexOf);
		assertTrue(initial == item);

		assertTrue(items.contains(new Item(2)));
	}

	private static List<Item> generateItems(int count) {
		List<Item> items = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			items.add(new Item(i));
		}
		return items;
	}

	/**
	 * The Class Item.
	 */
	private static class Item {
		private final int data;

		/**
		 * Instantiates a new item.
		 *
		 * @param data
		 *            the data
		 */
		public Item(int data) {
			this.data = data;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + data;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Item other = (Item) obj;
			if (data != other.data) {
				return false;
			}
			return true;
		}

	}

}
