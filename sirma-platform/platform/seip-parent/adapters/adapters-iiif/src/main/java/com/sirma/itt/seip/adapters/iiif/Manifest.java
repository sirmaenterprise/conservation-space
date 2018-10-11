package com.sirma.itt.seip.adapters.iiif;

import java.io.IOException;
import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import com.sirma.sep.content.ContentInfo;

/**
 * Class that stores the information for a manifest.
 *
 * @author Nikolay Ch
 */
public class Manifest {
	private ContentInfo contentInfo;

	/**
	 * Instantiates the class by given content info.
	 *
	 * @param contentInfo
	 *            the content info
	 */
	public Manifest(ContentInfo contentInfo) {
		this.contentInfo = contentInfo;
	}

	/**
	 * Returns the manifest as json object.
	 *
	 * @return json object
	 */
	public JsonObject getAsJson() {
		try (JsonReader jsonReader = Json.createReader(contentInfo.getInputStream())) {
			return jsonReader.readObject();
		}
	}

	/**
	 * Returns the manifest as string.
	 *
	 * @return the string
	 * @throws IOException
	 *             if an error occurs
	 */
	public String getAsString() throws IOException {
		return contentInfo.asString();
	}

	/**
	 * Getter for the manifest input stream.
	 *
	 * @return the input stream
	 */
	public InputStream getInputStream() {
		return contentInfo.getInputStream();
	}

	/**
	 * Getter for the manifest id.
	 *
	 * @return the manifest id
	 */
	public String getManifestId() {
		return contentInfo.getContentId();
	}

	/**
	 * Getter for the name of the manifest file.
	 *
	 * @return the name
	 */
	public String getName() {
		return contentInfo.getName();
	}

	/**
	 * Getter for the length of the manifest.
	 *
	 * @return the legnth
	 */
	public long getLength() {
		return contentInfo.getLength();
	}

	/**
	 * Checks if a manifest exists with the given id.
	 *
	 * @return true if exists and false if not
	 */
	public boolean exists() {
		return contentInfo.exists();
	}

}
