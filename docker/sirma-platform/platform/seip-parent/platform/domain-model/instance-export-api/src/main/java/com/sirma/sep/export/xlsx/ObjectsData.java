package com.sirma.sep.export.xlsx;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the information about the objects that should be exported.
 *
 * @author A. Kunchev
 */
public class ObjectsData {

	private final List<String> manuallySelectedObjects;
	private final String instanceHeaderType;
	private final Map<String, List<String>> selectedProperties;
	private final Map<String, Set<String>> selectedSubProperties;

	/**
	 * Instantiates new object data.
	 *
	 * @param manuallySelectedObjects
	 *            contains the ids of the objects that are manually selected to be exported
	 * @param instanceHeaderType
	 *            shows what kind of header should be exported for the exported objects
	 * @param selectedProperties
	 *            contains the properties that should be exported for the objects
	 */
	ObjectsData(final List<String> manuallySelectedObjects, final String instanceHeaderType,
			final Map<String, List<String>> selectedProperties, final Map<String, Set<String>> selectedSubProperties) {
		this.manuallySelectedObjects = manuallySelectedObjects;
		this.instanceHeaderType = instanceHeaderType;
		this.selectedProperties = selectedProperties;
		this.selectedSubProperties = selectedSubProperties;
	}

	/**
	 * Gets the identifiers of the manually selected objects.
	 *
	 * @return instance ids of the selected objects
	 */
	public List<String> getManuallySelectedObjects() {
		return manuallySelectedObjects;
	}

	/**
	 * Gets the type of the instance headers(default, compact, breadcrumb, etc.) that should be displayed.
	 *
	 * @return type of the instance header
	 */
	public String getInstanceHeaderType() {
		return instanceHeaderType;
	}

	/**
	 * Gets map with definition id or semantic class names as keys and values selected properties for them. <br>
	 * For example:
	 *
	 * <pre>
	 *     GEC20001 -> "title", "description"
	 *     ET120001 -> ""
	 *     testImage -> ""
	 * </pre>
	 *
	 * OR
	 *
	 * <pre>
	 *      http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Case -> "type", "identifier"
	 *      http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Image -> "hasParent,
	 *      http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Project -> ""
	 * </pre>
	 *
	 * @return map with key selected definition id or semantic class names and values selected properties for it
	 */
	public Map<String, List<String>> getSelectedProperties() {
		return selectedProperties;
	}

	/**
	 * Gets map with object property id as keys and values selected sub properties. <br>
	 * For example:
	 * 
	 * <pre>
	 *     partOf -> "title", "parentOf"
	 *     createdBy -> "firstName", "lastName"
	 *     partOf -> "title"
	 * </pre>
	 * 
	 * @return map with key selected object property and values selected sub properties for this property
	 */
	public Map<String, Set<String>> getSelectedSubProperties() {
		return selectedSubProperties;
	}
}
