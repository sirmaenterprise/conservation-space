package com.sirma.itt.seip.adapters.iiif;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;

import org.junit.Test;

import com.srima.itt.seip.adapters.mock.ContentInfoMock;

/**
 * Tests the Manifest class which contains the information about a manifest.
 *
 * @author Nikolay Ch
 */
public class ManifestTest {

	/**
	 * Test the if the manifest class returns correct id and name.
	 */
	@Test
	public void testManfiestNameAndId() {
		ContentInfoMock contentInfo = new ContentInfoMock("manifestId");
		contentInfo.setName("manifestName");
		Manifest manifest = new Manifest(contentInfo);

		assertEquals(manifest.getName(), "manifestName");
		assertEquals(manifest.getManifestId(), "manifestId");
	}

	/**
	 * Test the correct creation of json object.
	 *
	 * @throws IOException
	 *             if an error occurs
	 */
	@Test
	public void testJsonObjectCreation() throws IOException {
		JsonObject jsonObject = Json.createObjectBuilder().add("name", "manifestName").build();
		InputStream stream = new ByteArrayInputStream(jsonObject.toString().getBytes("UTF-8"));
		ContentInfoMock contentInfo = new ContentInfoMock("manifestId");
		contentInfo.setInputStream(stream);
		Manifest manifest = new Manifest(contentInfo);
		assertEquals(manifest.getAsJson(), jsonObject);
		assertNotNull(manifest.getAsString());
	}
}
