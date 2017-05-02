package com.sirma.itt.migration.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * The Class FileRegistryEntry that represents a single row entry from table
 * <code>qvi_file_register</code>.
 * 
 * @author BBonev
 */
public class FileRegistryEntry implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = 4218463141570359531L;

	/** The source path. */
	private String sourcePath;

	/** The target path. */
	private String targetPath;

	/** The file name. */
	private String fileName;

	/** The dest file name. */
	private String destFileName;

	/** The crc. */
	private String crc;

	/** The status. */
	private Integer status;

	/** The modified by. */
	private String modifiedBy;

	/** The modified date. */
	private Date modifiedDate;

	/** The node id. */
	private String nodeId;

	/**
	 * Getter method for sourcePath.
	 *
	 * @return the sourcePath
	 */
	public String getSourcePath() {
		return sourcePath;
	}

	/**
	 * Setter method for sourcePath.
	 *
	 * @param sourcePath
	 *            the sourcePath to set
	 */
	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	/**
	 * Getter method for targetPath.
	 *
	 * @return the targetPath
	 */
	public String getTargetPath() {
		return targetPath;
	}

	/**
	 * Setter method for targetPath.
	 *
	 * @param targetPath
	 *            the targetPath to set
	 */
	public void setTargetPath(String targetPath) {
		this.targetPath = targetPath;
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
	 * Getter method for destFileName.
	 *
	 * @return the destFileName
	 */
	public String getDestFileName() {
		return destFileName;
	}

	/**
	 * Setter method for destFileName.
	 *
	 * @param destFileName
	 *            the destFileName to set
	 */
	public void setDestFileName(String destFileName) {
		this.destFileName = destFileName;
	}

	/**
	 * Getter method for crc.
	 *
	 * @return the crc
	 */
	public String getCrc() {
		return crc;
	}

	/**
	 * Setter method for crc.
	 *
	 * @param crc
	 *            the crc to set
	 */
	public void setCrc(String crc) {
		this.crc = crc;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * Getter method for modifiedBy.
	 *
	 * @return the modifiedBy
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * Setter method for modifiedBy.
	 *
	 * @param modifiedBy
	 *            the modifiedBy to set
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	/**
	 * Getter method for modifiedDate.
	 *
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * Setter method for modifiedDate.
	 *
	 * @param modifiedDate
	 *            the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * Getter method for nodeId.
	 *
	 * @return the nodeId
	 */
	public String getNodeId() {
		return nodeId;
	}

	/**
	 * Setter method for nodeId.
	 *
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileRegistryEntry [crc=").append(crc)
				.append(", sourcePath=").append(sourcePath)
				.append(", targetPath=").append(targetPath)
				.append(", fileName=").append(fileName).append(", status=")
				.append(status).append(", modifiedBy=").append(modifiedBy)
				.append(", modifiedDate=").append(modifiedDate)
				.append(", nodeId=").append(nodeId).append("]");
		return builder.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((crc == null) ? 0 : crc.hashCode());
		return result;
	}

	/**
	 * {@inheritDoc}
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
		FileRegistryEntry other = (FileRegistryEntry) obj;
		if (crc == null) {
			if (other.crc != null) {
				return false;
			}
		} else if (!crc.equals(other.crc)) {
			return false;
		}
		return true;
	}

}
