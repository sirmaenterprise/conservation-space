package com.sirma.sep.widget;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.configuration.ConfigurationProperty;
import com.sirma.itt.seip.configuration.annotation.Configuration;
import com.sirma.itt.seip.configuration.annotation.ConfigurationPropertyDefinition;

/**
 * Configurations related to the widgets.
 * 
 * @author Mihail Radkov
 */
@ApplicationScoped
public class WidgetConfigurations {

	@Inject
	@Configuration
	@ConfigurationPropertyDefinition(name = "widget.image.types", defaultValue = "emf:Image",type=Uri.class, label = "Default object type for image widget.")
	private ConfigurationProperty<Uri> imageWidgetTypes;

	/**
	 * Getter method for imageWidgetTypes.
	 *
	 * @return the imageWidgetTypes
	 */
	public ConfigurationProperty<Uri> getImageWidgetTypes() {
		return imageWidgetTypes;
	}

}
