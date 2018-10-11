package com.sirma.sep.export.xlsx.components;

import java.io.Serializable;

/**
 * Class representation of link enformation (addres and label).
 *
 * @author Boyan Tonchev.
 */
public class ExportExcelLink implements Serializable {

	private String address;

	private String label;

	/**
	 * Initialize object.
	 *
	 * @param address
	 * 		- the addres of link.
	 * @param label
	 * 		- the label of link.
	 */
	public ExportExcelLink(String address, String label) {
		this.address = address;
		this.label = label;
	}

	/**
	 * Getter method for link addres.
	 *
	 * @return -the link address.
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Geter method for link label.
	 *
	 * @return - the link label.
	 */
	public String getLabel() {
		return label;
	}
}
