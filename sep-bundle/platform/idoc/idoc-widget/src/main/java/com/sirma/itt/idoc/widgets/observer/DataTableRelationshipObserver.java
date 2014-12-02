package com.sirma.itt.idoc.widgets.observer;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.idoc.web.events.CreateRelationshipFromWidgetEvent;
import com.sirma.itt.idoc.web.events.Widget;

/**
 * Handles the {@link CreateRelationshipFromWidgetEvent} for a datatable widget.
 * 
 * @author yasko
 */
@ApplicationScoped
public class DataTableRelationshipObserver extends BaseWidgetRelationshipObserver {

	private static final Logger LOGGER = Logger.getLogger(DataTableRelationshipObserver.class);

	/**
	 * Observer for the {@link CreateRelationshipFromWidgetEvent} triggered for a datatable widget.
	 * This method checks if there are manually selected objects and if there are it creates a
	 * 'references' relationship.
	 * 
	 * @param event
	 *            Event payload.
	 * @throws JSONException
	 *             Error while retrieving data from the json config.
	 */
	public void handleDataTableRelationships(
			@Observes @Widget(name = "datatable") CreateRelationshipFromWidgetEvent event)
			throws JSONException {

		if (event.getWidgetValue().has("manuallySelectedObjects")) {
			JSONArray manuallySelected = event.getWidgetValue().getJSONArray(
					"manuallySelectedObjects");
			int length = manuallySelected.length();

			List<InstanceReference> idTypePairs = new ArrayList<>(length);
			for (int i = 0; i < length; i++) {
				JSONObject item = manuallySelected.getJSONObject(i);
				String type = JsonUtil.getStringValue(item, "type");
				String dbId = JsonUtil.getStringValue(item, "dbId");
				if ((type == null) || (dbId == null)) {
					LOGGER.warn("Missing data passed to method handleDataTableRelationships: "
							+ item);
					continue;
				}

				if (dbId.equals(event.getFrom().getIdentifier())) {
					continue;
				}
				if ((event.getNewLinkedInstances() == null)
						|| event.getNewLinkedInstances().contains(dbId)) {
					idTypePairs.add(getInstanceReference(type, dbId));
				}
			}

			createObjectReferencesLink(event.getFrom(), idTypePairs);
		}
	}
}
