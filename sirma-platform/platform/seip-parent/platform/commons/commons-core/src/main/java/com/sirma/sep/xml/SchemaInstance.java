/**
 *
 */
package com.sirma.sep.xml;

import java.io.InputStream;

/**
 * Represents an information for single schema. Provides means of accessing the schema and it's name (optional).
 *
 * @author BBonev
 */
public interface SchemaInstance {

	/**
	 * Gets the schema name.
	 *
	 * @return the name or <code>null</code>if not supported
	 */
	String getName();

	/**
	 * Gets the schema stream
	 *
	 * @return the schema
	 */
	InputStream getSchema();

	/**
	 * Creates a {@link SchemaInstance} that will load a schema file that is loadable as Java resource.
	 *
	 * @param sourceReference
	 *            the source reference class that should be used for the loading
	 * @param path
	 *            the path to load relative to the given class
	 * @return the schema instance
	 */
	static SchemaInstance fromResource(Class<?> sourceReference, String path) {
		return new ResourceSchemaInstance(sourceReference, path);
	}

	/**
	 * {@link SchemaInstance} implementation that can load a resource schema file
	 *
	 * @author BBonev
	 */
	class ResourceSchemaInstance implements SchemaInstance {

		private final Class<?> sourceReference;
		private final String name;

		/**
		 * Instantiates a new resource schema instance.
		 *
		 * @param sourceReference
		 *            the source reference
		 * @param name
		 *            the name
		 */
		public ResourceSchemaInstance(Class<?> sourceReference, String name) {
			this.sourceReference = sourceReference;
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public InputStream getSchema() {
			InputStream resource = sourceReference.getResourceAsStream(getName());
			if (resource == null) {
				resource = sourceReference.getClassLoader().getResourceAsStream(getName());
			}
			return resource;
		}

		@Override
		public String toString() {
			return new StringBuilder(32).append("SchemaLocation[").append(name).append("]").toString();
		}
	}
}
