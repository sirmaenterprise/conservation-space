package com.sirma.itt.seip.template.rest.handlers.writers;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.CONTENT;
import static com.sirma.itt.seip.rest.utils.JsonKeys.ID;
import static com.sirma.itt.seip.rest.utils.JsonKeys.TEMPLATE_INSTANCE_ID;
import static com.sirma.itt.seip.template.TemplateProperties.FOR_TYPE;
import static com.sirma.itt.seip.template.TemplateProperties.PRIMARY;

import java.io.Serializable;

import javax.json.stream.JsonGenerator;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.rest.utils.JSON;
import com.sirma.itt.seip.rest.utils.JsonKeys;
import com.sirma.itt.seip.template.TemplateInstance;
import com.sirma.itt.seip.template.TemplateProperties;

/**
 * Utility class for generating JSON from {@link TemplateInstance}.
 *
 * @author yasko
 */
public final class TemplateInstanceToJson {

	private TemplateInstanceToJson() {
		// utility
	}

	/**
	 * Writes the json representation of {@link TemplateInstance} to a {@link JsonGenerator}.
	 *
	 * @param generator
	 *            {@link JsonGenerator} to write to.
	 * @param template
	 *            {@link TemplateInstance} to serialize as json.
	 */
	public static void writeJson(JsonGenerator generator, TemplateInstance template) {
		generator.writeStartObject();

		writeMandatoryProperty(template.getId(), ID, generator);
		JSON.addIfNotNull(generator, TEMPLATE_INSTANCE_ID, template.getCorrespondingInstance());

		generator.write(FOR_TYPE, template.getForType()).writeStartObject(JsonKeys.PROPERTIES).write(PRIMARY,
				template.getBoolean(PRIMARY, Boolean.FALSE));

		JSON.addIfNotNull(generator, TemplateProperties.TITLE, template.getString(TemplateProperties.TITLE));
		JSON.addIfNotNull(generator, TemplateProperties.PURPOSE, template.getString(TemplateProperties.PURPOSE));

		generator.writeEnd();

		String content = template.getContent();
		if (content != null) {
			generator.write(CONTENT, content);
		}
		generator.writeEnd();
	}

	private static void writeMandatoryProperty(Serializable value, String key, JsonGenerator generator) {
		if (StringUtils.isNullOrEmpty(value.toString())) {
			throw new EmfRuntimeException("Invalid template! Missing " + key);
		}
		generator.write(key, String.valueOf(value));
	}
}
