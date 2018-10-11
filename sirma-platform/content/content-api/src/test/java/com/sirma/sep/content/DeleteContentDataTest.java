package com.sirma.sep.content;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Test for  {@link DeleteContentData}
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 09/01/2018
 */
public class DeleteContentDataTest {
	@Test
	public void testEquals() throws Exception {
		assertEquals(buildData("storeName", "contentId", "tenantId", "key", "value"),
				buildData("storeName", "contentId", "tenantId", "key", "value"));
	}

	@Test
	public void testEquals_shouldDetectChanges() throws Exception {
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value"),
				buildData("storeName2", "contentId", "tenantId", "key", "value"));
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value"),
				buildData("storeName", "contentId2", "tenantId", "key", "value"));
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value"),
				buildData("storeName", "contentId", "tenantId2", "key", "value"));
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value"),
				buildData("storeName", "contentId", "tenantId", "key2", "value"));
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value"),
				buildData("storeName", "contentId", "tenantId", "key", "value2"));
	}

	private DeleteContentData buildData(String store, String contentId, String tenantId, String propKey,
			String propValue) {
		return new DeleteContentData().setStoreName(store)
				.setContentId(contentId)
				.setTenantId(tenantId)
				.addProperty(propKey, propValue);
	}

	@Test
	public void testHashCode() throws Exception {
		assertEquals(buildData("storeName", "contentId", "tenantId", "key", "value").hashCode(),
				buildData("storeName", "contentId", "tenantId", "key", "value").hashCode());
	}

	@Test
	public void testHashCode_shouldDetectChanges() throws Exception {
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value").hashCode(),
				buildData("storeName2", "contentId", "tenantId", "key", "value").hashCode());
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value").hashCode(),
				buildData("storeName", "contentId2", "tenantId", "key", "value").hashCode());
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value").hashCode(),
				buildData("storeName", "contentId", "tenantId2", "key", "value").hashCode());
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value").hashCode(),
				buildData("storeName", "contentId", "tenantId", "key2", "value").hashCode());
		assertNotEquals(buildData("storeName", "contentId", "tenantId", "key", "value").hashCode(),
				buildData("storeName", "contentId", "tenantId", "key", "value2").hashCode());
	}

	@Test
	public void jsonConversion() {
		DeleteContentData data = buildData("storeName", "contentId", "tenantId", "key", "value");
		String jsonString = data.asJsonString();
		DeleteContentData copy = DeleteContentData.fromJsonString(jsonString);
		assertEquals(data, copy);
	}

}
