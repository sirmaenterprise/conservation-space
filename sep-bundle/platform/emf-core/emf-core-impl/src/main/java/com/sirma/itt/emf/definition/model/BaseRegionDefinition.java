package com.sirma.itt.emf.definition.model;

import java.util.LinkedList;
import java.util.List;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.emf.definition.compile.EmfMergeableFactory;
import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.domain.model.Mergeable;
import com.sirma.itt.emf.domain.model.Node;

/**
 * Base class that contains region definitions
 * 
 * @param <E>
 *            the element type
 * @author BBonev
 */
public class BaseRegionDefinition<E extends BaseRegionDefinition<?>> extends BaseDefinition<E>
		implements RegionDefinitionModel {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 3195236246758015433L;

	/** The regions. */
	@Tag(105)
	protected List<RegionDefinition> regions;

	/**
	 * Getter method for regions.
	 * 
	 * @return the regions
	 */
	@Override
	public List<RegionDefinition> getRegions() {
		if (regions == null) {
			regions = new LinkedList<RegionDefinition>();
		}
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
	@SuppressWarnings("unchecked")
	public E mergeFrom(E source) {
		super.mergeFrom(source);

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getRegions()),
				MergeHelper.convertToMergable(source.getRegions()), getRegionFactory());
		return (E) this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initBidirection() {
		super.initBidirection();

		if (regions != null) {
			for (RegionDefinition regionDefinition : regions) {
				RegionDefinitionImpl definitionImpl = (RegionDefinitionImpl) regionDefinition;
				definitionImpl.setBaseRegionDefinition(this);
				definitionImpl.initBidirection();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BaseRegionDefinition [regions=");
		builder.append(regions);
		builder.append(", toString()=");
		builder.append(super.toString());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Gets the region factory for creating new regions when merging. Override if needed. The
	 * current implementation will return non template region instances.
	 * 
	 * @return the region factory
	 */
	@SuppressWarnings("unchecked")
	protected MergeableInstanceFactory<Mergeable<Object>> getRegionFactory() {
		return EmfMergeableFactory.REGION_DEFINITION;
	}

	@Override
	public boolean hasChildren() {
		return super.hasChildren() || !getRegions().isEmpty();
	}

	@Override
	public Node getChild(String name) {
		Node find = super.getChild(name);
		if (find == null) {
			for (RegionDefinition regionDefinition : getRegions()) {
				if (regionDefinition.hasChildren()) {
					find = regionDefinition.getChild(name);
					if (find != null) {
						break;
					}
				}
			}
		}
		return find;
	}
}
