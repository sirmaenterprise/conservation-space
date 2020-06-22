package com.sirma.itt.seip.instance.content.share;

import com.esotericsoftware.kryo.serializers.TaggedFieldSerializer.Tag;

import java.io.Serializable;

/**
 * Contains needed data for sharing instance content.
 *
 * @author <a href="mailto:ivo.rusev@sirma.bg">Ivo Rusev</a>
 * @since 20/09/2017
 */
public class ContentShareData implements Serializable {

	@Tag(1)
	private String instanceId;
	@Tag(2)
	private String title;
	@Tag(3)
	private String contentId;
	@Tag(4)
	private String token;
	@Tag(5)
	private String format;

	private ContentShareData() {
	}

	/**
	 * Builds a new {@link ContentShareData} object.
	 *
	 * @return the new object.
	 */
	public static ContentShareData buildEmpty() {
		return new ContentShareData();
	}

	public String getInstanceId() {
		return instanceId;
	}

	public ContentShareData setInstanceId(String instanceId) {
		this.instanceId = instanceId;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public ContentShareData setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getContentId() {
		return contentId;
	}

	public ContentShareData setContentId(String contentId) {
		this.contentId = contentId;
		return this;
	}

	public String getToken() {
		return token;
	}

	public ContentShareData setToken(String token) {
		this.token = token;
		return this;
	}

	public String getFormat() {
		return format;
	}

	public ContentShareData setFormat(String format) {
		this.format = format;
		return this;
	}
}
