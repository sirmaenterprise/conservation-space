/*
 * Copyright (c) 2012 17.09.2012 , Sirma ITT.
 */
package com.sirma.cmf.web.upload;

import java.io.InputStream;

/**
 * The Class UploadedFile.
 * 
 * @author Borislav Banchev
 */
public class UploadedFile {

	/** The Name. */
	private String name;

	/** The mime. */
	private String mime;

	/** The length. */
	private long length;

	/** The data. */
	private InputStream data;

	/**
	 * Gets the data.
	 * 
	 * @return the data
	 */
	public InputStream getData() {
		return data;
	}

	/**
	 * Sets the data.
	 * 
	 * @param data
	 *            the new data
	 */
	public void setData(InputStream data) {
		this.data = data;
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
		// int extDot = name.lastIndexOf('.');
		// if (extDot > 0) {
		// String extension = name.substring(extDot + 1);
		// if ("bmp".equals(extension)) {
		// mime = "image/bmp";
		// } else if ("jpg".equals(extension)) {
		// mime = "image/jpeg";
		// } else if ("gif".equals(extension)) {
		// mime = "image/gif";
		// } else if ("png".equals(extension)) {
		// mime = "image/png";
		// } else {
		// mime = "image/unknown";
		// }
		// }
	}

	/**
	 * Gets the length.
	 * 
	 * @return the length
	 */
	public long getLength() {
		return length;
	}

	/**
	 * Sets the length.
	 * 
	 * @param length
	 *            the new length
	 */
	public void setLength(long length) {
		this.length = length;
	}

	/**
	 * Sets the mimetype.
	 * 
	 * @param contentType
	 *            is the mimetype for the file
	 */
	public void setMimetype(String contentType) {
		mime = contentType;
	}

	/**
	 * Getter method for mime.
	 * 
	 * @return the mime
	 */
	public String getMimetype() {
		return mime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UploadedFile [name=");
		builder.append(name);
		builder.append(", mime=");
		builder.append(mime);
		builder.append(", length=");
		builder.append(length);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (int) (length ^ (length >>> 32));
		result = (prime * result) + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param obj
	 *            is the object to compare
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		UploadedFile other = (UploadedFile) obj;
		if (length != other.length) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
