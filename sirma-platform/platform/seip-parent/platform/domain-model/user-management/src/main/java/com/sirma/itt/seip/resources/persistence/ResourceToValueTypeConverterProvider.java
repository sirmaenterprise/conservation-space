package com.sirma.itt.seip.resources.persistence;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.rdf4j.model.Value;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.resources.EmfGroup;
import com.sirma.itt.seip.resources.EmfUser;
import com.sirma.itt.seip.resources.Resource;
import com.sirma.itt.semantic.NamespaceRegistryService;

/**
 * {@link TypeConverterProvider} to register converting the user and group objects to semantic values
 * 
 * @author BBonev
 */
@Singleton
public class ResourceToValueTypeConverterProvider implements TypeConverterProvider {

	@Inject
	private NamespaceRegistryService namespaceRegistryService;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(EmfUser.class, Value.class, this::convertResource);
		converter.addConverter(EmfGroup.class, Value.class, this::convertResource);
	}

	Value convertResource(Resource source) {
		if (source.getId() == null) {
			return null;
		}
		return namespaceRegistryService.buildUri(source.getId().toString());
	}
}
