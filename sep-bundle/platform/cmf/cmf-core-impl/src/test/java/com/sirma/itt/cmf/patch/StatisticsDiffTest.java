package com.sirma.itt.cmf.patch;

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * The Class StatisticsDiffTest.
 * 
 * @author BBonev
 */
public class StatisticsDiffTest {

	/**
	 * Test diff.
	 */
	@Test
	public void testDiff() {
		List<Pair<String, String>> statistics1 = new LinkedList<Pair<String, String>>();
		List<Pair<String, String>> statistics2 = new LinkedList<Pair<String, String>>();

		add(statistics1, "1", "1");
		add(statistics1, "2", "2");
		add(statistics1, "3", "4");
		add(statistics1, "4", "6");
		add(statistics1, "5", "6");
		add(statistics1, "0", "1");
		add(statistics1, "4", "0");
		add(statistics1, "9", "1");
		add(statistics1, "9", "1");
		add(statistics1, "9", "1");

		add(statistics2, "1", "1");
		add(statistics2, "2", "3");
		// add(statistics2, "3", "4");
		add(statistics2, "4", "6");
		add(statistics2, "5", "2");
		add(statistics2, "0", "1");
		add(statistics2, "0", "1");
		add(statistics2, "0", "1");
		add(statistics2, "4", "0");
		add(statistics2, "9", "2");

		List<String> diff = diff(statistics1, statistics2);

		List<String> expected = new LinkedList<String>();
		expected.add("\n(1, 1) == (1, 1)");
		expected.add("\n(2, 3) != (2, 2)");
		expected.add("\n(3, 4) <======== ");
		expected.add("\n(4, 6) == (4, 6)");
		expected.add("\n(4, 6) == (4, 6)");
		expected.add("\n(5, 2) != (5, 6)");
		expected.add("\n(0, 1) == (0, 1)");
		expected.add("\n ========> (0, 1)");
		expected.add("\n ========> (0, 1)");
		expected.add("\n(4, 0) == (4, 0)");
		expected.add("\n(4, 0) == (4, 0)");
		expected.add("\n(9, 2) != (9, 1)");
		expected.add("\n(9, 1) <======== ");
		expected.add("\n(9, 1) <======== ");

		Assert.assertEquals(expected, diff);
	}

	/**
	 * Adds the.
	 * 
	 * @param list
	 *            the list
	 * @param first
	 *            the first
	 * @param second
	 *            the second
	 */
	static void add(List<Pair<String, String>> list, String first, String second) {
		list.add(new Pair<String, String>(first, second));
	}

	/**
	 * Diff.
	 * 
	 * @param list1
	 *            the list1
	 * @param list2
	 *            the list2
	 * @return the list
	 */
	static List<String> diff(List<Pair<String, String>> list1, List<Pair<String, String>> list2) {
		List<String> result = new LinkedList<String>();
		int i, j;
		for (i = 0, j = 0; (i < list1.size()) && (j < list2.size());) {
			Pair<String, String> pair1 = list1.get(i);
			Pair<String, String> pair2 = list2.get(j);
			if (pair1.getFirst().equals(pair2.getFirst())) {
				if (!pair1.getSecond().equals(pair2.getSecond())) {
					// hash differs
					result.add(new StringBuilder().append("\n").append(pair2).append(" != ")
							.append(pair1).toString());
				} else {
					result.add(new StringBuilder().append("\n").append(pair2).append(" == ")
							.append(pair1).toString());
				}
				i++;
				j++;
			} else {
				int indexOf1 = CollectionUtils.indexOf(list1, pair2, i + 1);
				if (indexOf1 == -1) {
					// reached end of the other list and not found the element we need to
					// print remaining and finish
					int indexOf2 = CollectionUtils.indexOf(list2, pair1, j + 1);
					if (indexOf2 == -1) {
						for (int k = i; k < list1.size(); k++) {
							Pair<String, String> pair = list1.get(k);
							result.add(new StringBuilder().append("\n").append(pair)
									.append(" <======== ").toString());
						}
						for (int k = j; k < list2.size(); k++) {
							Pair<String, String> pair = list2.get(k);
							result.add(new StringBuilder().append("\n").append(" ========> ")
									.append(pair).toString());
						}
						// end printing
						break;
					} else {
						// element from the first is found into the second list
						for (int k = j; k < indexOf2; k++, j++) {
							Pair<String, String> pair = list2.get(k);
							result.add(new StringBuilder().append("\n").append(" ========> ")
									.append(pair).toString());
						}
						result.add(new StringBuilder().append("\n").append(list2.get(indexOf2))
								.append(" == ").append(pair1).toString());
					}
				} else {
					for (int k = i; k < indexOf1; k++, i++) {
						Pair<String, String> pair = list1.get(k);
						result.add(new StringBuilder().append("\n").append(pair)
								.append(" <======== ").toString());
					}
					result.add(new StringBuilder().append("\n").append(pair2).append(" == ")
							.append(list1.get(indexOf1)).toString());
				}
			}
		}
		if (i < list1.size()) {
			for (; i < list1.size(); i++) {
				Pair<String, String> pair = list1.get(i);
				result.add(new StringBuilder().append("\n").append(pair).append(" <======== ")
						.toString());
			}
		}
		if (j < list2.size()) {
			for (; j < list2.size(); j++) {
				Pair<String, String> pair = list2.get(i);
				result.add(new StringBuilder().append("\n").append(" ========> ").append(pair)
						.toString());
			}
		}
		return result;
	}

}
