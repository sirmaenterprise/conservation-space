package com.sirma.itt.seip.testutil.mocks;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.AllowedChildDefinition;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.StateTransition;
import com.sirma.itt.seip.definition.TransitionDefinition;
import com.sirma.itt.seip.definition.TransitionGroupDefinition;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.json.JsonRepresentable;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * The Class DefinitionMock.
 *
 * @author BBonev
 */
public class DefinitionMock implements GenericDefinition, JsonRepresentable {

	private static final long serialVersionUID = 1L;
	private String type = "script";
	protected String identifier;
	protected String expression;
	protected Integer hash;
	protected List<PropertyDefinition> configurations = new LinkedList<>();
	protected List<PropertyDefinition> fields = new LinkedList<>();
	protected List<RegionDefinition> regions = new LinkedList<>();
	private String container;
	private Long revision;
	private List<StateTransition> stateTransitions = new LinkedList<>();
	private List<TransitionDefinition> transitions = new LinkedList<>();
	private List<TransitionGroupDefinition> transitionGroups = new LinkedList<>();
	private List<AllowedChildDefinition> allowedChildren = new LinkedList<>();
	private String purpose;
	private String dmsId;
	private boolean isAstract;

	/**
	 * Default constructor.
	 */
	public DefinitionMock() {
		// empty
	}

	/**
	 * Constructor with identifier.
	 */
	public DefinitionMock(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public List<RegionDefinition> getRegions() {
		return regions;
	}

	/**
	 * Setter method for regions.
	 *
	 * @param regions
	 *            the regions to set
	 */
	public void setRegions(List<RegionDefinition> regions) {
		this.regions = regions;
	}

	@Override
	public boolean hasChildren() {
		return !getFields().isEmpty() || !getRegions().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		return fieldsStream().filter(p -> p.getName().equals(name)).findAny().orElse(null);
	}

	@Override
	public String getParentDefinitionId() {
		return null;
	}

	@Override
	public String getDmsId() {
		return dmsId;
	}

	@Override
	public void setDmsId(String dmsId) {
		this.dmsId = dmsId;
	}

	@Override
	public String getContainer() {
		return container;
	}

	@Override
	public void setContainer(String container) {
		this.container = container;
	}

	@Override
	public boolean isAbstract() {
		return isAstract;
	}

	public void setAbstract(boolean asAbstract) {
		isAstract = asAbstract;
	}

	@Override
	public Long getRevision() {
		return revision;
	}

	@Override
	public void setRevision(Long revision) {
		this.revision = revision;
	}

	@Override
	public List<PropertyDefinition> getFields() {
		return fields;
	}

	public void setFields(List<PropertyDefinition> fields) {
		this.fields= fields;
	}

	@Override
	public Integer getHash() {
		return hash;
	}

	@Override
	public void setHash(Integer hash) {
		this.hash = hash;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public List<StateTransition> getStateTransitions() {
		return stateTransitions;
	}

	@Override
	public List<TransitionDefinition> getTransitions() {
		return transitions;
	}

	@Override
	public List<TransitionGroupDefinition> getTransitionGroups() {
		return transitionGroups;
	}

	public void setTransitionGroups(List<TransitionGroupDefinition> transitionGroups) {
		this.transitionGroups = transitionGroups;
	}

	@Override
	public List<AllowedChildDefinition> getAllowedChildren() {
		return allowedChildren;
	}

	@Override
	public PathElement getParentElement() {
		return null;
	}

	@Override
	public String getPath() {
		return getIdentifier();
	}

	@Override
	public String getRenderAs() {
		return null;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public String getPurpose() {
		return purpose;
	}

	@Override
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	@Override
	public String getReferenceId() {
		return null;
	}

	@Override
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Iterator<PropertyDefinition> iterator() {
		return getFields().iterator();
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "identifier", getIdentifier());
		Collection<JSONObject> fieldsData = TypeConverterUtil.getConverter().convert(JSONObject.class, getFields());
		JsonUtil.addToJson(object, "fields", new JSONArray(fieldsData));
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// nothing to do
	}

	@Override
	public List<PropertyDefinition> getConfigurations() {
		return configurations;
	}

	public void setConfigurations(List<PropertyDefinition> configurations) {
		this.configurations = configurations;
	}

	@Override
	public Optional<PropertyDefinition> getField(String name) {
		return findField(PropertyDefinition.hasName(name).or(PropertyDefinition.hasUri(name)));
	}
}
