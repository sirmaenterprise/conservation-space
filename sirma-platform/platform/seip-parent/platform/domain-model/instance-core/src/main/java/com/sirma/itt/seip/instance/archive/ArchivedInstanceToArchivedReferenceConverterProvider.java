package com.sirma.itt.seip.instance.archive;

import static com.sirma.itt.seip.domain.instance.DefaultProperties.SEMANTIC_TYPE;

import java.io.Serializable;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.sirma.itt.seip.convert.Converter;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.convert.TypeConverterProvider;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ArchivedInstance;
import com.sirma.itt.seip.domain.instance.ArchivedInstanceReference;
import com.sirma.itt.seip.domain.instance.InitializedInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.exceptions.InstanceNotFoundException;
import com.sirma.itt.seip.instance.InstanceTypeResolver;
import com.sirma.itt.seip.instance.InstanceTypes;
import com.sirma.itt.seip.instance.version.InstanceVersionService;

/**
 * Provider for conversion of instances to archived references and backward reference to instance.
 *
 * @author A. Kunchev
 */
@ApplicationScoped
public class ArchivedInstanceToArchivedReferenceConverterProvider implements TypeConverterProvider {

	@Inject
	private InstanceVersionService instanceVersionService;

	@Inject
	private DefinitionService definitionService;

	@Inject
	private InstanceTypeResolver instanceTypeResolver;

	@Inject
	private InstanceTypes instanceTypes;

	@Override
	public void register(TypeConverter converter) {
		converter.addConverter(ArchivedInstance.class, ArchivedInstanceReference.class, instanceToReference());
		converter.addConverter(ArchivedInstanceReference.class, InitializedInstance.class, referenceToInstance());
	}

	/**
	 * Implementation of {@link Converter#convert(Object)} which transforms passed {@link ArchivedInstance} to
	 * {@link ArchivedInstanceReference}. Primary used in {@link Instance#toReference()}.
	 *
	 * @return new {@link ArchivedInstanceReference} build from passed instance
	 */
	private Converter<ArchivedInstance, ArchivedInstanceReference> instanceToReference() {
		return source -> {
			Serializable id = source.getId();
			Optional<InstanceReference> originalReference = instanceTypeResolver
					.resolveReference(source.getTargetId());

			if (originalReference.isPresent()) {
				InstanceReference reference = originalReference.get();
				return new ArchivedInstanceReference(id.toString(), reference.getReferenceType(), reference.getType());
			}

			if (source.isValueNull(SEMANTIC_TYPE)) {
				throw new EmfRuntimeException("Could not convert instance with id - [" + id + "] to reference.");
			}

			Optional<InstanceType> type = instanceTypes.from(source.get(SEMANTIC_TYPE));
			if (!type.isPresent()) {
				throw new EmfRuntimeException("Could not resolve instance type for instnace with id - [" + id + "]");
			}

			InstanceType instanceType = type.get();
			DataTypeDefinition typeDefinition = definitionService.getDataTypeDefinition(instanceType.getId());
			return new ArchivedInstanceReference(id.toString(), typeDefinition, instanceType);
		};
	}

	/**
	 * Implementation of {@link Converter#convert(Object)} which transforms passed {@link ArchivedInstanceReference} to
	 * {@link InitializedInstance} that contains {@link ArchivedInstance} resolved for the passed reference. Primary
	 * used in {@link InstanceReference#toInstance()}.
	 *
	 * @return new {@link InitializedInstance} containing the {@link ArchivedInstance} for the passed reference
	 */
	private Converter<ArchivedInstanceReference, InitializedInstance> referenceToInstance() {
		return source -> {
			String id = source.getId();
			Instance instance = instanceVersionService.loadVersion(id);
			if (instance == null) {
				throw new InstanceNotFoundException(id);
			}

			return new InitializedInstance(instance);
		};
	}
}
