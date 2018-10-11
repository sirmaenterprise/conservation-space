package com.sirma.itt.seip.definition.convert;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;

/**
 * Adds a converter from class to {@link DataTypeDefinition}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionsConverter implements TypeConverterProvider {

	@Inject
	private DefinitionService definitionService;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(Class.class, DataTypeDefinition.class,
				source -> definitionService.getDataTypeDefinition(source.getName()));
		converter.addConverter(String.class, DataTypeDefinition.class, definitionService::getDataTypeDefinition);
		converter.addConverter(Long.class, DataTypeDefinition.class, definitionService::getDataTypeDefinition);
	}
}
