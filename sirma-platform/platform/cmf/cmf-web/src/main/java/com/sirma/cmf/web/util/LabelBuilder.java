package com.sirma.cmf.web.util;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.cmf.web.DocumentContext;
import com.sirma.itt.cmf.workflows.WorkflowHelper;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.web.util.DateUtil;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.codelist.CodelistService;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.definition.label.LabelProvider;
import com.sirma.itt.seip.domain.instance.DefaultProperties;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.Lockable;
import com.sirma.itt.seip.domain.instance.PropertyModel;
import com.sirma.itt.seip.domain.security.Action;
import com.sirma.itt.seip.resources.ResourceService;
import com.sirma.itt.seip.util.EqualsHelper;

/**
 * Utility functions for building application specific compound text messages.
 *
 * @author svelikov
 */
@Named
@ApplicationScoped
public class LabelBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(LabelBuilder.class);

	@Inject
	private CodelistService codelistService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	private ResourceService resourceService;

	@Inject
	private LabelProvider labelProvider;

	@Inject
	private DateUtil dateUtil;

	/**
	 * Gets the display name for user or returns the passed username.
	 *
	 * @param username
	 *            the username
	 * @return the display name for user or passed username.
	 */
	public String getDisplayNameForUser(String username) {
		if ("null".equals(username)) {
			return username;
		}
		return resourceService.getDisplayName(username);
	}

	/**
	 * Gets the display name for user.
	 *
	 * @param documentContext
	 *            the document context
	 * @param property
	 *            the property
	 * @return the display name for user
	 */
	public String getUserDisplayName(DocumentContext documentContext, String property) {
		String result = "";
		if (documentContext != null && property != null) {
			Instance currentInstance = documentContext.getCurrentInstance();
			if (currentInstance != null) {
				Serializable serializable = currentInstance.getProperties().get(property);
				String displayName = resourceService.getDisplayName(serializable);
				if (displayName != null) {
					result = displayName;
				}
			}
		}
		return result;
	}

	/**
	 * Gets the dashlet total count string.
	 *
	 * @param resultCount
	 *            the result count
	 * @param totalCount
	 *            the total count
	 * @return the dashlet total count string
	 */
	public String getDashletTotalCountString(int resultCount, int totalCount) {
		String template = labelProvider.getValue("cmf.dashboard.dashlet.totalCount");
		return MessageFormat.format(template, resultCount, totalCount);
	}

	/**
	 * Gets the accepted file type message.
	 *
	 * @param fileType
	 *            the file type
	 * @return the accepted file type message
	 */
	public String getAcceptedFileTypeMessage(String fileType) {
		return labelProvider.getValue(LabelConstants.DOCUMENT_UPLOAD_ACCEPTED_FILE_TYPE) + " " + fileType;
	}

	//
	// Case labels
	//

	// -------------------------------------------------
	// Document labels
	// -------------------------------------------------


	/**
	 * Assembles a summary for metadata fields for given document.
	 *
	 * @param documentInstance
	 *            Given document instance.
	 * @return Assembled summary string.
	 */
	public String getDocumentContextDataSummary(Instance documentInstance) {
		// Modified on {0} by {1}
		Map<String, Serializable> properties = documentInstance.getProperties();
		return MessageFormat.format(labelProvider.getValue(LabelConstants.DOCUMENT_CONTEXT_DATA_SUMMARY),
				properties.get(DefaultProperties.MODIFIED_ON),
				getDisplayNameForUser((String) properties.get(DefaultProperties.MODIFIED_BY)));
	}

	/**
	 * Assembles the warning message when a document is locked by user.
	 *
	 * @param documentInstance
	 *            Document instance.
	 * @return Message.
	 */
	// TODO: delete when document isLocked banner is ready
	public String getDocumentIsLockedMessage(Lockable documentInstance) {
		String name = documentInstance.getLockedBy();

		StringBuilder message = new StringBuilder(
				labelProvider.getValue(LabelConstants.MSG_WARNING_DOCUMENT_IS_LOCKED));
		message.append(" ").append(getDisplayNameForUser(name));

		return message.toString();
	}

	/**
	 * Returns message for historical document versions.
	 *
	 * @return formatted message
	 */
	public String getHistoricalDocumentMessage() {
		StringBuilder message = new StringBuilder(labelProvider.getValue(LabelConstants.HISTORICAL_VERSION));
		return message.toString();
	}

	// ----------------------------------------
	// Task and workflow labels
	// ----------------------------------------

	/**
	 * Assemble image path.
	 *
	 * @param imageName
	 *            Image name.
	 * @return Image path.
	 */
	public String getIconImagePath(String imageName) {
		return "images:icon_" + imageName + ".png";
	}

	/**
	 * Assemble image path using given extension.
	 *
	 * @param imageName
	 *            Image name.
	 * @param extension
	 *            Extension.
	 * @return Image path.
	 */
	public String getIconImagePath(String imageName, String extension) {
		return "images:icon_" + imageName + "." + extension;
	}

	/**
	 * User and version date processing into a single string.
	 *
	 * @param user
	 *            - user link string
	 * @param then
	 *            - version date
	 * @return concatenated left & then as string
	 */

	public String concatVersionStr(String user, Date then) {
		String durationString = "";
		String space = " ";
		String patternDays = labelProvider.getValue(LabelConstants.DOC_VERSIONS_DAYS);
		String patternHours = labelProvider.getValue(LabelConstants.DOC_VERSIONS_HOURS);
		String patternMinutes = labelProvider.getValue(LabelConstants.DOC_VERSIONS_MINUTES);
		long day = 1L * 24 * 60 * 60 * 1000;
		long hour = 1L * 60 * 60 * 1000;
		long minute = 1L * 60 * 1000;
		Calendar calThen = Calendar.getInstance();
		calThen.setTime(then);
		Calendar calNow = Calendar.getInstance();
		long diff = calNow.getTimeInMillis() - calThen.getTimeInMillis();
		long days = diff / day;
		long hours = diff / hour;
		long minutes = diff / minute;

		if (days >= 1) {
			durationString = MessageFormat.format(patternDays, days);
		} else if (hours >= 1 && hours < 24) {
			durationString = MessageFormat.format(patternHours, hours);
		} else if (minutes >= 1 && minutes < 60) {
			durationString = MessageFormat.format(patternMinutes, minutes);
		}
		return getDisplayNameForUser(user) + space + durationString;
	}

	/**
	 * Generates internationalized alert message.
	 *
	 * @param label
	 *            - string place holder
	 * @param extension
	 *            - string to embed in pace holder
	 * @return assembled string
	 */
	public String getWarningMessage(String label, String extension) {
		return String.format(label, extension);
	}

	/**
	 * Gets the action confirmation message.
	 *
	 * @param message
	 *            the message
	 * @param action
	 *            the action
	 * @return the action confirmation message
	 */
	public String getActionConfirmationMessage(String message, Action action) {
		return message.replace("{{operationName}}", action.getLabel());
	}

	/**
	 * Gets the confirm ok button label.
	 *
	 * @return the confirm ok button label
	 */
	public String getConfirmOkButtonLabel() {
		return labelProvider.getValue(LabelConstants.CONFIRM_OK_BUTTON);
	}

	/**
	 * Gets the confirm cancel button label.
	 *
	 * @return the confirm cancel button label
	 */
	public String getConfirmCancelButtonLabel() {
		return labelProvider.getValue(LabelConstants.CONFIRM_CANCEL_BUTTON);
	}

	/**
	 * Gets the confirm no button label.
	 *
	 * @return the confirm no button label
	 */
	public String getConfirmNoButtonLabel() {
		return labelProvider.getValue(LabelConstants.CONFIRM_NO_BUTTON);
	}

	/**
	 * Gets the codelist display value.
	 *
	 * @param model
	 *            the model
	 * @param property
	 *            the property
	 * @return the codelist display value
	 */
	public String getCodelistDisplayValue(PropertyModel model, String property) {
		String type = (String) model.getProperties().get(property);
		if (type == null) {
			return null;
		}
		type = WorkflowHelper.stripEngineId(type);
		PropertyDefinition definition = null;
		definition = dictionaryService.getProperty(property, model);
		if (definition != null && definition.getCodelist() != null) {
			String description = codelistService.getDescription(definition.getCodelist(), type);
			if (StringUtils.isNotNullOrEmpty(description)) {
				return description;
			}
		} else {
			LOGGER.debug("Property definition was not found or no codelist set: " + definition);
		}
		return type;
	}

	/**
	 * Gets the codelist display value.
	 *
	 * @param model
	 *            the model
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @return the codelist display value
	 */
	public String getCodelistDisplayValue(DefinitionModel model, String key, String value) {
		Integer cl = null;
		PropertyDefinition fieldDefinition = getFieldDefinition(model, key);
		if (fieldDefinition == null) {
			if (model instanceof RegionDefinitionModel) {
				for (RegionDefinition regionDefinition : ((RegionDefinitionModel) model).getRegions()) {
					fieldDefinition = getFieldDefinition(regionDefinition, key);
					if (fieldDefinition != null) {
						cl = fieldDefinition.getCodelist();
						break;
					}
				}
			}
		} else {
			cl = fieldDefinition.getCodelist();
		}
		String descr = value;
		if (cl != null) {
			String description = codelistService.getDescription(cl, value);
			if (StringUtils.isNotNullOrEmpty(description)) {
				descr = description;
			}
		}
		return descr;
	}

	/**
	 * Gets the field definition.
	 *
	 * @param model
	 *            the model
	 * @param name
	 *            the name
	 * @return the field definition
	 */
	private static PropertyDefinition getFieldDefinition(DefinitionModel model, String name) {
		for (PropertyDefinition definition : model.getFields()) {
			if (EqualsHelper.nullSafeEquals(definition.getName(), name, true)) {
				return definition;
			}
		}
		return null;
	}

	/**
	 * Gets the check box modifier.
	 *
	 * @param instance
	 *            the instance
	 * @param instanceProperty
	 *            the instance property
	 * @param checkBoxProperty
	 *            the check box property
	 * @return the check box modifier
	 */
	public String getCheckBoxModifier(Instance instance, String instanceProperty, String checkBoxProperty) {
		String userId = null;
		Date modifiedOn = null;

		Serializable serializable = instance.getProperties().get(instanceProperty);

		if (serializable instanceof Instance) {
			Serializable serializable2 = ((Instance) serializable).getProperties().get(checkBoxProperty);
			if (serializable2 instanceof Instance) {
				Map<String, Serializable> properties = ((Instance) serializable2).getProperties();
				if (properties.isEmpty()) {
					return "";
				}
				userId = (String) properties.get(DefaultProperties.CHECK_BOX_MODIFIED_FROM);
				modifiedOn = (Date) properties.get(DefaultProperties.CHECK_BOX_MODIFIED_ON);
			}
		}

		if (userId == null && modifiedOn == null) {
			return "";
		}
		String template = labelProvider.getValue(LabelConstants.CMF_CHECKBOX_MODIFIER_LINK_LABEL);

		return MessageFormat.format(template, getDisplayNameForUser(userId),
				modifiedOn == null ? "" : dateUtil.getFormattedDateTime(modifiedOn));
	}

	/**
	 * Gets the link description label.
	 *
	 * @param relatedInstance
	 *            the related instance
	 * @return the link description label
	 */
	public String getLinkDescriptionLabel(Instance relatedInstance) {
		// "<span class='info-row'>описание на връзка: <b>{0}</b></span>"
		String linkDescriptionLabel = MessageFormat.format(
				labelProvider.getValue(LabelConstants.CMF_CASE_LINK_DESCRIPTION_LABEL),
				relatedInstance.getProperties().get("relationLabel"));
		return linkDescriptionLabel;
	}

	/**
	 * Escapes javascript specific symbols in a string.
	 *
	 * @param text
	 *            String to escape.
	 * @return Escaped string.
	 */
	public String escapeJavascipt(String text) {
		return StringEscapeUtils.escapeJavaScript(text);
	}

	/**
	 * Assembles the warning message when an object is locked by user.
	 *
	 * @param objectInstance
	 *            Object instance.
	 * @return Message.
	 */
	// TODO: delete when object isLocked banner is ready
	public String getObjectIsLockedMessage(Instance objectInstance) {
		String name = objectInstance.getLockedBy();

		StringBuilder msg = new StringBuilder(labelProvider.getValue(LabelConstants.MSG_WARNING_OBJECT_IS_LOCKED));
		msg.append(" ").append(getDisplayNameForUser(name));

		return msg.toString();
	}

	/**
	 * Getter method for labelProvider.
	 *
	 * @return the labelProvider
	 */
	public LabelProvider getLabelProvider() {
		return labelProvider;
	}
}