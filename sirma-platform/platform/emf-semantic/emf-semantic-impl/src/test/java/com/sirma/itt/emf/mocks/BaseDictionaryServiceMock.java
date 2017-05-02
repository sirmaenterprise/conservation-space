package com.sirma.itt.emf.mocks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.dozer.DozerBeanMapper;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.sirma.itt.seip.Uri;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.MutableDictionaryService;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.PrototypeDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.instance.InstanceType;
import com.sirma.itt.seip.instance.CommonInstance;
import com.sirma.itt.seip.model.DataType;

/**
 * Base mock implementation of {@link DictionaryService} interface
 *
 * @author BBonev
 */
public abstract class BaseDictionaryServiceMock implements DictionaryService {

	private static final long serialVersionUID = 6505248230297996019L;

	private DozerBeanMapper dozerMapper;

	// holder map containing the bean configuration
	private Map<Class<?>, BeanConfiguration> beanRegistry;

	private static Map<String, URI> instanceUris = new HashMap<>();
	private static Map<URI, String> instanceClasses = new HashMap<>();

	/**
	 * Bean configuration class containing the JAXB and model bean class. Setting are used for unmarshalling and latter
	 * conversion of definitions
	 *
	 * @author Valeri Tishev
	 */
	public static class BeanConfiguration {
		public Class<?> jaxBClass;
		public Class<?> modelImplClass;

		/**
		 * Instantiates a new bean configuration.
		 */
		private BeanConfiguration() {

		}

		/**
		 * Instantiates a new bean configuration.
		 *
		 * @param jaxBClass
		 *            the JAXB class
		 * @param modelImplClass
		 *            the bean model implementation class
		 */
		public BeanConfiguration(Class<?> jaxBClass, Class<?> modelImplClass) {
			this.jaxBClass = jaxBClass;
			this.modelImplClass = modelImplClass;
		}

		/**
		 * Gets the JAXB class.
		 *
		 * @return the JAXB class
		 */
		public Class<?> getJaxBClass() {
			return jaxBClass;
		}

		/**
		 * Gets the the bean model implementation class.
		 *
		 * @return the the bean model implementation class
		 */
		public Class<?> getModelImplClass() {
			return modelImplClass;
		}
	}

	/**
	 * Instantiates a new dictionary service implementation mock.
	 */
	public BaseDictionaryServiceMock() {
		init();
	}

	private void init() {

		initializeInstanceUries(instanceUris);
		initializeInstanceClasses(instanceClasses);

		Collection<String> mappings = provideDozerMappings();

		List<String> mappingFiles = new ArrayList<>();

		for (String mappingUri : mappings) {
			URL resource = getClass().getClassLoader().getResource(mappingUri);
			mappingFiles.add(resource.toString());
		}

		dozerMapper = new DozerBeanMapper(mappingFiles);

		// initialise the bean registry containing
		// the JAXB and model bean implementation class
		beanRegistry = new HashMap<>();
		initializeBeanRegistry(beanRegistry);
	}

	protected abstract void initializeInstanceUries(Map<String, URI> instanceUris);

	protected abstract void initializeInstanceClasses(Map<URI, String> instanceClasses);

	protected abstract void initializeBeanRegistry(Map<Class<?>, BeanConfiguration> beanRegistry);

	protected abstract Collection<String> provideDozerMappings();

	/**
	 * Unmarshall file by given type
	 *
	 * @param <S>
	 *            the generic type
	 * @param file
	 *            the file
	 * @param type
	 *            the type
	 * @return the unmarshalled object
	 */
	@SuppressWarnings("unchecked")
	private static <S> S unmarshall(File file, Class<S> type) {
		JAXBContext context;
		try (InputStream fileInput = new FileInputStream(file);
				InputStreamReader reader = new InputStreamReader(fileInput, StandardCharsets.UTF_8)) {
			context = JAXBContext.newInstance(type);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (S) unmarshaller.unmarshal(reader);
		} catch (JAXBException e) {
			throw new IllegalStateException("Failed unmarshalling file [" + file.getAbsolutePath() + "]");
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("Definition file [" + file.getAbsolutePath() + "] does not exist.", e);
		} catch (IOException e1) {
			throw new IllegalStateException("Failed unmarshalling file [" + file.getAbsolutePath() + "]");
		}
	}

	/**
	 * Load definition.
	 *
	 * @param <E>
	 *            the element type
	 * @param type
	 *            the type
	 * @param definition
	 *            the definition
	 * @return the e
	 */
	@SuppressWarnings("unchecked")
	private <E extends DefinitionModel> E loadDefinition(Class<E> type, File definition) {
		BeanConfiguration classHolder = beanRegistry.get(type);
		return (E) dozerMapper.map(unmarshall(definition, classHolder.getJaxBClass()), classHolder.getModelImplClass());
	}

	@Override
	public <E extends DefinitionModel> List<E> getAllDefinitions(Class<?> ref) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId) {
		return (E) find(defId);
	}

	protected abstract File resolveDefinitionFile(String defId);

	/**
	 * Gets the actual data type.
	 *
	 * @param propertyType
	 *            the property type
	 * @return the actual data type
	 */
	protected DataType getActualDataType(String propertyType) {
		DataType dataType = null;
		// TODO : add number, date, etc.
		if (propertyType.startsWith("an..")) {
			dataType = new DataType();
			dataType.setJavaClassName(String.class.getName());
			dataType.setName(DataTypeDefinition.TEXT);

		} else if (propertyType.equals("boolean")) {
			dataType = new DataType();
			dataType.setJavaClassName(Boolean.class.getName());
			dataType.setName(DataTypeDefinition.BOOLEAN);
		} else if (propertyType.equals("INSTANCE")) {
			dataType = new DataType();
			dataType.setJavaClassName(CommonInstance.class.getName());
			dataType.setName(DataTypeDefinition.INSTANCE);
		} else if (propertyType.equals("dateTime")) {
			dataType = new DataType();
			dataType.setJavaClassName(Date.class.getName());
			dataType.setName(DataTypeDefinition.DATETIME);
		} else if (propertyType.equals("uri")) {
			dataType = new DataType();
			dataType.setJavaClassName(Uri.class.getName());
			dataType.setName(DataTypeDefinition.URI);
		}
		return dataType;
	}

	@Override
	public <E extends DefinitionModel> E getDefinition(Class<E> ref, String defId, Long version) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public PropertyDefinition getProperty(String currentQName, Long revision, PathElement pathElement) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public PrototypeDefinition getPrototype(String currentQName, Long revision, PathElement pathElement) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public Long getPropertyId(String propertyName, Long revision, PathElement pathElement, Serializable value) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public String getPropertyById(Long propertyId) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public PrototypeDefinition getProperty(Long propertyId) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	// TODO : load data type definitions from types.xml
	@Override
	@SuppressWarnings("rawtypes")
	public DataTypeDefinition getDataTypeDefinition(Object key) {
		Object localKey = key;

		if (key instanceof Class) {
			Class keyClass = (Class) key;
			localKey = keyClass.getName();
		}

		if (!(localKey instanceof String)) {
			localKey.toString();
		}

		String className = null;
		URI uri = null;
		String name = (String) localKey;
		if (name.indexOf(":", 1) > 0) {
			// full URI
			uri = ValueFactoryImpl.getInstance().createURI(name);
			className = instanceClasses.get(uri);
		} else if (name.indexOf(".", 1) > 0) {
			// class name
			uri = instanceUris.get(name);
			className = name;
		} else {
			uri = instanceUris.get(name);
			className = instanceClasses.get(uri);
		}

		if (className != null) {
			DataType dataType = new DataType();
			dataType.setName(name);
			dataType.setJavaClassName(className);
			try {
				dataType.setJavaClass(Class.forName(className));
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			if (uri != null) {
				dataType.setUri(uri.toString());
			}
			return dataType;
		}
		return null;
	}

	@Override
	public Map<String, Serializable> filterProperties(DefinitionModel model, Map<String, Serializable> properties,
			DisplayType displayType) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public DefinitionModel getInstanceDefinition(Instance instance) {
		return resolveInstanceDefinition(instance);
	}

	protected abstract DefinitionModel resolveInstanceDefinition(Instance instance);

	@Override
	public PrototypeDefinition getDefinitionByValue(String propertyName, Serializable serializable) {
		throw new UnsupportedOperationException("Not implemented yet.");
	}

	@Override
	public MutableDictionaryService getMutableInstance() {
		return null;
	}

	@Override
	public DefinitionModel find(String defId) {
		File pathToDefinition = resolveDefinitionFile(defId);

		if (pathToDefinition == null) {
			return null;
		}

		DefinitionModel definitionModel = loadDefinition(GenericDefinition.class, pathToDefinition);

		for (PropertyDefinition property : definitionModel.getFields()) {
			property.setDataType(getActualDataType(property.getType()));
		}

		return definitionModel;
	}

	@Override
	public String getDefinitionIdentifier(DefinitionModel model) {
		return null;
	}

	@Override
	public <E extends DefinitionModel> Stream<E> getAllDefinitions() {
		return Stream.empty();
	}

	@Override
	public Stream<DefinitionModel> getAllDefinitions(InstanceType instanceType) {
		return Stream.empty();
	}

	@Override
	public String getDefaultDefinitionId(Instance target) {
		return null;
	}
}