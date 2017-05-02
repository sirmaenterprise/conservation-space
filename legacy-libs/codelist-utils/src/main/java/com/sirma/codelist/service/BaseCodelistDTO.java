package com.sirma.codelist.service;

import java.util.Date;
import java.util.List;

/**
 * Base args for searching in codelists.
 * 
 * @param <T>
 *            Type of the target entity.
 * @author Yasen Terziivanov.
 */
public class BaseCodelistDTO<T> {

	/**
	 * Constructor.
	 * 
	 * @param targetEntity
	 *            target entity to select from.
	 */
	public BaseCodelistDTO(Class<T> targetEntity) {
		this.targetEntity = targetEntity;
	}

	/**
	 * Matched results.
	 */
	private List<T> result;

	/**
	 * Root entity to select from.
	 */
	private Class<T> targetEntity;

	/**
	 * Order by property name.
	 */
	private String orderBy;

	/**
	 * ASC/DESC.
	 */
	private boolean orderDirection = true;

	/**
	 * Beginning of validity period. Can be null.
	 */
	private Date validFrom;

	/**
	 * End of validity period. Can be null.
	 */
	private Date validTo;

	/**
	 * Getter method for result.
	 * 
	 * @return the result
	 */
	public List<T> getResult() {
		return result;
	}

	/**
	 * Setter method for result.
	 * 
	 * @param result
	 *            the result to set
	 */
	public void setResult(List<T> result) {
		this.result = result;
	}

	/**
	 * Getter method for targetEntity.
	 * 
	 * @return the targetEntity
	 */
	public Class<T> getTargetEntity() {
		return targetEntity;
	}

	/**
	 * Setter method for targetEntity.
	 * 
	 * @param targetEntity
	 *            the targetEntity to set
	 */
	public void setTargetEntity(Class<T> targetEntity) {
		this.targetEntity = targetEntity;
	}

	/**
	 * Getter method for orderBy.
	 * 
	 * @return the orderBy
	 */
	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * Setter method for orderBy.
	 * 
	 * @param orderBy
	 *            the orderBy to set
	 */
	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	/**
	 * Getter method for orderDirection.
	 * 
	 * @return the orderDirection
	 */
	public boolean isOrderDirection() {
		return orderDirection;
	}

	/**
	 * Setter method for orderDirection.
	 * 
	 * @param orderDirection
	 *            the orderDirection to set
	 */
	public void setOrderDirection(boolean orderDirection) {
		this.orderDirection = orderDirection;
	}

	/**
	 * Getter method for validFrom.
	 * 
	 * @return the validFrom
	 */
	public Date getValidFrom() {
		return validFrom;
	}

	/**
	 * Setter method for validFrom.
	 * 
	 * @param validFrom
	 *            the validFrom to set
	 */
	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	/**
	 * Getter method for validTo.
	 * 
	 * @return the validTo
	 */
	public Date getValidTo() {
		return validTo;
	}

	/**
	 * Setter method for validTo.
	 * 
	 * @param validTo
	 *            the validTo to set
	 */
	public void setValidTo(Date validTo) {
		this.validTo = validTo;
	}

}
