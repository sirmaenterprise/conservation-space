package com.sirma.itt.emf.audit.command;

import java.util.List;

import javax.inject.Inject;

import com.sirma.itt.emf.audit.activity.AuditActivity;
import com.sirma.itt.emf.instance.model.CommonInstance;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.search.SearchService;
import com.sirma.itt.emf.search.model.SearchArguments;
import com.sirma.itt.semantic.search.SemanticQueries;

/**
 * Collects the context of given object from the semantic DB by its system ID.
 * 
 * @author Mihail Radkov
 */
public class AuditContextCommand {

	@Inject
	private SearchService searchService;

	/**
	 * Executes the command upon the provided audit activity. If the object system ID is null,
	 * nothing is retrieved as context.
	 * 
	 * @param activity
	 *            the audit activity
	 */
	public void execute(AuditActivity activity) {
		if (activity != null) {
			String objectSystemID = activity.getObjectSystemID();
			if (objectSystemID != null) {
				List<CommonInstance> result = getContextInstances(objectSystemID);
				StringBuilder builder = new StringBuilder();
				// TODO: TEST
				for (Instance context : result) {
					// TODO: Necessary ?
					if (context.getId() != null) {
						builder.append(context.getId()).append(";");
					}
				}
				// TODO: Is this performant?
				if (builder.length() > 0) {
					activity.setContext(builder.substring(0, builder.length() - 1));
				}
			}
		}
	}

	/**
	 * Retrieves the context of given object from the semantic DB by its system ID.
	 * 
	 * @param systemID
	 *            the system ID of the object
	 * @return list of objects as the context
	 */
	private List<CommonInstance> getContextInstances(String systemID) {
		SearchArguments<CommonInstance> filter = searchService
				.getFilter(SemanticQueries.QUERY_CONTEXT_FOR_AUDIT_ENTRY.getName(),
						CommonInstance.class, null);
		filter.getArguments().put("objectId", systemID);
		searchService.search(CommonInstance.class, filter);
		return filter.getResult();
	}

}
