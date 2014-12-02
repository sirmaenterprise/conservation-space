package com.sirma.itt.cmf.util;


import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.emf.definition.compile.MergeHelper;
import com.sirma.itt.emf.definition.model.BaseRegionDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;

/**
 * Test class for MergerHelper utility
 *
 * @author BBonev
 */
public class MergeHelperTest {

	/**
	 * Merge list test.
	 */
	@Test
	public void mergeListTest() {
		RegionDefinitionModel src = new BaseRegionDefinition<BaseRegionDefinition<?>>();
		RegionDefinitionModel target = new BaseRegionDefinition<BaseRegionDefinition<?>>();

		src.getFields().add(createProperty("prop1", "an..20", DisplayType.EDITABLE, true));
		src.getFields().add(createProperty("prop2", "an..230", DisplayType.HIDDEN, false));
		src.getFields().add(createProperty("prop3", "n10", DisplayType.READ_ONLY, false));

		target.getFields().add(createProperty("prop1", "an..20", DisplayType.SYSTEM, true));
		target.getFields().add(createProperty("prop2", "an..200", DisplayType.READ_ONLY, false));
		target.getFields().add(createProperty("prop3", "n10", DisplayType.EDITABLE, false));
		target.getFields().add(createProperty("prop4", "n10", DisplayType.EDITABLE, true));

		((BidirectionalMapping) src).initBidirection();
		((BidirectionalMapping) target).initBidirection();

		MergeHelper.mergeTemplate(target, src);

		Assert.assertEquals(4, target.getFields().size());
		Assert.assertEquals(DisplayType.SYSTEM,
				((PropertyDefinition) target.getChild("prop1")).getDisplayType());

		System.out.println(target);
	}

	/**
	 * Merge list test2.
	 */
	@Test
	public void mergeListTest2() {
		RegionDefinitionModel src = new BaseRegionDefinition<BaseRegionDefinition<?>>();
		RegionDefinitionModel target = new BaseRegionDefinition<BaseRegionDefinition<?>>();

		src.getFields().add(createProperty("prop1", "an..20", DisplayType.EDITABLE, true));
		src.getFields().add(createProperty("prop2", "an..230", DisplayType.HIDDEN, false));
		src.getFields().add(createProperty("prop3", "n10", DisplayType.READ_ONLY, false));
		src.getFields().add(createProperty("prop4", "n10", DisplayType.EDITABLE, true));

		target.getFields().add(createProperty("prop1", "an..20", DisplayType.SYSTEM, true));

		((BidirectionalMapping) src).initBidirection();
		((BidirectionalMapping) target).initBidirection();

		MergeHelper.mergeTemplate(target, src);

		Assert.assertEquals(4, target.getFields().size());
		Assert.assertEquals(DisplayType.SYSTEM,
				((PropertyDefinition) target.getChild("prop1")).getDisplayType());

		System.out.println(target);
	}

	/**
	 * Merge list test3.
	 */
	@Test
	public void mergeListTest3() {
		RegionDefinitionModel src = new BaseRegionDefinition<BaseRegionDefinition<?>>();
		RegionDefinitionModel target = new BaseRegionDefinition<BaseRegionDefinition<?>>();

		src.getFields().add(createProperty("prop1", "an..20", DisplayType.EDITABLE, true));
		src.getFields().add(createProperty("prop2", "an..230", DisplayType.HIDDEN, false));
		src.getFields().add(createProperty("prop3", "n10", DisplayType.READ_ONLY, false));
		src.getFields().add(createProperty("prop4", "n10", DisplayType.EDITABLE, true));

		RegionDefinition region = new RegionDefinitionImpl();
		region.setIdentifier("region");
		region.getFields().add(createProperty("prop11", "an..20", DisplayType.EDITABLE, true));
		region.getFields().add(createProperty("prop21", "an..230", DisplayType.HIDDEN, false));
		region.getFields().add(createProperty("prop31", "n10", DisplayType.READ_ONLY, false));
		region.getFields().add(createProperty("prop41", "n10", DisplayType.EDITABLE, true));

		src.getRegions().add(region);

		target.getFields().add(createProperty("prop1", "an..20", DisplayType.SYSTEM, true));

		region = new RegionDefinitionImpl();
		region.setIdentifier("region");
		region.getFields().add(createProperty("prop11", "an..20", DisplayType.READ_ONLY, false));
		region.getFields().add(createProperty("prop21", "n10", DisplayType.SYSTEM, true));

		target.getRegions().add(region);

		((BidirectionalMapping) src).initBidirection();
		((BidirectionalMapping) target).initBidirection();

		MergeHelper.mergeTemplate(target, src);

		Assert.assertEquals(4, target.getFields().size());
		Assert.assertEquals(DisplayType.SYSTEM,
				((PropertyDefinition) target.getChild("prop1")).getDisplayType());
		Assert.assertEquals(4, target.getRegions().get(0).getFields().size());
		Assert.assertEquals(DisplayType.READ_ONLY,
				((PropertyDefinition) target.getChild("prop11")).getDisplayType());
		Assert.assertEquals(DisplayType.SYSTEM,
				((PropertyDefinition) target.getChild("prop21")).getDisplayType());
		Assert.assertEquals("n10", ((PropertyDefinition) target.getChild("prop21")).getType());
		Assert.assertEquals(DisplayType.READ_ONLY,
				((PropertyDefinition) target.getChild("prop31")).getDisplayType());
		Assert.assertTrue(((PropertyDefinition) target.getChild("prop21")).isMandatory());

		System.out.println(target);
	}

	/**
	 * Creates the property.
	 *
	 * @param name
	 *            the name
	 * @param type
	 *            the type
	 * @param displayType
	 *            the display type
	 * @param mandatory
	 *            the mandatory
	 * @return the property definition
	 */
	private PropertyDefinition createProperty(String name, String type, DisplayType displayType,
			boolean mandatory) {
		PropertyDefinitionProxy proxy = new PropertyDefinitionProxy();
		proxy.setName(name);
		proxy.setType(type);
		proxy.setDisplayType(displayType);
		proxy.setMandatory(mandatory);
		proxy.setDefaultProperties();
		return proxy;
	}
}
