package com.sirma.itt.emf.mail;

import java.io.Serializable;

/**
 * Holds an information for a single mail attachment.
 * 
 * @author Adrian Mitev
 */
public class MailAttachment implements Serializable {

	private static final long serialVersionUID = 7878141908230817025L;

	private String fileName;

	private String mimeType;

	private byte[] content;

	/**
	 * Initializes properties.
	 * 
	 * @param fileName
	 *            name of the attachment.
	 * @param mimeType
	 *            content type of the attachment.
	 * @param content
	 *            attachment content.
	 */
	public MailAttachment(String fileName, String mimeType, byte[] content) {
		this.fileName = fileName;
		this.mimeType = mimeType;
		this.content = content;
	}

	/**
	 * Default constructor
	 */
	public MailAttachment() {
	}

	/**
	 * Getter method for fileName.
	 * 
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Setter method for fileName.
	 * 
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Getter method for mimeType.
	 * 
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Setter method for mimeType.
	 * 
	 * @param mimeType
	 *            the mimeType to set
	 */
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Getter method for content.
	 * 
	 * @return the content
	 */
	public byte[] getContent() {
		return content;
	}

	/**
	 * Setter method for content.
	 * 
	 * @param content
	 *            the content to set
	 */
	public void setContent(byte[] content) {
		this.content = content;
	}

}
