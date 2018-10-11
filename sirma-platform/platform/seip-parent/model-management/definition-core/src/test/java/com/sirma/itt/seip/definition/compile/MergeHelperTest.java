package com.sirma.itt.seip.definition.compile;


import org.junit.Assert;
import org.junit.Test;

import com.sirma.itt.seip.definition.RegionDefinition;
import com.sirma.itt.seip.definition.RegionDefinitionModel;
import com.sirma.itt.seip.definition.model.BaseRegionDefinition;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;

/**
 * Test class for MergerHelper utility
 *
 * @author BBonev
 */
@SuppressWarnings("static-method")
public class MergeHelperTest {

	@Test
	public void mergeListTest() {
		RegionDefinitionModel src = new BaseRegionDefinition<>();
		RegionDefinitionModel target = new BaseRegionDefinition<>();

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
		Assert.assertEquals(DisplayType.SYSTEM, ((PropertyDefinition) target.getChild("prop1")).getDisplayType());

		System.out.println(target);
	}

	@Test
	public void mergeListTest2() {
		RegionDefinitionModel src = new BaseRegionDefinition<>();
		RegionDefinitionModel target = new BaseRegionDefinition<>();

		src.getFields().add(createProperty("prop1", "an..20", DisplayType.EDITABLE, true));
		src.getFields().add(createProperty("prop2", "an..230", DisplayType.HIDDEN, false));
		src.getFields().add(createProperty("prop3", "n10", DisplayType.READ_ONLY, false));
		src.getFields().add(createProperty("prop4", "n10", DisplayType.EDITABLE, true));

		target.getFields().add(createProperty("prop1", "an..20", DisplayType.SYSTEM, true));

		((BidirectionalMapping) src).initBidirection();
		((BidirectionalMapping) target).initBidirection();

		MergeHelper.mergeTemplate(target, src);

		Assert.assertEquals(4, target.getFields().size());
		Assert.assertEquals(DisplayType.SYSTEM, ((PropertyDefinition) target.getChild("prop1")).getDisplayType());

		System.out.println(target);
	}

	@Test
	public void mergeListTest3() {
		RegionDefinitionModel src = new BaseRegionDefinition<>();
		RegionDefinitionModel target = new BaseRegionDefinition<>();

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
		Assert.assertEquals(DisplayType.SYSTEM, ((PropertyDefinition) target.getChild("prop1")).getDisplayType());
		Assert.assertEquals(4, target.getRegions().get(0).getFields().size());
		Assert.assertEquals(DisplayType.READ_ONLY, ((PropertyDefinition) target.getChild("prop11")).getDisplayType());
		Assert.assertEquals(DisplayType.SYSTEM, ((PropertyDefinition) target.getChild("prop21")).getDisplayType());
		Assert.assertEquals("n10", ((PropertyDefinition) target.getChild("prop21")).getType());
		Assert.assertEquals(DisplayType.READ_ONLY, ((PropertyDefinition) target.getChild("prop31")).getDisplayType());
		Assert.assertTrue(((PropertyDefinition) target.getChild("prop21")).isMandatory());
	}

	@Test
	public void replaceIfNullTest() {
		// one of the field properties is missing
		Assert.assertFalse(MergeHelper.replaceIfNull(null, Boolean.FALSE).booleanValue());
		Assert.assertFalse(MergeHelper.replaceIfNull(Boolean.FALSE, null).booleanValue());
		Assert.assertTrue(MergeHelper.replaceIfNull(Boolean.TRUE, null).booleanValue());

		// with available properties
		Assert.assertFalse(MergeHelper.replaceIfNull(Boolean.FALSE, Boolean.FALSE).booleanValue());
		Assert.assertTrue(MergeHelper.replaceIfNull(Boolean.TRUE, Boolean.FALSE).booleanValue());
		Assert.assertFalse(MergeHelper.replaceIfNull(Boolean.FALSE, Boolean.TRUE).booleanValue());
		Assert.assertTrue(MergeHelper.replaceIfNull(Boolean.TRUE, Boolean.TRUE).booleanValue());
	}

	@Test
	public void mergeFields() {
		RegionDefinitionModel src = new BaseRegionDefinition<>();
		src.getFields().add(createProperty("prop1", "an..20", DisplayType.EDITABLE, true));
		src.getFields().add(createProperty("prop2", "an..20", DisplayType.EDITABLE, false));

		RegionDefinitionModel target = new BaseRegionDefinition<>();
		target.getFields().add(createProperty("prop1", "an..20", DisplayType.EDITABLE, false));
		target.getFields().add(createProperty("prop2", "an..20", DisplayType.EDITABLE, true));

		MergeHelper.mergeTemplate(target, src);
		Assert.assertFalse(((PropertyDefinition) target.getChild("prop1")).isMandatory().booleanValue());
		Assert.assertTrue(((PropertyDefinition) target.getChild("prop2")).isMandatory().booleanValue());
	}

	private static PropertyDefinition createProperty(String name, String type, DisplayType displayType,
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
