package com.sirma.itt.cmf.alfresco4.services.convert;

import java.util.Properties;
import java.util.function.Supplier;

import com.sirma.itt.seip.cache.lookup.EntityLookupCacheContext;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.expressions.ExpressionsManager;

/**
 * The Class DMSTypeConvertorMockUp - mockup for {@link DMSTypeConvertor}. Because of package visibility it is stored on
 * same path
 */
public class DMSTypeConverterMockUp extends DMSTypeConverterImpl {

	/**
	 * Instantiates a new dMS type convertor mock up.
	 *
	 * @param typeConverter
	 *            the type converter
	 * @param definitionService
	 *            the definition service
	 * @param evaluatorManager
	 *            the evaluator manager
	 * @param model
	 *            the model
	 * @param cacheContext
	 *            the cache context
	 * @param convertorProperties
	 *            the convertor properties
	 */
	public DMSTypeConverterMockUp(TypeConverter typeConverter, DefinitionService definitionService,
			ExpressionsManager evaluatorManager, String model, EntityLookupCacheContext cacheContext,
			Supplier<Properties> convertorProperties, Supplier<DefinitionModel> baseModelSupplier,
			HashCalculator hashCalculator) {
		super(typeConverter, definitionService, evaluatorManager, new ConvertibleModels(model), cacheContext,
				convertorProperties, baseModelSupplier, hashCalculator);
	}

	/**
	 * Creates the mockup.
	 *
	 * @param converter
	 *            the converter
	 * @param definitionService
	 *            the definition service
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
	public static DMSTypeConverterImpl create(TypeConverter converter, DefinitionService definitionService,
			ExpressionsManager evaluatorManager, String model, EntityLookupCacheContext cacheContext,
			Supplier<Properties> convertorProperties, Supplier<DefinitionModel> baseModelSupplier) {
		return new DMSTypeConverterMockUp(converter, definitionService, evaluatorManager, model, cacheContext,
				convertorProperties, baseModelSupplier, null);
	}
}
