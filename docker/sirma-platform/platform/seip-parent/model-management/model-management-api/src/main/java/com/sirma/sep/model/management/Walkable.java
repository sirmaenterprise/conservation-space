package com.sirma.sep.model.management;

/**
 * Defines a way for the model to be iterated using {@code name=value} pairs in the form of Path nodes.
 * After resolving the desired direction of the path the implementations should call {@link Path#proceed(Object)} with
 * the result of the step. If the given result is of type {@link Walkable} and there are more steps then walking will
 * continue until the path is resolved or the model returns non Walkable item
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 27/07/2018
 */
public interface Walkable {

	/**
	 * Process path step. If the step is resolved then the implementation should call it's {@link Path#proceed(Object) proceed} method. <br>
	 * Example implementation:
	 * <pre><code>
	 *     public Object walk(Path step) {
	 *         if (step.getName().equals(...)) {
	 *             return step.proceed(resolveData(step.getValue()));
	 *         }
	 *         throw new IllegalArgumentException();
	 *     }
	 * </code></pre>
	 *
	 * @param step the step to process
	 * @return the resolved element at the end of the path.
	 */
	Object walk(Path step);

	/**
	 * Parses the given path and walks to the given node if any.
	 *
	 * @param path the path to walk to select the given node
	 * @return the selected node
	 */
	default Object select(String path) {
		return Path.parsePath(path).walk(this);
	}
}
