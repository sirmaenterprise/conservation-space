package com.sirma.itt.pm.web.utils;

import java.text.MessageFormat;
import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Specializes;

import com.sirma.cmf.web.util.LabelBuilder;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.pm.constants.ProjectProperties;
import com.sirma.itt.pm.domain.model.ProjectInstance;

/**
 * Utility class for building custom labels for PM module.
 * 
 * @author BBonev
 */
@Specializes
@ApplicationScoped
public class PmLabelBuilder extends LabelBuilder implements LabelConstantsPm {

	/**
	 * Gets the project link label.
	 * 
	 * @param instance
	 *            the case instance
	 * @return the case link label
	 */
	public String getProjectLinkLabel(ProjectInstance instance) {
		// <span class='link-row'>{0}, ({1}), {2}</span><span class='info-row'>Owner: <b>{3}</b>,
		// Status: <b>{4}</b>, Created on: <b>{5,date,dd.MM.yyyy, HH:mm}</b></span>
		String template = labelProvider.getValue(PROJECT_LINK_LABEL);

		String id = (String) instance.getProperties().get(DefaultProperties.UNIQUE_IDENTIFIER);
		String type = getCodelistDisplayValue(instance, ProjectProperties.TYPE);
		String title = (String) instance.getProperties().get(ProjectProperties.TITLE);
		String state = getCodelistDisplayValue(instance, ProjectProperties.STATUS);
		String owner = (String) instance.getProperties().get(ProjectProperties.OWNER);
		Date createdOn = (Date) instance.getProperties().get(ProjectProperties.CREATED_ON);

		String label = MessageFormat.format(template, id, type, title,
				getDisplayNameForUser(owner), state, createdOn);
		return label;
	}
}
