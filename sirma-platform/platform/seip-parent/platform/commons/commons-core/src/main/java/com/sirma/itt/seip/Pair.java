package com.sirma.itt.seip;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Utility class for containing two things that aren't like each other.
 *
 * @param <F>
 *            the generic type
 * @param <S>
 *            the generic type
 * @author BBonev
 */
public class Pair<F, S> implements Serializable, Copyable<Pair<F, S>> {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7495037741259710709L;

	/** The Constant NULL_PAIR. */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final Pair NULL_PAIR = new Pair(null, null);

	/**
	 * Null pair.
	 *
	 * @param <X>
	 *            the generic type
	 * @param <Y>
	 *            the generic type
	 * @return the pair
	 */
	public static final <X, Y> Pair<X, Y> nullPair() {
		return NULL_PAIR;
	}

	/**
	 * Returns a collector that produces a map from a stream of {@link Pair}s where map keys are the the elements
	 * returned from {@link Pair#getFirst()}.
	 *
	 * @param <X>
	 *            the map key type
	 * @param <Y>
	 *            the map value type
	 * @return the collector
	 */
	public static <X, Y> Collector<Pair<X, Y>, ?, Map<X, Y>> toMap() {
		return Collectors.toMap(Pair::getFirst, Pair::getSecond);
	}

	/**
	 * Returns a predicate that checks if the value returned by {@link #getFirst()} is non <code>null</code>
	 *
	 * @param <X>
	 *            the first value type
	 * @param <Y>
	 *            the second value type
	 * @param <R>
	 *            the pair type type
	 * @return the predicate
	 */
	public static <X, Y, R extends Pair<X, Y>> Predicate<R> nonNullFirst() {
		return (pair) -> pair != null && pair.getFirst() != null;
	}

	/**
	 * Returns a predicate that checks if the value returned by {@link #getSecond()} is non <code>null</code>
	 *
	 * @param <X>
	 *            the first value type
	 * @param <Y>
	 *            the second value type
	 * @param <R>
	 *            the pair type type
	 * @return the predicate
	 */
	public static <X, Y, R extends Pair<X, Y>> Predicate<R> nonNullSecond() {
		return (pair) -> pair != null && pair.getSecond() != null;
	}

	/**
	 * Returns a predicate that checks if the values returned by {@link #getFirst()} and {@link #getSecond()} are non
	 * <code>null</code>
	 *
	 * @param <X>
	 *            the first value type
	 * @param <Y>
	 *            the second value type
	 * @param <R>
	 *            the pair type type
	 * @return the predicate
	 */
	@SuppressWarnings("unchecked")
	public static <X, Y, R extends Pair<X, Y>> Predicate<R> nonNull() {
		return (Predicate<R>) nonNullFirst().and(nonNullSecond());
	}

	/**
	 * Create transform that produces a pair by applying the transformers functions given as arguments. The value passed
	 * to the result function will be passed to the two given functions to produce the pair elements.
	 *
	 * @param <T>
	 *            the source argument type
	 * @param <X>
	 *            the pair first type
	 * @param <Y>
	 *            the pair second type
	 * @param first
	 *            the transformer function for the first pair element
	 * @param second
	 *            the transformer function for the second pair element
	 * @return a function that builds pair based on the given transformer functions.
	 */
	public static <T, X, Y> Function<T, Pair<X, Y>> from(Function<T, X> first, Function<T, Y> second) {
		Objects.requireNonNull(first, "First transformer function could not be null");
		Objects.requireNonNull(second, "Second transformer function could not be null");
		return (t) -> new Pair<>(first.apply(t), second.apply(t));
	}

	/**
	 * The first member of the pair.
	 */
	@Tag(10)
	private F first;

	/**
	 * The second member of the pair.
	 */
	@Tag(20)
	private S second;

	/**
	 * Instantiates a new pair.
	 */
	public Pair() {
		this(null, null);
	}

	/**
	 * Make a new one.
	 *
	 * @param first
	 *            The first member.
	 * @param second
	 *            The second member.
	 */
	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Instantiates a new pair by coping the values from the given pair
	 *
	 * @param copyFrom
	 *            the copy from
	 */
	public Pair(Pair<? extends F, ? extends S> copyFrom) {
		this.first = copyFrom.getFirst();
		this.second = copyFrom.getSecond();
	}

	/**
	 * Get the first member of the tuple.
	 *
	 * @return The first member.
	 */
	public F getFirst() {
		return first;
	}

	/**
	 * Get the second member of the tuple.
	 *
	 * @return The second member.
	 */
	public S getSecond() {
		return second;
	}

	/**
	 * Sets the first.
	 *
	 * @param first
	 *            the new first
	 */
	public void setFirst(F first) {
		this.first = first;
	}

	/**
	 * Sets the second.
	 *
	 * @param second
	 *            the new second
	 */
	public void setSecond(S second) {
		this.second = second;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Pair<?, ?>)) {
			return false;
		}
		Pair<?, ?> o = (Pair<?, ?>) other;
		return EqualsHelper.nullSafeEquals(this.first, o.first) && EqualsHelper.nullSafeEquals(this.second, o.second);
	}

	@Override
	public int hashCode() {
		return (first == null ? 0 : first.hashCode()) + (second == null ? 0 : second.hashCode());
	}

	@Override
	public String toString() {
		return new StringBuilder(128).append("(").append(first).append(", ").append(second).append(")").toString();
	}

	@Override
	public Pair<F, S> createCopy() {
		return new Pair<>(getFirst(), getSecond());
	}
}
