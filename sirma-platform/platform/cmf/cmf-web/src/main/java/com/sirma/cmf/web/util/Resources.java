package com.sirma.cmf.web.util;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * Resources producer.
 *
 * @author svelikov
 */
public class Resources {

	/**
	 * Producer for the project stage.
	 *
	 * @return the ProjectStage.
	 */
	@Produces
	@Named("getStage")
	@ApplicationScoped
	public com.sirma.cmf.web.util.ProjectStage getStage() {
		boolean isDevelopement = FacesContext.getCurrentInstance().getApplication().getProjectStage().toString().equals(
				ProjectStage.Development.name());

		com.sirma.cmf.web.util.ProjectStage stage = new com.sirma.cmf.web.util.ProjectStage();
		stage.setDevelopement(isDevelopement);

		return stage;
	}

}
