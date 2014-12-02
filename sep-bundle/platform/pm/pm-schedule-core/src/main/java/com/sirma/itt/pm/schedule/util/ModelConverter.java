package com.sirma.itt.pm.schedule.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistService;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterUtil;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.Node;
import com.sirma.itt.emf.domain.model.TreeNode;
import com.sirma.itt.emf.evaluation.ExpressionContext;
import com.sirma.itt.emf.evaluation.ExpressionContextProperties;
import com.sirma.itt.emf.evaluation.ExpressionsManager;
import com.sirma.itt.emf.exceptions.EmfRuntimeException;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.resources.model.ResourceRole;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.JsonUtil;
import com.sirma.itt.pm.schedule.model.ScheduleAssignment;
import com.sirma.itt.pm.schedule.model.ScheduleDependency;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleEntryProperties;
import com.sirma.itt.pm.schedule.model.ScheduleResourceRole;

/**
 * Helper class for model conversion to JSON string and in reverse.
 *
 * @author BBonev
 */
public class ModelConverter {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.getLogger(ModelConverter.class);
	/**
	 * Sync properties from the incoming tasks to the result tree
	 *
	 * @param template
	 *            the template
	 * @param toUpdate
	 *            the to update
	 */
	public static void syncProperties(Map<Long, JSONObject> template, JSONArray toUpdate) {
		for (int i = 0; i < toUpdate.length(); i++) {
			try {
				JSONObject object = toUpdate.getJSONObject(i);
				Long id = JsonUtil.getLongValue(object, ScheduleEntryProperties.ID);
				JSONObject source = template.get(id);
				if (source != null) {
					JsonUtil.copyMissingProperties(source, object);
				}
			} catch (JSONException e) {
				LOGGER.warn("syncProperties: " + e.getMessage());
			}

		}
	}

	/**
	 * Builds the assignment store.
	 *
	 * @param <A>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param assignments
	 *            the assignments
	 * @param resources
	 *            the resources
	 * @return the string
	 */
	public static <A extends ScheduleAssignment, R extends ResourceRole> JSONObject buildAssignmentStore(
			List<A> assignments, List<R> resources) {
		JSONObject store = new JSONObject();
		JsonUtil.addToJson(store, "assignments", new JSONArray(TypeConverterUtil.getConverter()
				.convert(JSONObject.class, assignments)));
		JsonUtil.addToJson(store, "resources", new JSONArray(TypeConverterUtil.getConverter()
				.convert(JSONObject.class, resources)));
		return store;
	}

	/**
	 * Builds the assignment store.
	 *
	 * @param <A>
	 *            the generic type
	 * @param <R>
	 *            the generic type
	 * @param assignments
	 *            the assignments
	 * @param resources
	 *            the resources
	 * @return the string
	 */
	public static <A extends ScheduleAssignment, R extends ScheduleResourceRole> JSONObject buildScheduleAssignmentStore(
			List<A> assignments, List<R> resources) {
		JSONObject store = new JSONObject();
		JsonUtil.addToJson(store, "assignments", new JSONArray(TypeConverterUtil.getConverter()
				.convert(JSONObject.class, assignments)));
		JsonUtil.addToJson(store, "resources", new JSONArray(TypeConverterUtil.getConverter()
				.convert(JSONObject.class, resources)));
		return store;
	}

	/**
	 * Builds the tasks store.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <I>
	 *            the generic type
	 * @param entries
	 *            the entries
	 * @param converter
	 *            the converter
	 * @return the jSON object
	 */
	public static <T extends TreeNode<I>, I> JSONArray buildTaskStore(List<T> entries,
			TypeConverter converter) {
		return JsonUtil.buildTree(entries, converter);
	}

	/**
	 * Builds the task store as a copy of the given json array template.
	 *
	 * @param <T>
	 *            the generic type
	 * @param <I>
	 *            the generic type
	 * @param template
	 *            the template
	 * @param entries
	 *            the entries
	 * @param converter
	 *            the converter
	 * @return the jSON array
	 */
	public static <T extends TreeNode<I>, I> JSONArray buildTaskStoreAs(JSONArray template,
			List<T> entries, TypeConverter converter) {
		return JsonUtil.buildTreeAs(template, entries, converter);
	}

	/**
	 * Builds the dependency store.
	 *
	 * @param <D>
	 *            the generic type
	 * @param dependencies
	 *            the dependencies
	 * @return the JSON representation of the store
	 */
	public static <D extends ScheduleDependency> JSONArray buildDependencyStore(List<D> dependencies) {
		return new JSONArray(TypeConverterUtil.getConverter().convert(JSONObject.class,
				dependencies));
	}

	/**
	 * Builds the allowed children store.
	 *
	 * @param allowedChildren
	 *            the allowed children
	 * @param manager
	 *            the manager
	 * @param codelistService
	 *            the codelist service
	 * @param language
	 *            the language
	 * @return the allowed children store
	 */
	public static JSONObject buildAllowedChildrenStore(
			Map<String, List<DefinitionModel>> allowedChildren, ExpressionsManager manager,
			CodelistService codelistService, String language) {
		JSONObject store = new JSONObject();
		JSONObject deinitionMapping = new JSONObject();
		for (Entry<String, List<DefinitionModel>> entry : allowedChildren.entrySet()) {
			if (entry.getValue().isEmpty()) {
				continue;
			}
			JSONArray definitions = new JSONArray();
			ExpressionContext context = manager.createDefaultContext(null, null, null);
			for (DefinitionModel model : entry.getValue()) {
				String identifier = model.getIdentifier();
				String description = identifier;
				Node node = model.getChild(DefaultProperties.TYPE);
				if (node != null) {
					if (node instanceof PropertyDefinition) {
						PropertyDefinition property = (PropertyDefinition) node;
						context.put(ExpressionContextProperties.TARGET_FIELD, (Serializable) property);
						Serializable evaluate = manager.evaluate(property, context);
						if (evaluate != null) {
							if (property.getCodelist() != null) {
								String value = codelistService.getDescription(
										property.getCodelist(), evaluate.toString(), language);
								if (StringUtils.isNotNullOrEmpty(value)) {
									description = value;
								}
							} else {
								description = evaluate.toString();
							}
						}
					}
				}
				try {
					// build single element for the definition Id
					JSONObject definitionIDMapping = new JSONObject();
					definitionIDMapping.put("type", identifier);
					definitionIDMapping.put("name", description);
					definitions.put(definitionIDMapping);
				} catch (JSONException e) {
					throw new EmfRuntimeException("Invalid ");
				}
			}
			String typeId = getTypeId(entry.getKey());
			String displayName = entry.getKey();
			// append new element for the EntityType
			JSONObject entryMapping = new JSONObject();
			JsonUtil.addToJson(entryMapping, "type", typeId);
			JsonUtil.addToJson(entryMapping, "name", displayName);
			JsonUtil.append(store, ScheduleEntryProperties.ENTRY_TYPE, entryMapping);

			// add to definition mapping
			JsonUtil.addToJson(deinitionMapping, typeId, definitions);
		}

		// put all definition mappings into the result store
		JsonUtil.addToJson(store, ScheduleEntryProperties.TYPE, deinitionMapping);

		return store;
	}

	/**
	 * Builds the actions.
	 *
	 * @param <A>
	 *            the action type
	 * @param entry
	 *            the entry
	 * @param actions
	 *            the actions
	 * @return the jSON object
	 */
	public static <A extends Action> JSONObject buildActions(ScheduleEntry entry, Set<A> actions) {
		JSONObject object = new JSONObject();
		JSONObject actionsData = new JSONObject();
		for (A action : actions) {
			JSONObject jsonObject = TypeConverterUtil.getConverter().convert(JSONObject.class, action);
			JsonUtil.addToJson(actionsData, action.getActionId(), JsonUtil.getValueOrNull(jsonObject, action.getActionId()));
		}
		Object id = entry.getId();
		if (id == null) {
			id = entry.getPhantomId();
		}
		if (id == null) {
			throw new EmfRuntimeException("Cannot build actions model: Missing Identifiers");
		}
		JsonUtil.addToJson(object, id.toString(), actionsData);
		return object;
	}

	/**
	 * Gets the type id.
	 *
	 * @param key
	 *            the key
	 * @return the type id
	 */
	private static String getTypeId(String key) {
		if (EqualsHelper.nullSafeEquals(key, "case", true)) {
			return "caseinstance";
		} else if (EqualsHelper.nullSafeEquals(key, "workflow", true)) {
			return "workflowinstancecontext";
		} else if (EqualsHelper.nullSafeEquals(key, "task", true)) {
			return "standalonetaskinstance";
		}
		return key;
	}
}
