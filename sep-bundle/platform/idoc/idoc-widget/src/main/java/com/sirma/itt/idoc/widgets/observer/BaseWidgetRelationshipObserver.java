package com.sirma.itt.idoc.widgets.observer;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.instance.dao.ServiceRegister;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.link.LinkConstants;
import com.sirma.itt.emf.link.LinkService;

/**
 * Contains common methods for observers that create relationships based on available widgets.
 * 
 * @author yasko
 */
public abstract class BaseWidgetRelationshipObserver {

	@Inject
	private LinkService linkService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ServiceRegister serviceRegister;

	@Inject
	protected TypeConverter typeConverter;

	/**
	 * Creates a 'references' relationship.
	 * 
	 * @param from
	 *            From object.
	 * @param idTypePairs
	 *            id and type of the objects that need to be loaded and referenced.
	 */
	protected void createObjectReferencesLink(InstanceReference from,
			List<InstanceReference> idTypePairs) {
		for (InstanceReference to : idTypePairs) {
			if (!linkService.isLinked(from, to, LinkConstants.REFERENCES_URI)) {
				linkService.link(from, to, LinkConstants.REFERENCES_URI,
						LinkConstants.REFERENCES_URI, LinkConstants.DEFAULT_SYSTEM_PROPERTIES);
			}
		}
	}

	/**
	 * Gets the {@link DataTypeDefinition} from the simple class name to lower case.
	 * 
	 * @param type
	 *            Simple java class name to lower case.
	 * @return {@link DataTypeDefinition}
	 */
	protected DataTypeDefinition getDataTypeDefinition(String type) {
		return dictionaryService.getDataTypeDefinition(type);
	}

	/**
	 * Converts a object id to the type expected by the service.
	 * 
	 * @param id
	 *            Object to convert.
	 * @param instanceClass
	 *            Instance class.
	 * @return The converted serializable id.
	 */
	protected Serializable convertInstanceId(Object id, Class<?> instanceClass) {
		return TypeConverterUtil.getConverter().convert(
				serviceRegister.getInstanceDao(instanceClass).getPrimaryIdType(), id);
	}

	/**
	 * Creates a pair instance java class and converted identifier.
	 * 
	 * @param type
	 *            Simple class name to lower case.
	 * @param id
	 *            Id as string.
	 * @return the created instance reference
	 */
	protected InstanceReference getInstanceReference(String type, String id) {
		InstanceReference reference = typeConverter.convert(InstanceReference.class, type);
		reference.setIdentifier(id);
		return reference;
	}
}
