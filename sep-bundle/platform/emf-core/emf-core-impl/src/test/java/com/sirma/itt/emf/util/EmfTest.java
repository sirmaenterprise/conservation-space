package com.sirma.itt.emf.util;

import org.testng.annotations.BeforeClass;

import com.sirma.itt.commons.utils.reflection.ReflectionUtils;
import com.sirma.itt.emf.converter.Converter;
import com.sirma.itt.emf.converter.TypeConverter;
import com.sirma.itt.emf.converter.TypeConverterImpl;
import com.sirma.itt.emf.converter.TypeConverterUtilMock;
import com.sirma.itt.emf.converter.extensions.DefaultTypeConverter;
import com.sirma.itt.emf.converter.extensions.InstanceRefereneToClassConverterProvider;
import com.sirma.itt.emf.db.DefaultDbIdGenerator;
import com.sirma.itt.emf.db.SequenceEntityGenerator;
import com.sirma.itt.emf.definition.model.DataType;
import com.sirma.itt.emf.entity.LinkSourceId;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.instance.model.InstanceReference;
import com.sirma.itt.emf.resources.ResourceProperties;
import com.sirma.itt.emf.security.context.SecurityContextManager;
import com.sirma.itt.emf.security.model.EmfUser;

/**
 * The Class EmfTest.
 *
 * @author BBonev
 */
public abstract class EmfTest {

	/**
	 * Sets a current user to be admin before class execution.<br>
	 * Initialize the sequence generator.
	 */
	@BeforeClass
	public void setCurrentUser() {
		EmfUser user = new EmfUser("admin");
		user.getProperties().put(ResourceProperties.FIRST_NAME, "Admin");
		user.getProperties().put(ResourceProperties.LAST_NAME, "Adminov");
		user.getProperties().put(ResourceProperties.LANGUAGE, "bg");
		SecurityContextManager.authenticateFullyAs(user);

		// initialize the sequence generator
		SequenceEntityGenerator generator = new SequenceEntityGenerator();
		ReflectionUtils.setField(generator, "idGenerator", new DefaultDbIdGenerator());
		generator.onApplicationStart(null);
	}

	/**
	 * Creates the type converter initialized with some default converter.s
	 *
	 * @return the type converter
	 */
	public TypeConverter createTypeConverter() {
		TypeConverterImpl converter = new TypeConverterImpl();
		new DefaultTypeConverter().register(converter);
		new InstanceRefereneToClassConverterProvider().register(converter);

		converter.addConverter(Instance.class, InstanceReference.class,
				new Converter<Instance, InstanceReference>() {

					@Override
					public InstanceReference convert(Instance source) {
						DataType type = createDataType(source);
						return new LinkSourceId((String) source.getId(), type);
					}
				});

		TypeConverterUtilMock.setTypeConverter(converter);
		return converter;
	}

	/**
	 * Sets the reference field.
	 * 
	 * @param instance
	 *            the new reference field
	 */
	protected void setReferenceField(Instance instance) {
		ReflectionUtils.setField(instance, "reference", new LinkSourceId(instance.getId()
				.toString(), createDataType(instance), instance));
	}

	/**
	 * Creates the data type.
	 * 
	 * @param source
	 *            the source
	 * @return the data type
	 */
	protected DataType createDataType(Instance source) {
		DataType type = new DataType();
		type.setName(source.getClass().getSimpleName().toLowerCase());
		type.setJavaClassName(source.getClass().getName());
		type.setJavaClass(source.getClass());
		return type;
	}

}
