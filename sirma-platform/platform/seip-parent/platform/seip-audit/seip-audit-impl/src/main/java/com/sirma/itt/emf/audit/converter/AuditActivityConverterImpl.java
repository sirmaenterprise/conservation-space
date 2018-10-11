package com.sirma.itt.emf.audit.converter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.audit.command.AuditCommand;
import com.sirma.itt.emf.audit.command.AuditContext;
import com.sirma.itt.emf.label.retrieve.FieldId;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverParameters;
import com.sirma.itt.emf.label.retrieve.FieldValueRetrieverService;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.search.SearchRequest;
import com.sirma.itt.seip.plugin.ExtensionPoint;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * <p>
 * Converts {@link AuditActivity} objects to a more user-friendly (readable) version of them by invoking of the
 * available injected {@link AuditCommand#assignLabel(AuditActivity, com.sirma.itt.emf.audit.command.AuditContext)} .
 * </p>
 * <p>
 * The converting includes collecting bread crumb and compact headers from Solr, so the commands can be able to assign
 * headers to the activity's title and context.
 * </p>
 *
 * @author nvelkov
 * @author Mihail Radkov
 */
@ApplicationScoped
public class AuditActivityConverterImpl implements AuditActivityConverter {

	@Inject
	private FieldValueRetrieverService retriever;

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Inject
	@ExtensionPoint(AuditCommand.TARGET_NAME)
	private Iterable<AuditCommand> auditCommands;

	private static final String CONTEXT_SEPARATOR = ";";

	@Override
	public void convertActivities(List<AuditActivity> activities) {
		Map<String, String> objectHeaders = getObjectsHeaders(activities);

		AuditContext context = new AuditContext(objectHeaders);

		for (ListIterator<AuditActivity> iter = activities.listIterator(); iter.hasNext();) {
			AuditActivity activityCopy = new AuditActivity(iter.next());

			for (AuditCommand auditCommand : auditCommands) {
				auditCommand.assignLabel(activityCopy, context);
			}

			iter.set(activityCopy);
		}
	}

	/**
	 * Retrieves the breadcrumb headers for the object titles in the provided activities.
	 *
	 * @param activities
	 *            - the provided activities
	 * @return the mapping between instance id and header
	 */
	private Map<String, String> getObjectsHeaders(List<AuditActivity> activities) {
		Set<String> identifiers = new HashSet<>(activities.size());

		for (AuditActivity activity : activities) {

			// Add the object ids from the activities.
			if (StringUtils.isNotBlank(activity.getObjectSystemID())) {
				String id = activity.getObjectSystemID().trim();
				String fullUri = namespaceRegistryService.buildFullUri(id);
				identifiers.add(fullUri);
			}
			// Add the object ids from the context field.
			if (StringUtils.isNotBlank(activity.getContext())) {
				String[] contextIds = activity.getContext().split(CONTEXT_SEPARATOR);
				for (String contextId : contextIds) {
					identifiers.add(namespaceRegistryService.buildFullUri(contextId));
				}
			}
		}

		return getInstanceHeaders(identifiers, DefaultProperties.TITLE);
	}

	/**
	 * Calls the retrieved service to return the specific headers for the provided instance IDs
	 *
	 * @param instanceIds
	 *            - the provided instance IDs
	 * @param header
	 *            - the specific header
	 * @return the mapping between instance id and header
	 */
	private Map<String, String> getInstanceHeaders(Set<String> instanceIds, String header) {
		if (CollectionUtils.isEmpty(instanceIds)) {
			return CollectionUtils.emptyMap();
		}

		SearchRequest request = new SearchRequest();
		request.setRequest(new HashMap<String, List<String>>(2));
		request.add(FieldValueRetrieverParameters.FIELD, header);

		String[] idsArray = CollectionUtils.toArray(instanceIds, String.class);
		return retriever.getLabels(FieldId.HEADER, idsArray, request);
	}
}
