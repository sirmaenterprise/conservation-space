package com.sirma.cmf.web.form.control;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.properties.DefaultProperties;
import com.sirma.itt.emf.security.AuthenticationService;

/**
 * Action class called on check box change event.
 * 
 * @author BBonev
 */
@Named("checkboxListener")
public class CheckboxChangedListener {

	@Inject
	private Logger logger;

	@Inject
	private AuthenticationService authenticationService;

	/**
	 * Checkbox changed.
	 * 
	 * @param target
	 *            the target
	 * @param changedProperty
	 *            the changed property
	 * @param checkboxProperty
	 *            the checkbox property
	 */
	public void checkboxChanged(Instance target, String changedProperty, String checkboxProperty) {
		logger.debug("Checkbox " + changedProperty + " has changed his property "
				+ checkboxProperty);

		Serializable serializable = target.getProperties().get(changedProperty);

		if (serializable instanceof Instance) {
			Serializable serializable2 = ((Instance) serializable).getProperties().get(
					checkboxProperty);
			if (serializable2 instanceof Instance) {
				Map<String, Serializable> properties = ((Instance) serializable2).getProperties();
				properties.put(DefaultProperties.CHECK_BOX_MODIFIED_ON, new Date());
				properties.put(DefaultProperties.CHECK_BOX_MODIFIED_FROM,
						authenticationService.getCurrentUserId());
			}
		}
	}
}
