package com.sirma.itt.emf.audit.converter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * Converts {@link AuditActivity} objects to a more user-friendly (readable) version of them
 * 
 * @author nvelkov
 */
@ApplicationScoped
public class AuditActivityConverterImpl implements AuditActivityConverter {
	@Inject
	private FieldValueRetrieverService retriever;
	@Inject
	private DictionaryService dictionaryService;
	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public void convertActivity(AuditActivity activity) {
		String objectType = activity.getObjectType();
		String objectSubType = activity.getObjectSubType();
		String objectTitle = activity.getObjectTitle();
		String objectPreviousState = activity.getObjectPreviousState();
		String username = activity.getUserName();
		String actionId = activity.getActionID();
		// String context = activity.getContext();
		activity.setObjectTypeLabel(retriever.getLabel(FieldId.OBJECTTYPE, objectType));
		activity.setObjectSubTypeLabel(retriever.getLabel(FieldId.OBJECTSUBTYPE, objectSubType,
				objectType));
		activity.setUserDisplayName(retriever.getLabel(FieldId.USERNAME, username));
		activity.setAction(retriever.getLabel(FieldId.ACTIONID, actionId));

		if (objectType != null && !"".equals(objectType)) {
			activity.setObjectStateLabel(retriever.getLabel(FieldId.OBJECTSTATE,
					activity.getObjectState(), namespaceRegistryService.buildFullUri(objectType)));
			if (objectPreviousState != null && !"".equals(objectPreviousState)) {
				activity.setObjectPreviousStateLabel(retriever.getLabel(FieldId.OBJECTSTATE,
						activity.getObjectPreviousState(),
						namespaceRegistryService.buildFullUri(objectType)));
			}
		}
		if ((objectTitle != null) && !"".equals(objectTitle)) {
			String fullURI = namespaceRegistryService.buildFullUri(activity.getObjectType());
			activity.setObjectInstanceType(dictionaryService.getDataTypeDefinition(fullURI)
					.getName());
		}
	}

}
