package com.sirma.itt.emf.definition.model;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.MergeableBase;

/**
 * Default implementation for {@link AllowedChildDefinition}
 * 
 * @author BBonev
 */
public class AllowedChildDefinitionImpl extends MergeableBase<AllowedChildDefinitionImpl> implements
		Serializable, AllowedChildDefinition, BidirectionalMapping {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 6362366657918793437L;

	/** The value. */
	@Tag(1)
	protected String identifier;

	/** The permissions. */
	@Tag(2)
	protected List<AllowedChildConfiguration> permissions;

	/** The filters. */
	@Tag(3)
	protected List<AllowedChildConfiguration> filters;

	/** The parent definition. */
	protected transient DefinitionModel parentDefinition;

	/**
	 * The default type of the child is workflow to backward compatibility. The value is set in the
	 * getter method.
	 */
	@Tag(4)
	protected String type;

	/** The default child. */
	@Tag(5)
	protected Boolean defaultChild;

	/**
	 * Getter method for caseDefinition.
	 *
	 * @return the caseDefinition
	 */
	@Override
	public DefinitionModel getParentDefinition() {
		return parentDefinition;
	}

	/**
	 * Sets the parent definition.
	 * 
	 * @param parentDefinition
	 *            the new parent definition
	 */
	public void setParentDefinition(DefinitionModel parentDefinition) {
		this.parentDefinition = parentDefinition;
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Getter method for permissions.
	 *
	 * @return the permissions
	 */
	@Override
	public List<AllowedChildConfiguration> getPermissions() {
		if (permissions == null) {
			permissions = new LinkedList<AllowedChildConfiguration>();
		}
		return permissions;
	}

	/**
	 * Setter method for permissions.
	 *
	 * @param permissions
	 *            the permissions to set
	 */
	public void setPermissions(List<AllowedChildConfiguration> permissions) {
		this.permissions = permissions;
	}

	/**
	 * Getter method for filters.
	 *
	 * @return the filters
	 */
	@Override
	public List<AllowedChildConfiguration> getFilters() {
		if (filters == null) {
			filters = new LinkedList<AllowedChildConfiguration>();
		}
		return filters;
	}

	/**
	 * Setter method for filters.
	 *
	 * @param filters
	 *            the filters to set
	 */
	public void setFilters(List<AllowedChildConfiguration> filters) {
		this.filters = filters;
	}

	@Override
	@SuppressWarnings("unchecked")
	public AllowedChildDefinitionImpl mergeFrom(AllowedChildDefinitionImpl source) {
		identifier = MergeHelper.replaceIfNull(identifier, source.identifier);
		type = MergeHelper.replaceIfNull(type, source.type);
		defaultChild = MergeHelper.replaceIfNull(defaultChild, source.defaultChild);

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getPermissions()),
				MergeHelper.convertToMergable(source.getPermissions()),
				EmfMergeableFactory.ALLOWED_CHILDREN_CONFIGURATION);
		MergeHelper.mergeLists(MergeHelper.convertToMergable(getFilters()),
				MergeHelper.convertToMergable(source.getFilters()),
				EmfMergeableFactory.ALLOWED_CHILDREN_CONFIGURATION);
		return this;
	}

	@Override
	public void initBidirection() {
		// nothing to do here
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AllowedChildDefinitionImpl [");
		builder.append("identifier=");
		builder.append(identifier);
		builder.append(", permissions=");
		builder.append(permissions);
		builder.append(", filters=");
		builder.append(filters);
		builder.append(", defaultChild=");
		builder.append(defaultChild);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String getType() {
		if (type == null) {
			type = "workflow";
		}
		return type;
	}

	/**
	 * Setter method for type.
	 * 
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Getter method for defaultChild.
	 * 
	 * @return the defaultChild
	 */
	public Boolean getDefaultChild() {
		if (defaultChild == null) {
			defaultChild = Boolean.FALSE;
		}
		return defaultChild;
	}

	/**
	 * Setter method for defaultChild.
	 * 
	 * @param defaultChild
	 *            the defaultChild to set
	 */
	public void setDefaultChild(Boolean defaultChild) {
		this.defaultChild = defaultChild;
	}

	@Override
	public boolean isDefault() {
		return getDefaultChild().booleanValue();
	}

}
