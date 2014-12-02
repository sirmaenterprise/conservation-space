package com.sirma.bam.cmf.integration.caseinstance.dashboard;

import com.sirma.itt.emf.instance.model.EmfInstance;
import com.sirma.itt.emf.resources.model.Resource;

/**
 * This class will holds all recent activities details.
 * 
 * @author cdimitrov
 */
public class ActivityDetails extends EmfInstance{
	
	private static final long serialVersionUID = 4944214846209934761L;

	/** Activity name. */
	private String name;
	
	/** Activity icon path. */
	private String iconPath;
	
	/** User that perform the activity.*/
	private Resource user;
	
	/** Activity action. */
	private String action;
	
	/** Time since the current data. */
	private String timesince;
	
	/** Avatar suffix. */
	private static final String IMAGE_SUFFIX = "-icon-16.png";
	
	/** The object url. */
	private String url;

	/**
	 * Getter for activity name.
	 * 
	 * @return activity name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter for activity name.
	 * 
	 * @param name activity name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Getter for icon path.
	 * 
	 * @return icon path
	 */
	public String getIconPath() {
		return iconPath;
	}

	/**
	 * Setter for icon path.
	 * 
	 * @param iconPath icon path
	 */
	public void setIconPath(String iconPath) {
		this.iconPath = iconPath.toLowerCase()+IMAGE_SUFFIX;
	}

	/**
	 * Getter for user that perform the activity.
	 * 
	 * @return user as resource
	 */
	public Resource getUser() {
		return user;
	}

	/**
	 * Setter for user that perform the activity.
	 * 
	 * @param user activity user
	 */
	public void setUser(Resource user) {
		this.user = user;
	}

	/**
	 * Getter for activity action.
	 * 
	 * @return activity action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * Setter for activity action.
	 * 
	 * @param action activity action
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	/**
	 * Getter for time since the current date.
	 * 
	 * @return time since current date
	 */
	public String getTimesince() {
		return timesince;
	}

	/**
	 * Setter for time since the current date.
	 * 
	 * @param timesince time since the current date
	 */
	public void setTimesince(String timesince) {
		this.timesince = timesince;
	}
	
	/**
	 * Getter for object url.
	 * 
	 * @return object url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Setter for object url.
	 * 
	 * @param url object url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
}
