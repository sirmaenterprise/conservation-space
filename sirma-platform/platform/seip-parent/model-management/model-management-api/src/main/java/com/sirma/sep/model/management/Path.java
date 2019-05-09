package com.sirma.sep.model.management;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Copyable;

/**
 * Represents a model path. The Path implementation is double linked list. Once constructed the path could be traversed
 * using the {@link #walk(Walkable)} method.
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/07/2018
 */
public final class Path implements Serializable, Copyable<Path> {
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final Path EMPTY_PATH = new Path("", "");

	private Path next;
	private Path previous;
	private final String name;
	private final String value;

	private Path(String name, String value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Parse a given string path representation into Path instance. The path must comply the format <br>
	 * {@code [/]name=value[/name=value]}
	 *
	 * @param path string to parse
	 * @return the head of the created path
	 */
	public static Path parsePath(String path) {
		String[] pathSteps = path.split("/");
		return Stream.of(pathSteps)
				.filter(StringUtils::isNotBlank)
				.map(parsePathStep())
				.reduce((previous, next) -> {
					if (previous != null) {
						previous.appendInternal(next);
					}
					return next;
				}).orElse(EMPTY_PATH).head();
	}

	/**
	 * Create a path from the given name and value. The value will be properly decoded if needed during path building
	 *
	 * @param name the path name
	 * @param value the path value
	 * @return a path instance consisting of the given path name and value
	 */
	public static Path create(String name, String value) {
		Objects.requireNonNull(name, "Path name cannot be null");
		Objects.requireNonNull(value, "Path value cannot be null");
		return new Path(name, decodeValue(value));
	}

	private Path appendInternal(Path toAdd) {
		if (toAdd != null) {
			toAdd.previous = this;
			this.next = toAdd;
		}
		return toAdd; // return the new tail
	}

	private static Function<String, Path> parsePathStep() {
		return pathStep -> {
			String[] stepParts = pathStep.split("=");
			if (stepParts.length != 2) {
				throw new IllegalArgumentException("Invalid path step " + pathStep);
			}
			return new Path(stepParts[0], decodeValue(stepParts[1]));
		};
	}

	private static String decodeValue(String value) {
		try {
			return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException("Cannot decode path value " + value);
		}
	}

	/**
	 * Checks if rhe path has any steps. A path does not have any steps when initialized from an empty string or only
	 * path separator. Empty path resolves to the first {@link Walkable} or root.
	 *
	 * @return if the path is empty
	 */
	public boolean isEmpty() {
		return next == null && previous == null && name.isEmpty() && value.isEmpty();
	}

	/**
	 * Navigates to the fist step of the path from any of the path steps.
	 *
	 * @return the first element in the path or the current element if we are at the head in the moment
	 */
	public Path head() {
		if (previous != null) {
			return previous.head();
		}
		return this;
	}

	/**
	 * Navigates to the last element of the path from any of the path steps.
	 *
	 * @return the last element in the path or the current element if we are at the tail in the moment
	 */
	public Path tail() {
		if (next != null) {
			return next.tail();
		}
		return this;
	}

	/**
	 * Gets the next step in the path or null if there are at the tail of the path
	 *
	 * @return the next step in the path
	 */
	public Path next() {
		return next;
	}

	/**
	 * Get the previous step in the path or null if we are at the head
	 *
	 * @return the previous step in the path
	 */
	public Path previous() {
		return previous;
	}

	/**
	 * Checks if there is element before the current one.
	 *
	 * @return true if this is not the head of the path
	 */
	public boolean hasPrevious() {
		return previous != null;
	}

	/**
	 * Returns a copy of the current path without the remaining nodes. The tail of the new path will be the current node.
	 * If called on the head of a path will create new path with the same head but without any other elements.
	 *
	 * @return new path without the remaining elements
	 */
	public Path cutOffTail() {
		Path current = head();
		Path newTail = current.copyNode();
		while (current != this && current.hasNext()) {
			current = current.next();
			newTail = newTail.appendInternal(current.copyNode());
		}
		return newTail.head();
	}

	/**
	 * Appends the given path to a copy of the current path. The result is new path that begins with the current and
	 * ends with the given path node. The method does not modify the current path, but rather creates a copy and appends
	 * to it. The returned path points to the head of the new path. The append will happen always at the end of the
	 * current path chain no matter where the current node points. Note that the added path is also copied so it's not
	 * modified in any way.
	 *
	 * @param path the path to append
	 * @return new path that consist of the complete current path and the added given path
	 */
	public Path append(Path path) {
		Path copyToAppend = Objects.requireNonNull(path, "Cannot append null path!").createCopy();
		Path currentCopy = createCopy();
		// go to the end of the path and append new node there and then return the head of the path
		Path newTail = currentCopy.tail().appendInternal(copyToAppend);
		// this could not happen but sonar complains about it somehow
		return Objects.requireNonNull(newTail, "Build new null path tail!").head();
	}

	/**
	 * Checks if there is next element after the current one.
	 *
	 * @return true if this is not the tail of the path.
	 */
	public boolean hasNext() {
		return next != null;
	}

	private Path copyNode() {
		return new Path(getName(), getValue());
	}

	/**
	 * Gets the name of path step (<b>name</b>=value)
	 *
	 * @return the step name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the id of path step (name=<b>value</b>)
	 *
	 * @return the step id
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Walks over the current path using the given {@link Walkable}. If the path is empty the argument will be returned
	 *
	 * @param walkable the walkable instance to travers over.
	 * @return the current walkable of the path is empty or the resolved one after walking the path.
	 */
	public Object walk(Walkable walkable) {
		if (isEmpty()) {
			return walkable;
		}
		return walkable.walk(this);
	}

	/**
	 * Proceed with walking over the current path. There are several outcomes from this method depending on the argument and the path state:
	 * <ul>
	 * <li>If there are more steps and the argument is instance of {@link Walkable} then it's {@link Walkable#walk(Path) walk} method will be invoked with the next step of the path</li>
	 * <li>If there are more steps and the argument is not instance of {@link Walkable} then {@link IllegalStateException} will be thrown as we are not able to complete the path</li>
	 * <li>If there are no more steps then the argument is returned</li>
	 * </ul>
	 *
	 * @param value the value to use to proceed walking or to finish it
	 * @return the last value passed to the {@link #proceed(Object) proceed} method.
	 */
	public Object proceed(Object value) {
		if (next() != null) {
			if (value instanceof Walkable) {
				return ((Walkable) value).walk(next());
			} else {
				throw new IllegalStateException(
						"Cannot continue walking as reached non passable step " + this + " for value: " + value);
			}
		}
		return value;
	}

	/**
	 * Creates new path node that has the same id but value the one passed as argument. Effectively the path could
	 * select different object from the same type. The method does not transfer previous or next nodes.
	 *
	 * @param newValue the value to set for the path
	 * @return new path instance with the specified value.
	 */
	public Path createSimilar(String newValue) {
		return new Path(name, newValue);
	}

	@Override
	public String toString() {
		if (isEmpty()) {
			return "/";
		}
		return "/" + getName() + "=" + getEncodedValue() + (next != null ? next : "");
	}

	/**
	 * Returns the path representation as non encoded string. The result is intented for human reading and may not be
	 * reverse parseable by the {@link #parsePath(String)} if some of the values contains forbidden
	 * symbols like {@code /} or {@code =}.
	 *
	 * @return pretty representation of the current path
	 */
	public String prettyPrint() {
		if (isEmpty()) {
			return "/";
		}
		return "/" + getName() + "=" + getValue() + (next != null ? next : "");
	}

	private String getEncodedValue() {
		try {
			return URLEncoder.encode(getValue(), StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("", e);
		}
		return getValue();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Path)) {
			return false;
		}
		Path path = (Path) o;
		return Objects.equals(name, path.name) &&
				Objects.equals(value, path.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, value);
	}

	/**
	 * Create a deep copy of the given path. The path is copied from the head to tail no matter at what point in the
	 * path the call is made.
	 *
	 * @return the head of the new path
	 */
	@Override
	public Path createCopy() {
		Path current = head();
		Path newTail = current.copyNode();
		while(current.hasNext()) {
			current = current.next();
			newTail = newTail.appendInternal(current.copyNode());
		}
		return newTail.head();
	}
}
