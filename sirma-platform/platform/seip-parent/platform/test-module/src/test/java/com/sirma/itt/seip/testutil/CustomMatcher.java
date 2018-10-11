package com.sirma.itt.seip.testutil;

import java.util.function.Consumer;
import java.util.function.Predicate;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.mockito.Mockito;

/**
 * Custom matcher implementation that can be used for custom validations in
 * {@link Mockito#argThat(org.hamcrest.Matcher)}
 *
 * @param <T>
 *            the arg type
 * @author BBonev
 */
public class CustomMatcher<T> extends BaseMatcher<T> {

	private Predicate<T> predicate;
	private String message;

	/**
	 * Instantiates a new custom matcher.
	 *
	 * @param predicate
	 *            the predicate
	 */
	public CustomMatcher(Predicate<T> predicate) {
		this.predicate = predicate;
	}

	/**
	 * Instantiates a new custom matcher.
	 *
	 * @param predicate
	 *            the predicate
	 * @param message
	 *            the message
	 */
	public CustomMatcher(Predicate<T> predicate, String message) {
		this.predicate = predicate;
		this.message = message;
	}

	/**
	 * Creates new custom matcher from the given consumer. The matcher will always return <code>true</code>. This is
	 * useful when the matching is the in the consumer/predicate with assert and not just checks.
	 *
	 * <pre>
	 * <code>
	 *verify(service).save(argThat(CustomMatcher.of(instance -> assertNonNull(instance.getId()))), any(Operation.class));
	 * </code>
	 * </pre>
	 *
	 * @param <T>
	 *            the generic type
	 * @param consumer
	 *            the predicate
	 * @return the custom matcher
	 */
	public static <T> CustomMatcher<T> of(Consumer<T> consumer) {
		return new CustomMatcher<>(arg -> {
			consumer.accept(arg);
			return true;
		});
	}

	/**
	 * Creates new custom matcher from the given predicate
	 *
	 * @param <T>
	 *            the generic type
	 * @param predicate
	 *            the predicate
	 * @return the custom matcher
	 */
	public static <T> CustomMatcher<T> ofPredicate(Predicate<T> predicate) {
		return new CustomMatcher<>(predicate);
	}

	/**
	 * Creates new custom matcher from the given predicate
	 *
	 * @param <T>
	 *            the generic type
	 * @param predicate
	 *            the predicate
	 * @return the custom matcher
	 * @deprecated better use {@link #ofPredicate(Predicate)} as this method has method collision with {@link #of(Consumer)}
	 */
	@Deprecated
	public static <T> CustomMatcher<T> of(Predicate<T> predicate) {
		return ofPredicate(predicate);
	}

	/**
	 * Creates new custom matcher from the given predicate and message
	 *
	 * @param <T>
	 *            the generic type
	 * @param predicate
	 *            the predicate
	 * @param message
	 *            the message
	 * @return the custom matcher
	 */
	public static <T> CustomMatcher<T> of(Predicate<T> predicate, String message) {
		return new CustomMatcher<>(predicate, message);
	}

	/**
	 * Matches.
	 *
	 * @param item
	 *            the item
	 * @return true, if successful
	 */
	@Override
	@SuppressWarnings("unchecked")
	public boolean matches(Object item) {
		return predicate.test((T) item);
	}

	/**
	 * Describe to.
	 *
	 * @param description
	 *            the description
	 */
	@Override
	public void describeTo(Description description) {
		if (message != null) {
			description.appendText(message);
		} else {
			description.appendText("the custom match failed");
		}
	}

}
