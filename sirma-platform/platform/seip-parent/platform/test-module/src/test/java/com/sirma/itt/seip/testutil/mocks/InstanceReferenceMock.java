/**
 *
 */
package com.sirma.itt.seip.testutil.mocks;

import java.util.HashMap;

import org.mockito.MockingDetails;
import org.mockito.Mockito;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.ClassInstance;
import com.sirma.itt.seip.domain.instance.EmfInstance;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.domain.instance.InstanceReferenceImpl;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.exception.EmfRuntimeException;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.resources.EmfResource;
import com.sirma.itt.seip.util.ReflectionUtils;

/**
 * Mock for {@link InstanceReference} that allow different initializations. The implementation will mimic the original
 * implementation in the most cases but it's not kryo serializable.
 *
 * @author BBonev
 */
public class InstanceReferenceMock extends InstanceReferenceImpl {

	private static final long serialVersionUID = 363730152587158485L;

	/**
	 * Instantiates a new default reference mock with null parameters
	 */
	public InstanceReferenceMock() {
		this(null, null, null, null);
	}

	/**
	 * Instantiates a new default reference mock based on id and data type class
	 *
	 * @param id
	 *            is the instance id
	 * @param type
	 *            is the data type class
	 */
	public InstanceReferenceMock(String id, Class<?> type) {
		this(id, new DataTypeDefinitionMock(type, null), null, null);
	}

	/**
	 * Instantiates a new instance reference mock based on existing instance and set the instance reference to the
	 * initialized mock reference
	 *
	 * @param source
	 *            the source instance
	 */
	public InstanceReferenceMock(Instance source) {
		this((String) source.getId(), new DataTypeDefinitionMock(source), source, null);
		setType(source.type());
	}

	/**
	 * Instantiates a new link source id.
	 *
	 * @param sourceId
	 *            the source id
	 * @param sourceType
	 *            the source type
	 */
	public InstanceReferenceMock(String sourceId, DataTypeDefinition sourceType) {
		this(sourceId, sourceType, null, null);
	}

	/**
	 * Instantiates a new link source id from the given reference by coping the input data.
	 *
	 * @param copyFrom
	 *            the copy from
	 */
	public InstanceReferenceMock(InstanceReference copyFrom) {
		this(copyFrom.getId(), copyFrom.getReferenceType(), null, copyFrom.getType());
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param instance
	 *            the instance
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition type, Instance instance) {
		this(id, type, instance, null);
	}

	/**
	 * Instantiates a new instance reference mock.
	 *
	 * @param id
	 *            the id
	 * @param type
	 *            the type
	 * @param instance
	 *            the instance
	 * @param instanceType
	 *            the instance type
	 */
	public InstanceReferenceMock(String id, DataTypeDefinition type, Instance instance, InstanceType instanceType) {
		super(id, type, instanceType, instance);
		setReference(instance, this);
	}

	/**
	 * Instantiates a new link source id.
	 *
	 * @param sourceId
	 *            the source id
	 * @param sourceType
	 *            the source type
	 * @param type
	 *            the instance type
	 * @param instance
	 *            the instance that represents the current reference if fetched.
	 */
	public InstanceReferenceMock(String sourceId, DataTypeDefinition sourceType, InstanceType type) {
		super(sourceId, sourceType, type, null);
	}

	/**
	 * Creates a generic instance reference for the given id. The returned reference will have a mock object for data
	 * type and the method {@link #toInstance()} will return an {@link EmfInstance} with set id and reference the
	 * returned reference. This way the methods {@link Instance#toReference()} and
	 * {@link InstanceReference#toInstance()} will work end return the same values
	 *
	 * @param id
	 *            the id to set for the instance and the reference
	 * @return the instance reference
	 */
	public static InstanceReferenceMock createGeneric(String id) {
		EmfInstance instance = new EmfInstance(id);
		instance.setProperties(new HashMap<>());
		ClassInstance type = new ClassInstance();
		type.setCategory("case");
		type.setId("emf:Case");
		instance.setType(type);
		return createGeneric(instance);

	}

	/**
	 * Static initialization based on {@link #InstanceReferenceMock(Instance)}
	 *
	 * @param source
	 *            is the source instance
	 * @return the initialized mock
	 */
	public static InstanceReferenceMock createGeneric(Instance source) {
		return new InstanceReferenceMock(source);
	}

	/**
	 * Static initialization based on {@link #InstanceReferenceMock(String, DataTypeDefinition, Instance) }
	 *
	 * @param source
	 *            is the source instance
	 * @param dataType
	 *            custom data type
	 * @return the initialized mock
	 */
	public static InstanceReferenceMock createGeneric(Instance source, DataTypeDefinition dataType) {
		return new InstanceReferenceMock((String) source.getId(), dataType, source);
	}

	private static void setReference(Instance source, InstanceReferenceMock reference) {
		MockingDetails mockingDetails = Mockito.mockingDetails(source);
		if (mockingDetails.isMock() && !mockingDetails.isSpy()) {
			Mockito.when(source.toReference()).thenReturn(reference);
		} else if (source instanceof EmfInstance || source instanceof CommonInstance) {
			ReflectionUtils.setFieldValue(source, "reference", reference);
		} else if (source instanceof EmfResource) {
			((EmfResource) source).setReference(reference);
		} else if (source != null) {
			throw new EmfRuntimeException("Not supported reference type: " + source);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InstanceReferenceMock [").append(super.toString()).append("]");
		return builder.toString();
	}

}
