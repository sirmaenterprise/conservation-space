package com.sirma.itt.seip.rule.model;

import java.util.Map;

import com.sirma.itt.seip.collections.CollectionUtils;

/**
 * On duplicate configuration.
 *
 * @author BBonev
 */
public class OnDuplicateConfig {

	/** The operation. */
	private OnDuplicateOperation operation = OnDuplicateOperation.CONCATENATE;
	/** The separator. */
	private String separator = ",";

	/**
	 * Parses the given configuration object to {@link OnDuplicateConfig}.
	 *
	 * @param duplicateConf
	 *            the duplicate conf
	 * @return the on duplicate config
	 */
	public static OnDuplicateConfig parse(Map<String, Object> duplicateConf) {
		if (duplicateConf == null) {
			return null;
		}
		OnDuplicateConfig config = new OnDuplicateConfig();
		config.operation = OnDuplicateOperation.parse(duplicateConf.get("operation"));
		config.separator = (String) duplicateConf.get("separator");
		return config;
	}

	/**
	 * Converts the given object to map that is valid to be passed to {@link #parse(Map)} method.
	 *
	 * @param duplicateConf
	 *            the duplicate conf to read
	 * @return the map that contains the source properties or <code>null</code> if the source is <code>null</code>
	 */
	public static Map<String, Object> toMap(OnDuplicateConfig duplicateConf) {
		if (duplicateConf == null) {
			return null;
		}
		Map<String, Object> map = CollectionUtils.createHashMap(2);
		CollectionUtils.addNonNullValue(map, "operation", duplicateConf.getOperation());
		CollectionUtils.addNonNullValue(map, "separator", duplicateConf.getSeparator());
		return map;
	}

	/**
	 * Getter method for operation.
	 *
	 * @return the operation
	 */
	public OnDuplicateOperation getOperation() {
		return operation;
	}

	/**
	 * Getter method for separator.
	 *
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

}