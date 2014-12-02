package com.sirma.cmf.web.search;

import java.io.Serializable;
import java.util.List;

import javax.faces.model.DataModel;

/**
 * Implementation of DataModel allowing real pagination.
 * 
 * @param <E>
 *            model data type.
 * @author Adrian Mitev
 */
public class PagedListDataModel<E> extends DataModel<E> implements Serializable {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -1421416411107858476L;

	/**
	 * The current row index.
	 */
	private int rowIndex = -1;

	/**
	 * Total number of rows.
	 */
	private int totalNumRows;

	/**
	 * Rows in one page.
	 */
	private final int pageSize;

	/**
	 * The underlying list.
	 */
	private List<? extends E> list;

	/**
	 * Constructor initializing the mode.
	 * 
	 * @param list
	 *            the underlying list.
	 * @param totalNumRows
	 *            total rows.
	 * @param pageSize
	 *            rows per page.
	 */
	public PagedListDataModel(List<E> list, int totalNumRows, int pageSize) {
		setData(list);
		this.totalNumRows = totalNumRows;
		this.pageSize = pageSize;
	}

	/**
	 * Checks if the next row of the table is available.
	 * 
	 * @return true if the row is available, otherwise false.
	 */
	@Override
	public boolean isRowAvailable() {
		if (list == null) {
			return false;
		}
		return getRowIndex() >= 0 && getRowIndex() < list.size();
	}

	/**
	 * Getter method for the total row count.
	 * 
	 * @return the total row count.
	 */
	@Override
	public int getRowCount() {
		return totalNumRows;
	}

	/**
	 * Retrieves the object for the current row.
	 * 
	 * @return the object representing the current row.
	 */
	@Override
	public E getRowData() {
		if (list == null) {
			return null;
		} else if (!isRowAvailable()) {
			throw new IllegalArgumentException();
		}
		int dataIndex = getRowIndex();
		return list.get(dataIndex);
	}

	/**
	 * Computes the current row index.
	 * 
	 * @return the current row index.
	 */
	@Override
	public int getRowIndex() {
		return rowIndex % pageSize;
	}

	/**
	 * Setter method for the row index.
	 * 
	 * @param rowIndex
	 *            the row index to be set.
	 */
	@Override
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/**
	 * Getter method for the underlying list.
	 * 
	 * @return the underlying list.
	 */
	@Override
	public Object getWrappedData() {
		return list;
	}

	/**
	 * Getter method for the underlying list.
	 * 
	 * @return the underlying list.
	 */
	public List<? extends E> getData() {
		return list;
	}

	/**
	 * Setter method for the underlying list.
	 * 
	 * @param data
	 *            the underlying list
	 */
	public void setData(List<? extends E> data) {
		list = data;
	}

	/**
	 * Setter method for the underlying model.
	 * 
	 * @param list
	 *            the underlying list.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setWrappedData(Object list) {
		this.list = (List<E>) list;
	}

	/**
	 * Setter method for totalNumRows.
	 * 
	 * @param totalNumRows
	 *            the totalNumRows to set
	 */
	public void setTotalNumRows(int totalNumRows) {
		this.totalNumRows = totalNumRows;
	}

}
