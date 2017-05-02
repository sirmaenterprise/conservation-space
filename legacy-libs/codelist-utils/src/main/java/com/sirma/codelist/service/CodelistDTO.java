package com.sirma.codelist.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.sirma.codelist.constants.MatchMode;
import com.sirma.codelist.ws.stub.Item;

/**
 * Contains search arguments used for search in codelist tables.
 * 
 * @author Yasen Terziivanov.
 */
public class CodelistDTO extends BaseCodelistDTO<Item> {

	/**
	 * Constructor.
	 */
	public CodelistDTO() {
		super(Item.class);
	}

	/**
	 * Master codelist number.
	 */
	private Integer masterCl;

	/**
	 * Set of codelist numbers to search for.
	 */
	private Set<Integer> codelistNumbers;

	/**
	 * Set of codevalues to search for.
	 */
	private Set<String> codeValues;

	/**
	 * Set of master cl values to search for.
	 */
	private Set<String> masterValues;

	/**
	 * Set of cl descriptions to search for.
	 */
	private Set<String> descriptions;

	/**
	 * Indicates whether a IN or NOT IN clause will be used for
	 * {@link #codeValues}.
	 */
	private boolean codelistValuesFilterMode = true;

	/**
	 * Represents match mode for LIKE expression.
	 */
	private MatchMode codelistValuesMatchMode;

	/**
	 * Indicates whether a IN or NOT IN clause will be used for
	 * {@link #masterValues}.
	 */
	private boolean masterValuesFilterMode = true;

	/**
	 * Represents match mode for LIKE expression.
	 */
	private MatchMode masterValuesMatchMode;

	/**
	 * Indicates whether a IN or NOT IN clause will be used for
	 * {@link #descriptions}.
	 */
	private boolean descriptionsFilterMode = true;

	/**
	 * Represents match mode for LIKE expression.
	 */
	private MatchMode descriptionsMatchMode;

	/**
	 * Map containing extra criteria fields.
	 */
	private Map<String, Set<String>> filterMapValues;

	/**
	 * Filter modes for {@link #filterMapValues}.
	 */
	private Map<String, Boolean> filterMapModes = new HashMap<String, Boolean>();

	/**
	 * MAtch modes for {@link #filterMapValues}.
	 */
	private Map<String, MatchMode> filterMapMatchModes = new HashMap<String, MatchMode>();

	/**
	 * Represents whether or not a disjunction should be constructed for
	 * {@link #filterMapValues}.
	 * <table border=1>
	 * <tr>
	 * <th>Value</th>
	 * <th>Description
	 * </tr>
	 * <tr>
	 * <td>true</td>
	 * <td>Construct disjunction for {@link #filterMapValues}</td>
	 * </tr>
	 * <tr>
	 * <td>false (Default)</td>
	 * <td>Construct conjunction for {@link #filterMapValues}</td>
	 * </tr>
	 * </table>
	 */
	private boolean filterDisjunction = false;

	/**
	 * Getter method for masterCl.
	 * 
	 * @return the masterCl
	 */
	public Integer getMasterCl() {
		return masterCl;
	}

	/**
	 * Setter method for masterCl.
	 * 
	 * @param masterCl
	 *            the masterCl to set
	 */
	public void setMasterCl(Integer masterCl) {
		this.masterCl = masterCl;
	}

	/**
	 * Getter method for codelistNumbers.
	 * 
	 * @return the codelistNumbers
	 */
	public Set<Integer> getCodelistNumbers() {
		return codelistNumbers;
	}

	/**
	 * Setter method for codelistNumbers.
	 * 
	 * @param codelistNumbers
	 *            the codelistNumbers to set
	 */
	public void setCodelistNumbers(Set<Integer> codelistNumbers) {
		this.codelistNumbers = codelistNumbers;
	}

	/**
	 * Getter method for codeValues.
	 * 
	 * @return the codeValues
	 */
	public Set<String> getCodeValues() {
		return codeValues;
	}

	/**
	 * Setter method for codeValues.
	 * 
	 * @param codeValues
	 *            the codeValues to set
	 */
	public void setCodeValues(Set<String> codeValues) {
		this.codeValues = codeValues;
	}

	/**
	 * Getter method for masterValues.
	 * 
	 * @return the masterValues
	 */
	public Set<String> getMasterValues() {
		return masterValues;
	}

	/**
	 * Setter method for masterValues.
	 * 
	 * @param masterValues
	 *            the masterValues to set
	 */
	public void setMasterValues(Set<String> masterValues) {
		this.masterValues = masterValues;
	}

	/**
	 * Getter method for descriptions.
	 * 
	 * @return the descriptions
	 */
	public Set<String> getDescriptions() {
		return descriptions;
	}

	/**
	 * Setter method for descriptions.
	 * 
	 * @param descriptions
	 *            the descriptions to set
	 */
	public void setDescriptions(Set<String> descriptions) {
		this.descriptions = descriptions;
	}

	/**
	 * Getter method for filterMapValues.
	 * 
	 * @return the filterMapValues
	 */
	public Map<String, Set<String>> getFilterMapValues() {
		return filterMapValues;
	}

	/**
	 * Setter method for filterMapValues.
	 * 
	 * @param filterMapValues
	 *            the filterMapValues to set
	 */
	public void setFilterMapValues(Map<String, Set<String>> filterMapValues) {
		this.filterMapValues = filterMapValues;
	}

	/**
	 * Getter method for filterMapModes.
	 * 
	 * @return the filterMapModes
	 */
	public Map<String, Boolean> getFilterMapModes() {
		return filterMapModes;
	}

	/**
	 * Setter method for filterMapModes.
	 * 
	 * @param filterMapModes
	 *            the filterMapModes to set
	 */
	public void setFilterMapModes(Map<String, Boolean> filterMapModes) {
		this.filterMapModes = filterMapModes;
	}

	/**
	 * Getter method for filterMapMatchModes.
	 * 
	 * @return the filterMapMatchModes
	 */
	public Map<String, MatchMode> getFilterMapMatchModes() {
		return filterMapMatchModes;
	}

	/**
	 * Setter method for filterMapMatchModes.
	 * 
	 * @param filterMapMatchModes
	 *            the filterMapMatchModes to set
	 */
	public void setFilterMapMatchModes(
			Map<String, MatchMode> filterMapMatchModes) {
		this.filterMapMatchModes = filterMapMatchModes;
	}

	/**
	 * Getter method for codelistValuesFilterMode.
	 * 
	 * @return the codelistValuesFilterMode
	 */
	public boolean isCodelistValuesFilterMode() {
		return codelistValuesFilterMode;
	}

	/**
	 * Setter method for codelistValuesFilterMode.
	 * 
	 * @param codelistValuesFilterMode
	 *            the codelistValuesFilterMode to set
	 */
	public void setCodelistValuesFilterMode(boolean codelistValuesFilterMode) {
		this.codelistValuesFilterMode = codelistValuesFilterMode;
	}

	/**
	 * Getter method for codelistValuesMatchModel.
	 * 
	 * @return the codelistValuesMatchModel
	 */
	public MatchMode getCodelistValuesMatchMode() {
		return codelistValuesMatchMode;
	}

	/**
	 * Setter method for codelistValuesMatchModel.
	 * 
	 * @param codelistValuesMatchModel
	 *            the codelistValuesMatchModel to set
	 */
	public void setCodelistValuesMatchMode(MatchMode codelistValuesMatchModel) {
		this.codelistValuesMatchMode = codelistValuesMatchModel;
	}

	/**
	 * Getter method for masterValuesFilterMode.
	 * 
	 * @return the masterValuesFilterMode
	 */
	public boolean isMasterValuesFilterMode() {
		return masterValuesFilterMode;
	}

	/**
	 * Setter method for masterValuesFilterMode.
	 * 
	 * @param masterValuesFilterMode
	 *            the masterValuesFilterMode to set
	 */
	public void setMasterValuesFilterMode(boolean masterValuesFilterMode) {
		this.masterValuesFilterMode = masterValuesFilterMode;
	}

	/**
	 * Getter method for masterValuesMatchMode.
	 * 
	 * @return the masterValuesMatchMode
	 */
	public MatchMode getMasterValuesMatchMode() {
		return masterValuesMatchMode;
	}

	/**
	 * Setter method for masterValuesMatchMode.
	 * 
	 * @param masterValuesMatchMode
	 *            the masterValuesMatchMode to set
	 */
	public void setMasterValuesMatchMode(MatchMode masterValuesMatchMode) {
		this.masterValuesMatchMode = masterValuesMatchMode;
	}

	/**
	 * Getter method for descriptionsFilterMode.
	 * 
	 * @return the descriptionsFilterMode
	 */
	public boolean isDescriptionsFilterMode() {
		return descriptionsFilterMode;
	}

	/**
	 * Setter method for descriptionsFilterMode.
	 * 
	 * @param descriptionsFilterMode
	 *            the descriptionsFilterMode to set
	 */
	public void setDescriptionsFilterMode(boolean descriptionsFilterMode) {
		this.descriptionsFilterMode = descriptionsFilterMode;
	}

	/**
	 * Getter method for descriptionsMatchMode.
	 * 
	 * @return the descriptionsMatchMode
	 */
	public MatchMode getDescriptionsMatchMode() {
		return descriptionsMatchMode;
	}

	/**
	 * Setter method for descriptionsMatchMode.
	 * 
	 * @param descriptionsMatchMode
	 *            the descriptionsMatchMode to set
	 */
	public void setDescriptionsMatchMode(MatchMode descriptionsMatchMode) {
		this.descriptionsMatchMode = descriptionsMatchMode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("codelistNumbers: " + codelistNumbers);
		builder.append("codeValues: " + codeValues);
		builder.append("masterValues: " + masterValues);
		builder.append("descriptions: " + descriptions);
		builder.append("codelistValuesFilterMode: " + codelistValuesFilterMode);
		builder.append("codelistValuesMatchMode: " + codelistValuesMatchMode);
		builder.append("masterValuesFilterMode: " + masterValuesFilterMode);
		builder.append("masterValuesMatchMode: " + masterValuesMatchMode);
		builder.append("descriptionsFilterMode: " + descriptionsFilterMode);
		builder.append("descriptionsMatchMode: " + descriptionsMatchMode);
		builder.append("filterMapValues: " + filterMapValues);
		builder.append("filterMapModes: " + filterMapModes);
		builder.append("filterMapMatchModes: " + filterMapMatchModes);
		return builder.toString();
	}

	/**
	 * Getter method for filterDisjunction.
	 * 
	 * @return the filterDisjunction
	 */
	public boolean isFilterDisjunction() {
		return filterDisjunction;
	}

	/**
	 * Setter method for filterDisjunction.
	 * 
	 * @param filterDisjunction
	 *            the filterDisjunction to set
	 */
	public void setFilterDisjunction(boolean filterDisjunction) {
		this.filterDisjunction = filterDisjunction;
	}

	/**
	 * Initialize the sets for base filtering.
	 */
	public void initForBaseFiltering() {
		setCodelistNumbers(new LinkedHashSet<Integer>());
		setCodeValues(new LinkedHashSet<String>());
		setMasterValues(new LinkedHashSet<String>());
		setDescriptions(new LinkedHashSet<String>());
	}

	/**
	 * Initialize the maps for extra map filtering.
	 */
	public void initForMapFiltering() {
		setFilterMapValues(new LinkedHashMap<String, Set<String>>());
		setFilterMapMatchModes(new LinkedHashMap<String, MatchMode>());
		setFilterMapModes(new LinkedHashMap<String, Boolean>());
	}
}
