package com.sirma.itt.seip.concurrent;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * The Class FragmentedWorkTest.
 *
 * @author BBonev
 */
@Test
public class FragmentedWorkTest {
	/**
	 * Test work.
	 *
	 * @param data
	 *            the data
	 * @param fragments
	 *            the fragments
	 */
	@Test(dataProvider = "dataProvider")
	public void testWork(int data, int fragments) {
		AtomicInteger processed = new AtomicInteger();
		FragmentedWork.doWork(createData(data), fragments, f -> {
			Assert.assertNotNull(f);
			Assert.assertFalse(f.isEmpty());
			Assert.assertTrue(f.size() <= fragments);
			processed.getAndAdd(f.size());
		});
		Assert.assertEquals(processed.get(), data);
	}

	@Test(dataProvider = "dataProvider")
	public void testWorkWithResult(int data, int fragments) {
		AtomicInteger processed = new AtomicInteger();
		Collection<Integer> createData = createData(data);
		Collection<Integer> result = FragmentedWork.doWorkWithResult(createData, fragments, f -> {
			Assert.assertNotNull(f);
			Assert.assertFalse(f.isEmpty());
			Assert.assertTrue(f.size() <= fragments);
			processed.getAndAdd(f.size());
			return f;
		});
		Assert.assertEquals(processed.get(), data);
		assertEquals(result.size(), data);
		assertEquals(result, createData);
	}

	@Test(dataProvider = "dataProvider")
	public void testWorkWithReduce(int data, int fragments) {
		AtomicInteger processed = new AtomicInteger();
		Collection<Integer> createData = createData(data);
		Collection<Integer> result = FragmentedWork.doWorkAndReduce(createData, fragments, f -> {
			Assert.assertNotNull(f);
			Assert.assertFalse(f.isEmpty());
			Assert.assertTrue(f.size() <= fragments);
			processed.getAndIncrement();
			return f.size();
		});
		assertEquals(result.size(), processed.get());
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testComputeBatchSize_invalidFragmentNumber() {
		FragmentedWork.computeBatchForNFragments(1, 0);
	}

	@Test
	public void testComputeBatchSize() {
		Assert.assertEquals(FragmentedWork.computeBatchForNFragments(10, 2), 5);
		Assert.assertEquals(FragmentedWork.computeBatchForNFragments(13, 2), 7);
		Assert.assertEquals(FragmentedWork.computeBatchForNFragments(13, 3), 5);
		Assert.assertEquals(FragmentedWork.computeBatchForNFragments(13, 15), 1);
	}

	/**
	 * Provide test cases.
	 *
	 * @return the object[][]
	 */
	@DataProvider(name = "dataProvider")
	public Object[][] provideTestCases() {
		return new Object[][] { { 11, 5 }, { 11, 2 }, { 11, 12 }, { 11, 3 }, { 11, 11 }, { 12, 6 }, { 12, 3 },
				{ 12, 4 }, { 13, 7 }, { 0, 5 } };
	}

	/**
	 * Creates the data.
	 *
	 * @param size
	 *            the size
	 * @return the collection
	 */
	private static Collection<Integer> createData(int size) {
		List<Integer> data = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			data.add(Integer.valueOf(i));
		}
		return data;
	}

}
