package com.sirma.itt.seip.definition.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;
import com.sirma.itt.seip.Sealable;
import com.sirma.itt.seip.convert.TypeConverterUtil;
import com.sirma.itt.seip.definition.Mergeable;
import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.compile.EmfMergeableFactory;
import com.sirma.itt.seip.definition.compile.MergeHelper;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.domain.Node;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.json.JsonUtil;

/**
 * Base class that contains region definitions
 *
 * @param <E>
 *            the element type
 * @author BBonev
 */
public class BaseRegionDefinition<E extends BaseRegionDefinition<?>> extends BaseDefinition<E>
		implements RegionDefinitionModel {

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
			regions = new LinkedList<>();
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
		if (!isSealed()) {
			this.regions = regions;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public E mergeFrom(E source) {
		super.mergeFrom(source);

		MergeHelper.mergeLists(MergeHelper.convertToMergable(getRegions()),
				MergeHelper.convertToMergable(source.getRegions()), getRegionFactory());
		return (E) this;
	}

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
	 * Gets the region factory for creating new regions when merging. Override if needed. The current implementation
	 * will return non template region instances.
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

	@Override
	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		JsonUtil.addToJson(object, "identifier", getIdentifier());
		List<PropertyDefinition> fieldsData = getFields();
		List<RegionDefinition> regionData = getRegionCopy();
		List<Ordinal> sortRegionsAndFields = DefinitionUtil.sortRegionsAndFields(fieldsData, regionData);
		Collection<JSONObject> data = TypeConverterUtil.getConverter().convert(JSONObject.class, sortRegionsAndFields);
		JsonUtil.addToJson(object, "fields", new JSONArray(data));
		return object;
	}

	@Override
	public void fromJSONObject(JSONObject jsonObject) {
		// Method not in use
	}

	/**
	 * Method that copy original regions data to avoid concurrent modification.
	 *
	 * @return list with copied regions
	 */
	private List<RegionDefinition> getRegionCopy() {
		List<RegionDefinition> regionsCopy = new ArrayList<>(getRegions().size());
		for (RegionDefinition region : getRegions()) {
			regionsCopy.add(((RegionDefinitionImpl) region).createCopy());
		}
		return regionsCopy;
	}

	@Override
	public void seal() {

		if (isSealed()) {
			return;
		}

		regions = Collections.unmodifiableList(Sealable.seal(getRegions()));

		super.seal();
	}
}
