package com.sirma.itt.cmf.codelist.services;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.codelist.ws.stub.Codelist;
import com.sirma.codelist.ws.stub.Item;
import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.codelist.CodelistPropertiesConstants;
import com.sirma.itt.emf.codelist.adapter.CMFCodelistAdapter;
import com.sirma.itt.emf.codelist.model.CodeValue;
import com.sirma.itt.emf.util.CollectionUtils;

/**
 * Implementation class for codelist adapter that works with the provider service for external
 * codelist access. The class translates the codelist requests to the producer and converts the data
 * to more suitable internal representation.
 * 
 * @author BBonev
 */
@ApplicationScoped
public class CodelistServerAccessor implements CMFCodelistAdapter {

	/**
	 * Comment for serialVersionUID.
	 */
	private static final long serialVersionUID = -7594165599485728850L;

	/** The Constant LANGUAGE_BG. */
	public static final String LANGUAGE_BG = new Locale("bg").getLanguage();

	/** The Constant LANGUAGE_EN. */
	public static final String LANGUAGE_EN = new Locale("en").getLanguage();

	/** The codelist service provider. */
	@Inject
	private CodelistServiceProvider codelistServiceProvider;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, CodeValue> getCodeValues(Integer codelist) {
		
		List<Item> list = codelistServiceProvider.getServiceInstance().getItems(codelist,
				(Date) null);
		if (list == null) {
			return CollectionUtils.emptyMap();
		}
		Map<String, CodeValue> map = CollectionUtils.createLinkedHashMap(list.size());
		for (Item item : list) {
			CodeValue value = convert(item);
			if (value != null) {
				map.put(value.getValue(), value);
			}
		}
		return map;
	}

	/**
	 * Convert the given Item to CMF {@link CodeValue}.
	 * 
	 * @param item
	 *            the item
	 * @return the code value
	 */
	private CodeValue convert(Item item) {
		CodeValue value = new CodeValue();
		value.setCodelist(item.getCodelistNumber().intValue());
		value.setValue(item.getValue());
		Map<String, Serializable> map = new HashMap<String, Serializable>(7);
		map.put(LANGUAGE_BG, item.getDescription());
		// this is a workaround to work the CMF with multiple languages until
		// the codelist server is updated to support them also
		map.put(LANGUAGE_EN, item.getDescription());
		if (StringUtils.isNotNullOrEmpty(item.getComment())) {
			map.put(CodelistPropertiesConstants.COMMENT, item.getComment());
		}
		if (StringUtils.isNotNullOrEmpty(item.getExtra1())) {
			map.put(CodelistPropertiesConstants.EXTRA1, item.getExtra1());
		}
		if (StringUtils.isNotNullOrEmpty(item.getExtra2())) {
			map.put(CodelistPropertiesConstants.EXTRA2, item.getExtra2());
		}
		if (StringUtils.isNotNullOrEmpty(item.getExtra3())) {
			map.put(CodelistPropertiesConstants.EXTRA3, item.getExtra3());
		}
		if (StringUtils.isNotNullOrEmpty(item.getMasterValue())) {
			map.put(CodelistPropertiesConstants.MASTER_VALUE, item.getMasterValue());
		}
		value.setProperties(Collections.unmodifiableMap(map));
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void resetCodelist() {
		codelistServiceProvider.resetCodelistServiceInstance();
	}

	/**
	 * Retrieves all CodeLists from database and convert it to key - value map.
	 * 
	 * @return all found codelists
	 */
	public Map<BigInteger, String> getAllCodelists() {
		Map<BigInteger, String> values = new HashMap<BigInteger, String>();
		List<Codelist> list = codelistServiceProvider.getServiceInstance().getAllCodelists();
		
	    for(Codelist codelists : list) {
	    	if(codelists != null) {
	    		values.put(codelists.getId(), codelists.getDescription());
	    	}
	    }
	    
	    return values;
	}

}
