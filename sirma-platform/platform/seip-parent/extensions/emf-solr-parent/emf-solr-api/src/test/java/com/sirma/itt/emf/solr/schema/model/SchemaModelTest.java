package com.sirma.itt.emf.solr.schema.model;

import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.sirma.itt.seip.json.JsonUtil;

/**
 * The SchemaModelTest tests the {@link SolrSchemaModel}.
 */
public class SchemaModelTest {

	/**
	 * Tests the build.
	 */
	@Test
	public void build() {
		SolrSchemaModel newModel = new SolrSchemaModel("test", "1");
		JSONObject fieldModel = JsonUtil.toJsonObject("{\"paramname\":\"paramval\",\"paramname2\":\"paramval2\"}");

		SchemaEntryDynamicField entry1_1 = new SchemaEntryDynamicField("test", "solr.TextField",
				JsonUtil.toMap(fieldModel));
		SchemaEntryDynamicField entry2_1 = new SchemaEntryDynamicField("test2", "solr.TextField",
				JsonUtil.toMap(fieldModel));
		SchemaEntryDynamicField entry3_1 = new SchemaEntryDynamicField("test3", "solr.TextField",
				JsonUtil.toMap(fieldModel));
		SchemaEntryFieldType entry4_1 = new SchemaEntryFieldType("test4", "solr.TextField", JsonUtil.toMap(fieldModel));
		SchemaEntryField entry5_1 = new SchemaEntryField("test5", "solr.TextField", JsonUtil.toMap(fieldModel));
		SchemaEntryCopyField entry6_1 = new SchemaEntryCopyField("test6", "test7");

		SchemaEntryDynamicField entry1_2 = new SchemaEntryDynamicField("test", "solr.TextField",
				JsonUtil.toMap(fieldModel));
		try {
			newModel.addEntry(entry1_1);
			newModel.addEntry(entry2_1);
			newModel.addEntry(entry3_1);
			newModel.addEntry(entry4_1);
			newModel.addEntry(entry5_1);
			newModel.addEntry(entry6_1);
		} catch (Exception e) {
			Assert.fail("Should not fail on valid entries!", e);
		}
		try {
			newModel.addEntry(entry1_1);
			newModel.addEntry(entry1_2);
			Assert.fail("Should not add the same entry twice");
		} catch (Exception e) {
		}
	}

	/**
	 * Test the Diff alghoritm.
	 */
	@Test
	public void diff() {
		SolrSchemaModel oldModel = new SolrSchemaModel("test", "1");
		SolrSchemaModel newModel = new SolrSchemaModel("test", "1");
		JSONObject json1Object = JsonUtil.toJsonObject("{\"paramname\":\"paramval\",\"paramname2\":\"paramval2\"}");
		JSONObject json2Object = JsonUtil.toJsonObject("{\"paramname2\":\"paramval2\",\"paramname\":\"paramval\"}");
		JSONObject json3Object = JsonUtil
				.toJsonObject("{\"paramname2\":\"paramval2Changed\",\"paramname\":\"paramval\"}");
		SchemaEntryDynamicField entry1_1 = new SchemaEntryDynamicField("test", "solr.TextField",
				JsonUtil.toMap(json1Object));
		SchemaEntryDynamicField entry1_2 = new SchemaEntryDynamicField("test", "solr.TextField",
				JsonUtil.toMap(json2Object));
		SchemaEntryDynamicField entry1_3 = new SchemaEntryDynamicField("test", "solr.TextField",
				JsonUtil.toMap(json3Object));
		try {
			newModel.addEntry(entry1_1);
			oldModel.addEntry(entry1_2);
			String built = newModel.diff(oldModel).build();
			Assert.assertEquals(new JSONObject(built).length(), 0, "Same elements should not be replaced");
			// diff again
			newModel = new SolrSchemaModel("test", "1");
			newModel.addEntry(entry1_3);
			built = newModel.diff(oldModel).build();
			Assert.assertEquals(new JSONObject(built).length(), 1, "Same elements should not be replaced");
		} catch (Exception e) {
			Assert.fail("Should not fail on valid entries!", e);
		}

	}
}
