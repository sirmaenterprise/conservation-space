package com.sirma.itt.seip.definition.convert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;

/**
 * Adds a converter from class to {@link DataTypeDefinition}
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionsConverter implements TypeConverterProvider {

	@Inject
	private DictionaryService dictionaryService;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Class.class, DataTypeDefinition.class,
				source -> dictionaryService.getDataTypeDefinition(source.getName()));
		converter.addConverter(String.class, DataTypeDefinition.class,
				source -> dictionaryService.getDataTypeDefinition(source));
		converter.addConverter(Long.class, DataTypeDefinition.class,
				source -> dictionaryService.getDataTypeDefinition(source));
	}

}
