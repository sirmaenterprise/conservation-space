package com.sirma.itt.emf.web.application;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.web.config.EmfWebConfigurationProperties;

/**
 * The Class EmfApplication.
 * 
 * @author svelikov
 */
@Named
@ApplicationScoped
public class EmfApplication {

	/** The context path. */
	private String contextPath;

	/** The help module link. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.HELP_MODULE_LINK, defaultValue = "")
	private String helpModuleLink;

	/** The application mode developement. */
	@Inject
	@Config(name = EmfConfigurationProperties.APPLICATION_MODE_DEVELOPEMENT, defaultValue = "false")
	private Boolean applicationModeDevelopement;

	/** If footer should be visible. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.APPLICATION_FOOTER_ENABLE, defaultValue = "false")
	private Boolean applicationFooterEnable;

	/** The application module schedule disable. */
	@Inject
	@Config(name = EmfWebConfigurationProperties.APPLICATION_MODULE_SCHEDULE_DISABLE, defaultValue = "false")
	private Boolean applicationModuleScheduleDisable;

	@Inject
	@Config(name = EmfWebConfigurationProperties.JAVSCRIPT_DEBUG, defaultValue = "false")
	private Boolean javascriptDebug;

	@Inject
	@Config(name = EmfWebConfigurationProperties.SEARCH_RESULT_PAGER_PAGESIZE, defaultValue = "25")
	private int pagerPageSize;

	@Inject
	@Config(name = EmfWebConfigurationProperties.SEARCH_RESULT_PAGER_MAXPAGES, defaultValue = "5")
	private int pagerMaxPages;

	/** The session timeout period. */
	@Inject
	@Config(name = EmfConfigurationProperties.SESSION_TIMEOUT_PERIOD, defaultValue = "30")
	private Integer sessionTimeoutPeriod;

	/** The max allowed file size. */
	@Inject
	@Config(name = EmfConfigurationProperties.FILE_UPLOAD_MAXSIZE, defaultValue = "10000000")
	private Integer maxSize;

	@Produces
	@ApplicationScoped
	private ServletContext servletContext;

	/**
	 * Captures the ServletContext for injection providing.
	 * 
	 * @param event
	 *            ServletContextEvent.
	 */
	public void onApplicationStarted(@Observes ServletContextEvent event) {
		this.servletContext = event.getServletContext();
	}

	/**
	 * Getter method for contextPath.
	 * 
	 * @return the contextPath
	 */
	public String getContextPath() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext == null) {
			return contextPath;
		}
		return facesContext.getExternalContext().getRequestContextPath();
	}

	/**
	 * Setter method for contextPath.
	 * 
	 * @param contextPath
	 *            the contextPath to set
	 */
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	/**
	 * Getter method for applicationModeDevelopement.
	 * 
	 * @return the applicationModeDevelopement
	 */
	public Boolean getApplicationModeDevelopement() {
		return applicationModeDevelopement;
	}

	/**
	 * Setter method for applicationModeDevelopement.
	 * 
	 * @param applicationModeDevelopement
	 *            the applicationModeDevelopement to set
	 */
	public void setApplicationModeDevelopement(Boolean applicationModeDevelopement) {
		this.applicationModeDevelopement = applicationModeDevelopement;
	}

	/**
	 * Getter method for applicationFooterEnable.
	 * 
	 * @return the applicationFooterEnable
	 */
	public Boolean getApplicationFooterEnable() {
		return applicationFooterEnable;
	}

	/**
	 * Setter method for applicationFooterEnable.
	 * 
	 * @param applicationFooterEnable
	 *            the applicationFooterEnable to set
	 */
	public void setApplicationFooterEnable(Boolean applicationFooterEnable) {
		this.applicationFooterEnable = applicationFooterEnable;
	}

	/**
	 * Getter method for applicationModuleScheduleDisable.
	 * 
	 * @return the applicationModuleScheduleDisable
	 */
	public Boolean getApplicationModuleScheduleDisable() {
		return applicationModuleScheduleDisable;
	}

	/**
	 * Setter method for applicationModuleScheduleDisable.
	 * 
	 * @param applicationModuleScheduleDisable
	 *            the applicationModuleScheduleDisable to set
	 */
	public void setApplicationModuleScheduleDisable(Boolean applicationModuleScheduleDisable) {
		this.applicationModuleScheduleDisable = applicationModuleScheduleDisable;
	}

	/**
	 * Getter method for helpModuleLink.
	 * 
	 * @return the helpModuleLink
	 */
	public String getHelpModuleLink() {
		return helpModuleLink;
	}

	/**
	 * Setter method for helpModuleLink.
	 * 
	 * @param helpModuleLink
	 *            the helpModuleLink to set
	 */
	public void setHelpModuleLink(String helpModuleLink) {
		this.helpModuleLink = helpModuleLink;
	}

	/**
	 * Getter method for javascriptDebug.
	 * 
	 * @return the javascriptDebug
	 */
	public Boolean getJavascriptDebug() {
		return javascriptDebug;
	}

	/**
	 * Setter method for javascriptDebug.
	 * 
	 * @param javascriptDebug
	 *            the javascriptDebug to set
	 */
	public void setJavascriptDebug(Boolean javascriptDebug) {
		this.javascriptDebug = javascriptDebug;
	}

	/**
	 * Getter method for sessionTimeoutPeriod.
	 * 
	 * @return the sessionTimeoutPeriod
	 */
	public Integer getSessionTimeoutPeriod() {
		return sessionTimeoutPeriod;
	}

	/**
	 * Setter method for sessionTimeoutPeriod.
	 * 
	 * @param sessionTimeoutPeriod
	 *            the sessionTimeoutPeriod to set
	 */
	public void setSessionTimeoutPeriod(Integer sessionTimeoutPeriod) {
		this.sessionTimeoutPeriod = sessionTimeoutPeriod;
	}

	/**
	 * Getter method for pagerPageSize.
	 * 
	 * @return the pagerPageSize
	 */
	public int getPagerPageSize() {
		return pagerPageSize;
	}

	/**
	 * Getter method for pagerMaxPages.
	 * 
	 * @return the pagerMaxPages
	 */
	public int getPagerMaxPages() {
		return pagerMaxPages;
	}

	/**
	 * Getter method for maxSize.
	 * 
	 * @return the maxSize
	 */
	public int getMaxSize() {
		return maxSize;
	}

}
