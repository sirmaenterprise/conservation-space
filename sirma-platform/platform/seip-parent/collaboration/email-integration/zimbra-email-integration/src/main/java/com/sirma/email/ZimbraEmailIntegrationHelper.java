package com.sirma.email;

import java.util.ArrayList;
import java.util.List;

import com.sirma.sep.email.model.account.GenericAttribute;

import zimbra.KeyValuePair;
import zimbraadmin.Attr;
import zimbraadmin.CosInfoAttr;

/**
 * Utility class used for common operations between services and the mail server.
 *
 * @author g.tsankov
 */
public final class ZimbraEmailIntegrationHelper {

	/**
	 * No need for constructor.
	 */
	private ZimbraEmailIntegrationHelper() {
		// no need for constructor
	}

	/**
	 * Transforms a list of {@link Attr} or {@link CosInfoAttr} to a list of {@link GenericAttribute}
	 *
	 * @param attributes
	 *            list of zimbra attributes
	 * @return list of generic attributes.
	 */
	public static final List<GenericAttribute> toGenericAttributeList(List<? extends KeyValuePair> attributes) {
		List<GenericAttribute> convertedList = new ArrayList<>();

		for (KeyValuePair attribute : attributes) {
			convertedList.add(new GenericAttribute(attribute.getN(), attribute.getValue()));
		}

		return convertedList;
	}

	/**
	 * Creates a {@link Attr} from a {@link GenericAttribute}
	 *
	 * @param attribute
	 *            to convert
	 * @return converted attribute
	 */
	public static final Attr createZimbraAttribute(GenericAttribute attribute) {
		Attr attr = new Attr();
		attr.setN(attribute.getAttributeName());
		attr.setValue(attribute.getValue());
		return attr;
	}

	/**
	 * Creates a {@link Attr} from string name and value.
	 *
	 * @param attributeName
	 *            attribute name
	 * @param attributeValue
	 *            attribute value
	 * @return created attribute
	 */
	public static final Attr createZimbraAttribute(String attributeName, String attributeValue) {
		Attr attr = new Attr();
		attr.setN(attributeName);
		attr.setValue(attributeValue);
		return attr;
	}
}