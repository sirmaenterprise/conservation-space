package com.sirma.sep.content;

import static com.sirma.itt.seip.collections.CollectionUtils.addNonNullValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.sirma.itt.seip.PropertiesReader;
import com.sirma.itt.seip.json.JSON;

/**
 * DTO used to store static data for files that should be deleted from particular content store. The data is generated
 * from the {@link ContentStore#prepareForDelete(StoreItemInfo)} methods and should be passed to
 * {@link ContentStore#delete(DeleteContentData)} for actual content deletion.<p>
 *     The options {@link #doNotDeleteContent()} and {@link #setContentOnly(boolean)} could be used to control what
 *     exactly is deleted for the content entry.
 *     <ul>
 *         <li>{@link #doNotDeleteContent() doNotDeleteContent} controls if the actual content should be deleted or
 *         not from the content data store. Once deleted it cannot be restored. By default it will be deleted. In order
 *         not to delete the content it should be set to {@code false}.<br> The main purpose of this is not to delete
 *         referenced content by other content entries</li>
 *         <li>{@link #setContentOnly(boolean) setContentOnly} controls if the content entity should be deleted from the
 *         database. In order to not delete the content entity it should be set to {@code true}.<br> The main purpose of
 *         this is for content clean up during content move. Where we do not want to delete the content entity but only
 *         the actual content from the remote store after moving the file</li>
 *     </ul>
 * </p>
 *
 * @author <a href="mailto:borislav.bonev@sirma.bg">Borislav Bonev</a>
 * @since 22/12/2017
 */
public class DeleteContentData implements PropertiesReader {

	private String contentId;
	private String storeName;
	private String tenantId;
	private boolean contentOnly;
	private boolean deleteContent = true;

	private Map<String, Serializable> properties = new HashMap<>();
	/**
	 * Parse the given string as json and populates an {@link DeleteContentData} instance using the data found. The
	 * argument should be produced from the {@link #asJsonString()} method from other instance.
	 *
	 * @param data the json data to pass
	 * @return the constructed instance
	 */
	public static DeleteContentData fromJsonString(String data) {
		return JSON.readObject(data, json -> {
			DeleteContentData deleteContentData = new DeleteContentData()
					.setTenantId(json.getString("tenantId"))
					.setContentId(json.getString("contentId"))
					.setStoreName(json.getString("storeName"))
					.setContentOnly(json.getBoolean("contentOnly", false));
			if (!json.getBoolean("deleteContent", true)) {
				deleteContentData.doNotDeleteContent();
			}
			deleteContentData.getProperties().putAll(JSON.jsonToMap(json.getJsonObject("properties")));
			return deleteContentData;
		});
	}

	public String getContentId() {
		return contentId;
	}

	public DeleteContentData setContentId(String contentId) {
		this.contentId = contentId;
		return this;
	}

	public String getStoreName() {
		return storeName;
	}

	public DeleteContentData setStoreName(String storeName) {
		this.storeName = storeName;
		return this;
	}

	public String getTenantId() {
		return tenantId;
	}

	public DeleteContentData setTenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	@Override
	public Map<String, Serializable> getProperties() {
		return properties;
	}

	public boolean isContentOnly() {
		return contentOnly;
	}

	/**
	 * Specifies that only the referenced content should be deleted but not the referenced content entry.
	 *
	 * @param contentOnly if true only the content will be deleted without the entity, false to delete content and entity
	 * @return current instance for chaining
	 */
	public DeleteContentData setContentOnly(boolean contentOnly) {
		this.contentOnly = contentOnly;
		return this;
	}

	public DeleteContentData addProperty(String key, Serializable value) {
		addNonNullValue(properties, key, value);
		return this;
	}

	public boolean isDeleteContent() {
		return deleteContent;
	}

	/**
	 * This could be used to <b>disable</b> the actual content deletion. By default it's enabled.
	 *
	 * @return current instance for chaining
	 */
	public DeleteContentData doNotDeleteContent() {
		this.deleteContent = false;
		return this;
	}

	public String asJsonString() {
		JsonObjectBuilder main = Json.createObjectBuilder();
		main.add("tenantId", tenantId);
		main.add("contentId", contentId);
		main.add("storeName", storeName);
		main.add("contentOnly", contentOnly);
		main.add("deleteContent", deleteContent);
		main.add("properties", JSON.convertToJsonObject(getProperties()));
		return main.build().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof DeleteContentData)) {
			return false;
		}
		DeleteContentData data = (DeleteContentData) o;
		return Objects.equals(contentId, data.contentId) &&
				Objects.equals(storeName, data.storeName) &&
				Objects.equals(tenantId, data.tenantId) &&
				contentOnly == data.contentOnly &&
				Objects.equals(properties, data.properties);
	}

	@Override
	public int hashCode() {
		return Objects.hash(contentId, storeName, tenantId, contentOnly, properties);
	}
}
