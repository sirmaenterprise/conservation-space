package com.sirma.itt.seip.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Entity;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * Utility class providing helper methods for various types of <code>equals</code> functionality.
 *
 * @author BBonev
 */
public class EqualsHelper {

	private static final String FOUND_IN_LEFT = "\t<<<====== ";
	private static final String FOUND_IN_RIGHT = "\t\t\t======>>>\t";
	private static final String FOUND_EQUAL = "\t=======\t";
	private static final Logger LOGGER = LoggerFactory.getLogger(EqualsHelper.class);

	/**
	 * Instantiates a new equals helper.
	 */
	private EqualsHelper() {
		// utility class
	}

	/**
	 * Performs an equality check <code>left.equals(right)</code> after checking for null values
	 *
	 * @param left
	 *            the Object appearing in the left side of an <code>equals</code> statement
	 * @param right
	 *            the Object appearing in the right side of an <code>equals</code> statement
	 * @return Return true or false even if one or both of the objects are null
	 */
	public static boolean nullSafeEquals(Object left, Object right) {
		return left == right || left != null && right != null && left.equals(right); // NOSONAR
	}

	/**
	 * Performs an case-sensitive or case-insensitive equality check after checking for null values.
	 *
	 * @param left
	 *            the left
	 * @param right
	 *            the right
	 * @param ignoreCase
	 *            <tt>true</tt> to ignore case
	 * @return true, if successful
	 */
	public static boolean nullSafeEquals(String left, String right, boolean ignoreCase) {
		if (ignoreCase) {
			return left == right // NOSONAR
					|| left != null && right != null && left.equalsIgnoreCase(right);
		}
		return left == right || left != null && right != null && left.equals(right); // NOSONAR
	}

	/**
	 * Checks if the entity identifiers of the two given entity is equal.
	 *
	 * @param <S>
	 *            the generic type
	 * @param left
	 *            the left
	 * @param right
	 *            the right
	 * @return true, if successful
	 */
	@SuppressWarnings("unchecked")
	public static <S extends Serializable> boolean entityEquals(Entity<S> left, Entity<S> right) {
		if (left.getClass() == right.getClass() && left.getId() instanceof Comparable
				&& right.getId() instanceof Comparable) {
			return ((Comparable<S>) left.getId()).compareTo(right.getId()) == 0;
		}
		return false;
	}

	/**
	 * Returns a predicate that compares the value passed to the predicate with the argument. The compare is done via
	 * {@link #nullSafeEquals(Object, Object)}
	 *
	 * @param <T>
	 *            the compared type
	 * @param other
	 *            the other value to compare to
	 * @return the predicate that compares 2 objects
	 */
	public static <T> Predicate<T> equalsTo(T other) {
		return first -> nullSafeEquals(first, other);
	}

	/**
	 * Returns a predicate that compares the string passed to the predicate with the argument ignoring case. The compare
	 * is done via {@link #nullSafeEquals(String, String, boolean)}
	 *
	 * @param other
	 *            the other value to compare to
	 * @return the predicate that compares 2 strings
	 */
	public static Predicate<String> equalsToIgnoreCase(String other) {
		return first -> nullSafeEquals(first, other, true);
	}

	/** The Constant BUFFER_SIZE. */
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Performs a byte-level comparison between two streams.
	 *
	 * @param left
	 *            the left stream. This is closed at the end of the operation.
	 * @param right
	 *            an right stream. This is closed at the end of the operation.
	 * @return Returns <tt>true</tt> if the streams are identical to the last byte
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static boolean binaryStreamEquals(InputStream left, InputStream right) throws IOException {
		try (InputStream first = left; InputStream second = right) {
			if (first == second) { // NOSONAR
				// The same stream! This is pretty pointless, but they are
				// equal, nevertheless.
				return true;
			}
			return compareStreamsInternal(first, second);
		}
	}

	private static boolean compareStreamsInternal(InputStream first, InputStream second) throws IOException {
		byte[] leftBuffer = new byte[BUFFER_SIZE];
		byte[] rightBuffer = new byte[BUFFER_SIZE];
		while (true) {
			int leftReadCount = first.read(leftBuffer);
			int rightReadCount = second.read(rightBuffer);
			if (leftReadCount != rightReadCount) {
				// One stream ended before the other
				return false;
			} else if (leftReadCount == -1) {
				// Both streams ended without any differences found
				return true;
			}
			for (int i = 0; i < leftReadCount; i++) {
				if (leftBuffer[i] != rightBuffer[i]) {
					// We found a byte difference
					return false;
				}
			}
		}
		// The only exits with 'return' statements, so there is no need for
		// any code here
	}

	/**
	 * Compare two maps and generate a difference report between the actual and expected values. This method is
	 * particularly useful during unit tests as the result (if not <tt>null</tt>) can be appended to a failure message.
	 *
	 * @param actual
	 *            the map in hand
	 * @param expected
	 *            the map expected
	 * @return Returns a difference report or <tt>null</tt> if there were no differences. The message starts with a new
	 *         line and is neatly formatted.
	 */
	public static String getMapDifferenceReport(Map<?, ?> actual, Map<?, ?> expected) {
		Map<?, ?> copyResult = new HashMap<>(actual);

		boolean failure = false;

		StringBuilder sb = new StringBuilder(1024);
		sb.append("\nValues that don't match the expected values: ");
		for (Map.Entry<?, ?> entry : expected.entrySet()) {
			Object key = entry.getKey();
			Object expectedValue = entry.getValue();
			Object resultValue = actual.get(key);
			if (!EqualsHelper.nullSafeEquals(resultValue, expectedValue)) {
				sb
						.append("\n")
							.append("   Key: ")
							.append(key)
							.append("\n")
							.append("      Result:   ")
							.append(resultValue)
							.append("\n")
							.append("      Expected: ")
							.append(expectedValue);
				failure = true;
			}
			copyResult.remove(key);
		}
		sb.append("\nValues that are present but should not be: ");
		for (Map.Entry<?, ?> entry : copyResult.entrySet()) {
			Object key = entry.getKey();
			Object resultValue = entry.getValue();
			sb.append("\n").append("   Key: ").append(key).append("\n").append("      Result:   ").append(resultValue);
			failure = true;
		}
		if (failure) {
			return sb.toString();
		}
		return null;
	}

	/**
	 * Creates a diff as string between the given lists
	 *
	 * @param <E>
	 *            the element type
	 * @param list1
	 *            the list1
	 * @param list2
	 *            the list2
	 * @return the list
	 */
	@SuppressWarnings({ "squid:MethodCyclomaticComplexity", "squid:S134" })
	public static <E> List<String> diffLists(List<E> list1, List<E> list2) {
		List<String> result = new LinkedList<>();
		int i, j;
		for (i = 0, j = 0; i < list1.size() && j < list2.size();) {
			E pair1 = list1.get(i);
			E pair2 = list2.get(j);
			if (pair1.equals(pair2)) {
				result.add(new StringBuilder().append(pair2).append(FOUND_EQUAL).append(pair1).toString());
				i++;
				j++;
			} else {
				String s1 = pair1.toString();
				String s2 = pair2.toString();
				if (Math.abs(s1.length() - s2.length()) < Math.max(s1.length(), s2.length()) * 0.2
						&& s1.regionMatches(0, s2, 0, (int) Math.min(s1.length(), s2.length() * 0.3))) {
					result.add(new StringBuilder().append(pair2).append("\t<<<>>>\t").append(pair1).toString());
					i++;
					j++;
				} else {
					LOGGER.trace("Failed to compare \n{}\n{}", pair1, pair2);
					int indexOf1 = CollectionUtils.indexOf(list1, pair2, i + 1);
					if (indexOf1 == -1) {
						// TO DO: we can search for sub elements in the to result sub lists
						// reached end of the other list and not found the element we need to
						// print remaining and finish
						int indexOf2 = CollectionUtils.indexOf(list2, pair1, j + 1);
						if (indexOf2 == -1) {
							for (int k = i; k < list1.size(); k++) {
								E pair = list1.get(k);
								result.add(new StringBuilder().append(pair).append(FOUND_IN_LEFT).toString());
							}
							for (int k = j; k < list2.size(); k++) {
								E pair = list2.get(k);
								result.add(new StringBuilder().append(FOUND_IN_RIGHT).append(pair).toString());
							}
							// end printing
							break;
						}
						// element from the first is found into the second list
						for (int k = j; k < indexOf2; k++, j++) {
							E pair = list2.get(k);
							result.add(new StringBuilder().append(FOUND_IN_RIGHT).append(pair).toString());
						}
						result.add(new StringBuilder()
								.append(list2.get(indexOf2))
									.append(FOUND_EQUAL)
									.append(pair1)
									.toString());
					} else {
						for (int k = i; k < indexOf1; k++, i++) {
							E pair = list1.get(k);
							result.add(new StringBuilder().append(pair).append(FOUND_IN_LEFT).toString());
						}
						result.add(new StringBuilder()
								.append(pair2)
									.append(FOUND_EQUAL)
									.append(list1.get(indexOf1))
									.toString());
					}
				}
			}
		}
		buildDiffResult(list1, list2, result, i, j);

		return result;
	}

	private static <E> void buildDiffResult(List<E> list1, List<E> list2, List<String> result, int list1Index,
			int list2Index) {
		int i = list1Index;
		int j = list2Index;
		if (i < list1.size()) {
			for (; i < list1.size(); i++) {
				E pair = list1.get(i);
				result.add(new StringBuilder().append(pair).append(FOUND_IN_LEFT).toString());
			}
		}
		if (j < list2.size()) {
			for (; j < list2.size(); j++) {
				E pair = list2.get(j);
				result.add(new StringBuilder().append(FOUND_IN_RIGHT).append(pair).toString());
			}
		}
	}

	/**
	 * Enumeration for results returned by. {@link EqualsHelper#getMapComparison(Map, Map) map comparisons}.
	 *
	 * @author Derek Hulley
	 * @since 3.3
	 */
	public static enum MapValueComparison {

		/** The key was only present in the left map. */
		LEFT_ONLY,

		/** The key was only present in the right map. */
		RIGHT_ONLY,

		/** The key was present in both maps and the values were equal. */
		EQUAL,

		/** The key was present in both maps but not equal. */
		NOT_EQUAL;
	}

	/**
	 * Compare two maps.
	 * <p>
	 * The return codes that accompany the keys are:
	 * <ul>
	 * <li>{@link MapValueComparison#LEFT_ONLY}</li>
	 * <li>{@link MapValueComparison#RIGHT_ONLY}</li>
	 * <li>{@link MapValueComparison#EQUAL}</li>
	 * <li>{@link MapValueComparison#NOT_EQUAL}</li>
	 * </ul>
	 *
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 * @param left
	 *            the left side of the comparison
	 * @param right
	 *            the right side of the comparison
	 * @return Returns a map whose keys are a union of the two maps' keys, along with the value comparison result
	 */
	public static <K, V> Map<K, MapValueComparison> getMapComparison(Map<K, V> left, Map<K, V> right) {
		return getMapComparison(left, right, EqualsHelper::nullSafeEquals);
	}

	/**
	 * Compare two maps with custom comparator. The comparator must be able to handle <code>null</code> arguments.
	 * <p>
	 * The return codes that accompany the keys are:
	 * <ul>
	 * <li>{@link MapValueComparison#LEFT_ONLY}</li>
	 * <li>{@link MapValueComparison#RIGHT_ONLY}</li>
	 * <li>{@link MapValueComparison#EQUAL}</li>
	 * <li>{@link MapValueComparison#NOT_EQUAL}</li>
	 * </ul>
	 *
	 * @param <K>
	 *            the map key type
	 * @param <V>
	 *            the map value type
	 * @param left
	 *            the left side of the comparison
	 * @param right
	 *            the right side of the comparison
	 * @param comparator
	 *            the comparator to be used when comparing 2 values.
	 * @return Returns a map whose keys are a union of the two maps' keys, along with the value comparison result
	 */
	public static <K, V> Map<K, MapValueComparison> getMapComparison(Map<K, V> left, Map<K, V> right,
			BiPredicate<V, V> comparator) {
		Map<K, V> lleft = left;
		Map<K, V> lright = right;
		if (lleft == null) {
			lleft = Collections.emptyMap();
		}
		if (lright == null) {
			lright = Collections.emptyMap();
		}

		Set<K> keys = new HashSet<>(lleft.size() + lright.size());
		keys.addAll(lleft.keySet());
		keys.addAll(lright.keySet());

		Map<K, MapValueComparison> diff = CollectionUtils.createHashMap(lleft.size() + lright.size());

		// Iterate over the keys and do the comparisons
		for (K key : keys) {
			boolean leftHasKey = lleft.containsKey(key);
			boolean rightHasKey = lright.containsKey(key);
			V leftValue = lleft.get(key);
			V rightValue = lright.get(key);
			if (leftHasKey) {
				if (!rightHasKey) {
					diff.put(key, MapValueComparison.LEFT_ONLY);
				} else if (comparator.test(leftValue, rightValue)) {
					diff.put(key, MapValueComparison.EQUAL);
				} else {
					diff.put(key, MapValueComparison.NOT_EQUAL);
				}
			} else if (rightHasKey) {
				if (!leftHasKey) {
					diff.put(key, MapValueComparison.RIGHT_ONLY);
				} else if (comparator.test(leftValue, rightValue)) {
					diff.put(key, MapValueComparison.EQUAL);
				} else {
					diff.put(key, MapValueComparison.NOT_EQUAL);
				}
			} else {
				// How is it here?
			}
		}

		return diff;
	}

	/**
	 * Diffs single value and return the comparison result
	 *
	 * @param o1
	 *            the first value
	 * @param o2
	 *            the second value
	 * @return the value comparison result
	 */
	public static MapValueComparison diffValues(Object o1, Object o2) {
		if (o1 != null) {
			if (o2 == null) {
				return MapValueComparison.LEFT_ONLY;
			} else if (nullSafeEquals(o1, o2)) {
				return MapValueComparison.EQUAL;
			} else {
				return MapValueComparison.NOT_EQUAL;
			}
		} else if (o2 != null) {
			return MapValueComparison.RIGHT_ONLY;
		}
		return null;
	}

	/**
	 * Differentiate collections and return the elements that are only in the first and in the second collection. Note
	 * that for this method uses the collection elements' {@link #equals(Object)} and {@link #hashCode()} methods.
	 *
	 * @param <E>
	 *            the collection type
	 * @param left
	 *            the left side of the comparison
	 * @param right
	 *            the right side of the comparison
	 * @return the pair that has as first item the elements of the first collection and as second the one that are only
	 *         in the second collection
	 */
	public static <E> Pair<Set<E>, Set<E>> diffCollections(Collection<E> left, Collection<E> right) {
		Set<E> leftCopy = new HashSet<>(left);
		Set<E> rightCopy = new HashSet<>(right);

		Set<E> common = new HashSet<>();
		Set<E> tmp = new HashSet<>(left);
		tmp.retainAll(right);
		common.addAll(tmp);
		tmp = new HashSet<>(right);
		tmp.retainAll(left);
		common.addAll(tmp);

		leftCopy.removeAll(common);
		rightCopy.removeAll(common);

		return new Pair<>(leftCopy, rightCopy);
	}

	/**
	 * Compare object references based on they have actual value or null. NOTE: If both are <b>NOT</b> null then the
	 * method returns value '2'. If you need only null save compare use the other method
	 * {@link #nullSafeCompare(Comparable, Comparable)}.
	 * <p>
	 * <b>NOTE: </b>The method considers the <code>null</code> values as bigger that the real elements. If used in list
	 * sorting the <code>null</code> elements will appear at the end of the list.
	 *
	 * @param <E>
	 *            the element type
	 * @param o1
	 *            the o1
	 * @param o2
	 *            the o2
	 * @return the int
	 */
	public static <E> int nullCompare(E o1, E o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			}
			return 1;
		}
		if (o2 == null) {
			return -1;
		}
		return 2;
	}

	/**
	 * Compare object references based on they have actual value or null. If both are <b>NOT</b> null then the method
	 * will use their compareTo() method.
	 * <p>
	 * <b>NOTE: </b>The method considers the <code>null</code> values as bigger that the real elements. If used in list
	 * sorting the <code>null</code> elements will appear at the end of the list.
	 *
	 * @param <E>
	 *            the {@link Comparable} element type
	 * @param o1
	 *            the o1
	 * @param o2
	 *            the o2
	 * @return the int
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	public static <E extends Comparable<E>> int nullSafeCompare(E o1, E o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			}
			return 1;
		}
		if (o2 == null) {
			return -1;
		}
		return o1.compareTo(o2);
	}

	/**
	 * Returns the default value if the current value is <code>null</code>. With other words tries to return non
	 * <code>null</code>
	 *
	 * @param <T>
	 *            the element type
	 * @param currentValue
	 *            the current value
	 * @param defaultValue
	 *            the default value
	 * @return the first value if non <code>null</code> or the second if the first is <code>null</code>
	 */
	public static <T> T getOrDefault(T currentValue, T defaultValue) {
		return currentValue == null ? defaultValue : currentValue;
	}

	/**
	 * Checks for not null property resolved via the provided property mapper function.
	 * <p>
	 * Typical use is for implementing filtering based on a property of a class
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param propertyMapper
	 *            the property mapper
	 * @return the predicate
	 */
	public static <T, R> Predicate<T> hasNotNullProperty(Function<T, R> propertyMapper) {
		Objects.requireNonNull(propertyMapper, "Invalid property mapper");
		return value -> !Objects.isNull(propertyMapper.apply(value));
	}

	/**
	 * Checks for null property resolved via the provided property mapper function.
	 * <p>
	 * Typical use is for implementing filtering based on a property of a class
	 *
	 * @param <T>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param propertyMapper
	 *            the property mapper
	 * @return the predicate that checks if the returned value from the function is <code>null</code>
	 */
	public static <T, R> Predicate<T> hasNullProperty(Function<T, R> propertyMapper) {
		Objects.requireNonNull(propertyMapper, "Invalid property mapper");
		return value -> Objects.isNull(propertyMapper.apply(value));
	}

}
