package com.sirma.sep.xml;

/**
 * Defines a XML schema provider for particular type of XML to validate
 *
 * @author BBonev
 */
public interface XmlSchemaProvider {

	/**
	 * Gets the identifier for the current provider. The provider will be identified by this ID.
	 *
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Gets the main schema file location. Required method. This method should never return <code>null</code>.
	 *
	 * @return the main schema file location
	 */
	SchemaInstance getMainSchemaFileLocation();

	/**
	 * Gets the additional schema locations. If no additional schemas are present then the method should return
	 * <code>null</code>.
	 *
	 * @return the additional schema locations
	 */
	SchemaInstance[] getAdditionalSchemaLocations();

	/**
	 * Creates instance using the given parameters. The returned implementation just stores the given arguments and
	 * provides access to them via the interface methods.
	 *
	 * @param name
	 *            the name
	 * @param mainSchema
	 *            the main schema
	 * @param additional
	 *            the additional
	 * @return the xml schema provider
	 */
	static XmlSchemaProvider create(String name, SchemaInstance mainSchema, SchemaInstance... additional) {
		return new BaseSchemaProvider(name, mainSchema, additional);
	}

	/**
	 * Default implementation of {@link XmlSchemaProvider} that just store given {@link SchemaInstance}es.
	 *
	 * @author BBonev
	 */
	class BaseSchemaProvider implements XmlSchemaProvider {

		private final String id;
		private final SchemaInstance mainSchema;
		private final SchemaInstance[] additional;

		/**
		 * Instantiates a new base schema provider.
		 *
		 * @param name
		 *            the name
		 * @param mainSchema
		 *            the main schema
		 * @param additional
		 *            the additional
		 */
		public BaseSchemaProvider(String name, SchemaInstance mainSchema, SchemaInstance... additional) {
			id = name;
			this.mainSchema = mainSchema;
			this.additional = additional;
		}

		@Override
		public String getIdentifier() {
			return id;
		}

		@Override
		public SchemaInstance getMainSchemaFileLocation() {
			return mainSchema;
		}

		@Override
		public SchemaInstance[] getAdditionalSchemaLocations() {
			return additional;
		}

		@Override
		public String toString() {
			return new StringBuilder(32).append("SchemaType[").append(id).append("]").toString();
		}

	}
}
