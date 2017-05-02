package com.sirmaenterprise.sep.helprequest;

/**
 * Used to hold all data needed for help request res service. Type and subject will be used from service to generate
 * subject of mail. Type is code from code list. The service will fetch it value and concatenate it with subject.
 * For example: "code_value : subject"
 * 
 * @author Boyan Tonchev
 */
public class HelpRequestMessage {

	/** The subject of mail. */
	private String subject;

	/** The type of mail. For example: Bug, Question ...*/
	private String type;

	/** The content of mail. */
	private String description;

	/**
	 * Gets the subject entered by user.
	 *
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Sets the subject entered by user.
	 *
	 * @param subject the new subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Gets the type entered by user.
	 *
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the type entered by user.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Gets the content of mail entered by user.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the content of mail entered by user.
	 *
	 * @param description the new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
