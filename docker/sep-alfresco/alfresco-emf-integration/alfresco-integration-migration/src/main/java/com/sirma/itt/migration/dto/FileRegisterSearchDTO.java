package com.sirma.itt.migration.dto;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.sirma.itt.migration.constants.MigrationStatus;

/**
 * DTO object used for searching in the file register
 *
 * @author BBonev
 */
public class FileRegisterSearchDTO {
	private List<FileRegistryEntry> result;
	private int pageSize = 50;
	private int skipCount;
	private int totalCount;
	private String source;
	private String target;
	private MigrationStatus status;
	private Date modifiedFrom;
	private Date modifiedTo;
	private String nameFilter;
	private String modifiedBy;
	private List<String> include = new LinkedList<String>();
	private List<String> exclude = new LinkedList<String>();

	/**
	 * Getter method for result.
	 *
	 * @return the result
	 */
	public List<FileRegistryEntry> getResult() {
		return result;
	}

	/**
	 * Setter method for result.
	 *
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<FileRegistryEntry> result) {
		this.result = result;
	}

	/**
	 * Getter method for pageSize.
	 *
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * Setter method for pageSize.
	 *
	 * @param pageSize
	 *            the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * Getter method for totalCount.
	 *
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Setter method for totalCount.
	 *
	 * @param totalCount
	 *            the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * Getter method for source.
	 *
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Setter method for source.
	 *
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Getter method for status.
	 *
	 * @return the status
	 */
	public MigrationStatus getStatus() {
		return status;
	}

	/**
	 * Setter method for status.
	 *
	 * @param status
	 *            the status to set
	 */
	public void setStatus(MigrationStatus status) {
		this.status = status;
	}

	/**
	 * Getter method for modifiedFrom.
	 *
	 * @return the modifiedFrom
	 */
	public Date getModifiedFrom() {
		return modifiedFrom;
	}

	/**
	 * Setter method for modifiedFrom.
	 *
	 * @param modifiedFrom
	 *            the modifiedFrom to set
	 */
	public void setModifiedFrom(Date modifiedFrom) {
		this.modifiedFrom = modifiedFrom;
	}

	/**
	 * Getter method for modifiedTo.
	 *
	 * @return the modifiedTo
	 */
	public Date getModifiedTo() {
		return modifiedTo;
	}

	/**
	 * Setter method for modifiedTo.
	 *
	 * @param modifiedTo
	 *            the modifiedTo to set
	 */
	public void setModifiedTo(Date modifiedTo) {
		this.modifiedTo = modifiedTo;
	}

	/**
	 * Getter method for nameFilter.
	 *
	 * @return the nameFilter
	 */
	public String getNameFilter() {
		return nameFilter;
	}

	/**
	 * Setter method for nameFilter.
	 *
	 * @param nameFilter
	 *            the nameFilter to set
	 */
	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
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
	 * Getter method for skipCount.
	 *
	 * @return the skipCount
	 */
	public int getSkipCount() {
		return skipCount;
	}

	/**
	 * Setter method for skipCount.
	 *
	 * @param skipCount
	 *            the skipCount to set
	 */
	public void setSkipCount(int skipCount) {
		this.skipCount = skipCount;
	}

	/**
	 * Setter method for target.
	 *
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * Getter method for target.
	 *
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * Getter method for include.
	 *
	 * @return the include
	 */
	public List<String> getInclude() {
		return include;
	}

	/**
	 * Setter method for include.
	 *
	 * @param include
	 *            the include to set
	 */
	public void setInclude(List<String> include) {
		this.include = include;
	}

	/**
	 * Getter method for exclude.
	 *
	 * @return the exclude
	 */
	public List<String> getExclude() {
		return exclude;
	}

	/**
	 * Setter method for exclude.
	 *
	 * @param exclude
	 *            the exclude to set
	 */
	public void setExclude(List<String> exclude) {
		this.exclude = exclude;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("FileRegisterSearchDTO [result=").append(result)
				.append(", pageSize=").append(pageSize).append(", skipCount=")
				.append(skipCount).append(", totalCount=").append(totalCount)
				.append(", source=").append(source).append(", target=")
				.append(target).append(", status=").append(status)
				.append(", modifiedFrom=").append(modifiedFrom)
				.append(", modifiedTo=").append(modifiedTo)
				.append(", nameFilter=").append(nameFilter)
				.append(", modifiedBy=").append(modifiedBy)
				.append(", include=").append(include).append(", exclude=")
				.append(exclude).append("]");
		return builder.toString();
	}

}
