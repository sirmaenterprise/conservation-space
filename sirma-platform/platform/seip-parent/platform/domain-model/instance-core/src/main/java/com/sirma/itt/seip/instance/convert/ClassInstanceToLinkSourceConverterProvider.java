package com.sirma.itt.seip.instance.convert;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.SemanticDefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.model.LinkSourceId;

/**
 * Converts class instance<->reference using {@link SemanticDefinitionService} to load the classInstance.
 *
 * @author bbanchev
 */
@ApplicationScoped
public class ClassInstanceToLinkSourceConverterProvider extends AbstractInstanceToInstanceReferenceConverterProvider {

	/** The Constant DEFINITION_TYPE. */
	protected static final String DEFINITION_TYPE = ClassInstance.class.getSimpleName().toLowerCase();

	/** The semantic definition service. */
	@Inject
	private SemanticDefinitionService semanticDefinitionService;

	/**
	 * Convert class instance to link source converter.
	 *
	 * @param source
	 *            the source
	 * @return the link source id
	 */
	public LinkSourceId convertClassInstanceToLinkSourceConverter(ClassInstance source) {
		DataTypeDefinition dataTypeDefinition = definitionService.getDataTypeDefinition(DEFINITION_TYPE);
		return new LinkSourceId(Objects.toString(source.getId(), null), dataTypeDefinition, source);
	}

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(ClassInstance.class, LinkSourceId.class,
				this::convertClassInstanceToLinkSourceConverter);
		converter.addConverter(ClassInstance.class, InstanceReference.class,
				this::convertClassInstanceToLinkSourceConverter);
		converter.addConverter(LinkSourceId.class, ClassInstance.class,
				source -> semanticDefinitionService.getClassInstance(source.getId()));
		converter.addConverter(InstanceReference.class, ClassInstance.class,
				source -> semanticDefinitionService.getClassInstance(source.getId()));
	}
}