package com.sirma.itt.seip.testutil.mocks;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;

/**
 * Mock object.
 *
 * @author svelikov
 */
public class DataTypeDefinitionMock implements DataTypeDefinition {
	private final Class<?> clazz;
	private final String uri;
	private String name;

	/**
	 * Instantiates a new mock datatype.
	 *
	 * @param clazz
	 *            the clazz
	 * @param uri
	 *            the uri
	 */
	public DataTypeDefinitionMock(Class<?> clazz, String uri) {
		super();
		this.clazz = clazz;
		this.uri = uri;
	}

	/**
	 * Instantiates a new data type definition mock.
	 *
	 * @param instance
	 *            the instance
	 */
	public DataTypeDefinitionMock(Instance instance) {
		clazz = instance.getClass();
		uri = Optional.ofNullable(instance.type()).map(InstanceType::getId).map(Object::toString).orElse(null);
	}

	/**
	 * Instantiates a new data type definition mock.
	 *
	 * @param name
	 *            the name
	 */
	public DataTypeDefinitionMock(String name) {
		this.name = name;
		clazz = Object.class;
		uri = null;
	}

	@Override
	public Long getId() {
		return Long.valueOf(1L);
	}

	@Override
	public void setId(Long id) {
		// nothing to do
	}

	@Override
	public String getName() {
		if (name == null) {
			return getJavaClass().getSimpleName().toLowerCase();
		}
		return name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name
	 *            the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getTitle() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public String getJavaClassName() {
		return getJavaClass().getName();
	}

	@Override
	public Class<?> getJavaClass() {
		return clazz;
	}

	@Override
	public String getFirstUri() {
		return uri;
	}

	@Override
	public Set<String> getUries() {
		return Collections.emptySet();
	}

}
