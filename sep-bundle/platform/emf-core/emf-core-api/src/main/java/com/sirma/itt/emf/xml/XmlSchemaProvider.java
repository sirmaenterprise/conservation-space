package com.sirma.itt.emf.xml;

/**
 * Defines a XML schema provider for particular type of XML to validate
 *
 * @author BBonev
 */
public interface XmlSchemaProvider {

	/**
	 * Base class used to determine the schema location returned by the current provider.
	 * 
	 * @return the class
	 */
	Class<?> baseLoaderClass();
	/**
	 * Gets the identifier for the current provider. The provider will be identified by this ID.
	 *
	 * @return the identifier
	 */
	String getIdentifier();

	/**
	 * Gets the main schema file location. Required method. This method should never return
	 * <code>null</code>.
	 *
	 * @return the main schema file location
	 */
	String getMainSchemaFileLocation();

	/**
	 * Gets the additional schema locations. If no additional schemas are present then the method
	 * should return <code>null</code>.
	 *
	 * @return the additional schema locations
	 */
	String[] getAdditionalSchemaLocations();

}
