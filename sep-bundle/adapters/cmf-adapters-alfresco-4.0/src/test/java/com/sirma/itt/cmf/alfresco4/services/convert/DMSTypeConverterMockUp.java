package com.sirma.itt.cmf.alfresco4.services.convert;

import java.util.Properties;

import com.sirma.itt.emf.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.evaluation.ExpressionsManager;

/**
 * The Class DMSTypeConvertorMockUp - mockup for {@link DMSTypeConvertor}. Because of package
 * visibility it is stored on same path
 */
public class DMSTypeConverterMockUp extends DMSTypeConverter {

	/**
	 * Instantiates a new dMS type convertor mock up.
	 *
	 * @param typeConverter
	 *            the type converter
	 * @param dictionaryService
	 *            the dictionary service
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param model
	 *            the model
	 * @param cacheContext
	 *            the cache context
	 * @param convertorProperties
	 *            the convertor properties
	 */
	public DMSTypeConverterMockUp(TypeConverter typeConverter, DictionaryService dictionaryService,
			ExpressionsManager evaluatorManager, String model,
			EntityLookupCacheContext cacheContext, Properties convertorProperties) {
		super(typeConverter, dictionaryService, evaluatorManager, new ConvertibleModels(model),
				cacheContext, convertorProperties);
	}

	/**
	 * Creates the mockup.
	 *
	 * @param converter
	 *            the converter
	 * @param dictionaryService
	 *            the dictionary service
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param model
	 *            the model
	 * @param cacheContext
	 *            the cache context
	 * @param convertorProperties
	 *            the properties loaded
	 * @return the dMS type converter created with the given params
	 */
	public static DMSTypeConverter create(TypeConverter converter,
			DictionaryService dictionaryService, ExpressionsManager evaluatorManager, String model,
			EntityLookupCacheContext cacheContext, Properties convertorProperties) {
		return new DMSTypeConverterMockUp(converter, dictionaryService, evaluatorManager, model,
				cacheContext, convertorProperties);
	}
}
