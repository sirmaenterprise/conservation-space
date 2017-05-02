package com.sirma.itt.seip.definition.compile;

import java.util.Collection;
import java.util.List;

import com.sirma.itt.seip.annotation.Documentation;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.plugin.Plugin;

/**
 * Callback interface needed when compiling concrete definitions.
 *
 * @param <V>
 *            the concrete definition type
 */
@Documentation("Callback interface needed when compiling concrete definitions.")
public interface DefinitionCompilerCallback<V extends TopLevelDefinition> extends Plugin {

	String TARGET_NAME = "definitionCompilerCallback";

	/**
	 * Warm up cache.
	 */
	void warmUpCache();

	/**
	 * Gets the callback name.
	 *
	 * @return the callback name
	 */
	String getCallbackName();

	/**
	 * Gets the definition class.
	 *
	 * @return the definition class
	 */
	Class<V> getDefinitionClass();

	/**
	 * Gets the mapping class.
	 *
	 * @return the mapping class
	 */
	Class<?> getMappingClass();

	/**
	 * Gets the xml validation type.
	 *
	 * @return the xml validation type
	 */
	XmlSchemaProvider getXmlValidationType();

	/**
	 * Gets the definitions.
	 *
	 * @return the definitions
	 */
	List<FileDescriptor> getDefinitions();

	/**
	 * Sets the property revision.
	 *
	 * @param definition
	 *            the new property revision
	 */
	void setPropertyRevision(V definition);

	/**
	 * Normalize fields.
	 *
	 * @param definition
	 *            the definition
	 */
	void normalizeFields(V definition);

	/**
	 * Extract definition id.
	 *
	 * @param definition
	 *            the definition
	 * @return the string
	 */
	String extractDefinitionId(V definition);

	/**
	 * Update references.
	 *
	 * @param definition
	 *            the definition
	 * @param resolver
	 *            the resolver
	 * @return true, if successful
	 */
	boolean updateReferences(V definition, DefinitionReferenceResolver resolver);

	/**
	 * Gets the label definitions.
	 *
	 * @param definition
	 *            the definition
	 * @return the label definitions
	 */
	List<?> getLabelDefinitions(Object definition);

	/**
	 * Gets the filter definitions from template JaxB file.
	 *
	 * @param source
	 *            the source
	 * @return the filter definitions
	 */
	List<?> getFilterDefinitions(Object source);

	/**
	 * Save template to the DB.
	 *
	 * @param definition
	 *            the definition
	 * @return the v
	 */
	V saveTemplate(V definition);

	/**
	 * Find template in system's DB.
	 *
	 * @param identifier
	 *            the identifier
	 * @return the v
	 */
	V findTemplateInSystem(String identifier);

	/**
	 * Save template properties for the given new definition. The second argument is the reference definition that is in
	 * DB if any.
	 *
	 * @param newDefinition
	 *            the new definition
	 * @param oldDefinition
	 *            the old definition, if missing this should be <code>null</code>.
	 */
	void saveTemplateProperties(V newDefinition, V oldDefinition);

	/**
	 * Called just before persisting.
	 *
	 * @param definition
	 *            the definition
	 */
	void prepareForPersist(V definition);

	/**
	 * Validate compiled definition.
	 *
	 * @param definition
	 *            the definition
	 * @return true, if valid
	 */
	boolean validateCompiledDefinition(V definition);

	/**
	 * When the method is called the given callback should know that is called from other callback.
	 */
	void setReferenceMode();

	/**
	 * Gets the dependencies for the given definition they could be parent or reference. These dependencies should be
	 * only connected to the current instance and not to some child definitions. The return id should be only if the
	 * dependent definition is from the same type that has been processed.
	 *
	 * @param value
	 *            the value
	 * @return the dependencies
	 */
	Collection<String> getDependencies(V value);

	/**
	 * Resolver to be used when resolving reference definitions. It's mostly useful when resolving definitions from the
	 * same type currently compiling.
	 */
	interface DefinitionReferenceResolver {

		/**
		 * Resolve definition from the given type and identifier.
		 *
		 * @param <V>
		 *            the desired type to resolve
		 * @param type
		 *            the type
		 * @param identifier
		 *            the identifier
		 * @return the resolved definition or <code>null</code> if not found in the currently processed or in the
		 *         system.
		 */
		<V extends TopLevelDefinition> V resolve(Class<V> type, String identifier);
	}
}