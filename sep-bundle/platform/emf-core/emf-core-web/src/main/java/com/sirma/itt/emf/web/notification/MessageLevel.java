package com.sirma.itt.emf.web.notification;

/**
 * The Enum MessageLevel.
 * 
 * @author svelikov
 */
public enum MessageLevel {

	/** The info. */
	INFO("info-message"),
	/** The warn. */
	WARN("warn-message"),
	/** The error. */
	ERROR("error-message"),
	/** The fatal. */
	FATAL("fatal-message");

	/** The level. */
	private String level;

	/**
	 * Instantiates a new message level.
	 * 
	 * @param level
	 *            the level
	 */
	private MessageLevel(String level) {
		this.level = level;
	}

	/**
	 * Getter method for level.
	 * 
	 * @return the level
	 */
	public String getLevel() {
		return level;
	}

	/**
	 * Setter method for level.
	 * 
	 * @param level
	 *            the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}

}