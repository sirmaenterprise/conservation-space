package com.sirma.itt.emf.semantic.persistence;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.resources.ResourceService;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Utility class to accommodate common logic when implementing  the semantic properties converters.
 *
 * @author BBonev
 */
public class BasePropertiesConverter {

	/** The namespace registry service. */
	@Inject
	protected NamespaceRegistryService namespaceRegistryService;
	/** The resource service. */
	@Inject
	protected ResourceService resourceService;

	/**
	 * Checks if the given property definition points to a field that is a resource field.
	 *
	 * @param field
	 *            the field
	 * @return true, if is resource field
	 */
	public boolean isResourceField(PropertyDefinition field) {
		return (field.getControlDefinition() != null)
				&& (field.getControlDefinition().getIdentifier().equals("USER") || field
						.getControlDefinition().getIdentifier().equals("PICKLIST"));
	}

	/**
	 * Checks if the given field definition is to a field that should be converted to URI
	 *
	 * @param field
	 *            the field
	 * @return true, if is uri field
	 */
	public boolean isUriField(PropertyDefinition field) {
		return (field.getControlDefinition() != null)
				&& field.getControlDefinition().getIdentifier().equals("INSTANCE")
				&& (EqualsHelper.nullSafeEquals(DataTypeDefinition.TEXT, field.getType()) || ((field
						.getDataType() != null) && EqualsHelper.nullSafeEquals(field.getDataType()
						.getName(), DataTypeDefinition.TEXT)));
	}

	/**
	 * Checks if is sub instance field.
	 *
	 * @param field
	 *            the field
	 * @return true, if is sub instance field
	 */
	public boolean isSubInstanceField(PropertyDefinition field) {
		return (field.getControlDefinition() != null)
				&& field.getControlDefinition().getIdentifier().equals("INSTANCE")
				&& (EqualsHelper.nullSafeEquals(DataTypeDefinition.INSTANCE, field.getType()) || ((field
						.getDataType() != null) && EqualsHelper.nullSafeEquals(field.getDataType()
						.getName(), DataTypeDefinition.INSTANCE)));
	}

	/**
	 * Dumps the passed string messages into the log if it is debug enabled.
	 * 
	 * @param log
	 *            the log
	 * @param messages
	 *            the messages
	 */
	protected void debug(Logger log, Object... messages) {
		if (log.isDebugEnabled()) {
			StringBuilder builder = new StringBuilder();
			for (Object message : messages) {
				builder.append(message);
			}
			log.debug(builder.toString());
		}
	}

	/**
	 * Dumps the passed string messages into the log if it is trace enabled.
	 * 
	 * @param log
	 *            the log
	 * @param messages
	 *            the messages
	 */
	protected void trace(Logger log, Object... messages) {
		if (log.isTraceEnabled()) {
			StringBuilder builder = new StringBuilder();
			for (Object message : messages) {
				builder.append(message);
			}
			log.trace(builder.toString());
		}
	}

}
