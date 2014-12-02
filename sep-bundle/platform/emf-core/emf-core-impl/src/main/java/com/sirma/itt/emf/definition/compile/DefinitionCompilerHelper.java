package com.sirma.itt.emf.definition.compile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.emf.adapter.FileDescriptor;
import com.sirma.itt.emf.configuration.Config;
import com.sirma.itt.emf.configuration.EmfConfigurationProperties;
import com.sirma.itt.emf.db.DbDao;
import com.sirma.itt.emf.definition.DictionaryService;
import com.sirma.itt.emf.definition.MutableDictionaryService;
import com.sirma.itt.emf.definition.load.DefinitionCompilerCallback;
import com.sirma.itt.emf.definition.load.DefinitionValidator;
import com.sirma.itt.emf.definition.model.Condition;
import com.sirma.itt.emf.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.emf.definition.model.Conditional;
import com.sirma.itt.emf.definition.model.ControlDefinition;
import com.sirma.itt.emf.definition.model.DataTypeDefinition;
import com.sirma.itt.emf.definition.model.FilterDefinitionImpl;
import com.sirma.itt.emf.definition.model.PropertyDefinition;
import com.sirma.itt.emf.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.emf.definition.model.RegionDefinition;
import com.sirma.itt.emf.definition.model.RegionDefinitionImpl;
import com.sirma.itt.emf.definition.model.RegionDefinitionModel;
import com.sirma.itt.emf.definition.model.TransitionDefinition;
import com.sirma.itt.emf.definition.model.Transitional;
import com.sirma.itt.emf.definition.model.WritablePropertyDefinition;
import com.sirma.itt.emf.definition.model.jaxb.FilterDefinition;
import com.sirma.itt.emf.definition.model.jaxb.Label;
import com.sirma.itt.emf.definition.model.jaxb.LabelValue;
import com.sirma.itt.emf.domain.DisplayType;
import com.sirma.itt.emf.domain.MessageType;
import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.domain.Triplet;
import com.sirma.itt.emf.domain.VerificationMessage;
import com.sirma.itt.emf.domain.model.BidirectionalMapping;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.domain.model.PathElement;
import com.sirma.itt.emf.domain.model.Sortable;
import com.sirma.itt.emf.domain.model.TopLevelDefinition;
import com.sirma.itt.emf.dozer.DozerMapper;
import com.sirma.itt.emf.exceptions.DefinitionValidationException;
import com.sirma.itt.emf.filter.FilterService;
import com.sirma.itt.emf.hash.HashCalculator;
import com.sirma.itt.emf.io.ContentService;
import com.sirma.itt.emf.label.LabelDefinition;
import com.sirma.itt.emf.label.LabelService;
import com.sirma.itt.emf.label.model.LabelImpl;
import com.sirma.itt.emf.security.Secure;
import com.sirma.itt.emf.time.TimeTracker;
import com.sirma.itt.emf.util.EqualsHelper;
import com.sirma.itt.emf.util.PathHelper;
import com.sirma.itt.emf.util.SortableComparator;
import com.sirma.itt.emf.util.ValidationLoggingUtil;
import com.sirma.itt.emf.xml.CmfXmlValidator;
import com.sirma.itt.emf.xml.XmlError;
import com.sirma.itt.emf.xml.XmlSchemaProvider;
import com.sirma.itt.emf.xml.XmlValidatorError;

/**
 * Helper class for loading, validating and converting objects. Contains methods mostly used by
 * concrete implementations of the {@link DefinitionCompilerCallback}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionCompilerHelper {

	/** The Constant SORTABLE_COMPARATOR. */
	private static final SortableComparator SORTABLE_COMPARATOR = new SortableComparator();
	/** The Constant FIELD_PATTERN. */
	private static final Pattern FIELD_PATTERN = Pattern.compile("(?<!\\d+)\\[(\\w+?)\\]", Pattern.CANON_EQ);
	/** The Constant ASCII_CHARACTER_PATTERN. */
	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern
			.compile("^[\\x20-\\x7E\\r\\n\\t]+$");
	/** The Constant SP_CHARACTER_PATTERN. */
	private static final Pattern SP_CHARACTER_PATTERN = Pattern.compile("[\\s][\\s]+");

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DefinitionCompilerHelper.class);
	/** The debug. */
	private static final boolean DEBUG = LOGGER.isDebugEnabled();
	/** The trace. */
	private static final boolean TRACE = LOGGER.isTraceEnabled();

	/** The pool. */
	protected ForkJoinPool pool;
	/** The label provider. */
	@Inject
	private LabelService labelService;
	/** The filter service. */
	@Inject
	private FilterService filterService;
	/** The dozer mapper. */
	@Inject
	private DozerMapper dozerMapper;
	/** The content service. */
	@Inject
	private ContentService contentService;

	/** The initialize default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.INITIALIZE_DEFAULT_CONTAINER_ONLY, defaultValue = "false")
	private Boolean initializeDefaultContainer;

	/** The default container. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFAULT_CONTAINER)
	private String defaultContainer;

	/** The mutable dictionary service. */
	@Inject
	private MutableDictionaryService mutableDictionaryService;
	@Inject
	private DictionaryService dictionaryService;
	/** The validators. */
	@Inject
	@Any
	private Instance<DefinitionValidator> validators;

	/** The db dao. */
	@Inject
	private DbDao dbDao;

	/** The definition download pool size. */
	@Inject
	@Config(name = EmfConfigurationProperties.DEFINITION_DOWNLOAD_POOL_SIZE, defaultValue = "-1")
	private Integer definitionDownloadPoolSize;

	/** The hash calculator. */
	@Inject
	private HashCalculator hashCalculator;

	/**
	 * Initializes the.
	 */
	@PostConstruct
	public void init() {
		if (definitionDownloadPoolSize.intValue() < 0) {
			pool = new ForkJoinPool();
		} else {
			if (definitionDownloadPoolSize.intValue() == 0) {
				definitionDownloadPoolSize = 1;
			} else if (definitionDownloadPoolSize.intValue() > Runtime.getRuntime()
					.availableProcessors()) {
				int times = definitionDownloadPoolSize.intValue()
						/ Runtime.getRuntime().availableProcessors();
				// if the number of threads is more then 5 times the cores then we limit them
				if (times > 5) {
					definitionDownloadPoolSize = 5 * Runtime.getRuntime().availableProcessors();
				}
			}
			pool = new ForkJoinPool(definitionDownloadPoolSize.intValue());
		}
	}

	/**
	 * Load files content and convert them to top level definition objects and fills the default
	 * properties. The loading is done in parallel.
	 *
	 * @param <E>
	 *            the concrete element type
	 * @param definitions
	 *            the list of files to process
	 * @param targetClass
	 *            the target class is the final type
	 * @param jaxbClass
	 *            the intermediate type. The actual JaxB class
	 * @param xmlType
	 *            the xml type
	 * @param updateDefProps
	 *            the update definition properties
	 * @param callback
	 *            the callback
	 * @return the result mapping: definitionIdentifier->definition
	 */
	public <E extends TopLevelDefinition> List<E> loadFiles(List<FileDescriptor> definitions,
			Class<E> targetClass, Class<?> jaxbClass, XmlSchemaProvider xmlType,
			boolean updateDefProps, DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if ((definitions == null) || definitions.isEmpty()) {
			return Collections.emptyList();
		}

		TimeTracker timeTracker = new TimeTracker().begin();

		List<ForkJoinTask<Triplet<E, List<Label>, List<FilterDefinition>>>> tasks = new LinkedList<ForkJoinTask<Triplet<E, List<Label>, List<FilterDefinition>>>>();

		List<VerificationMessage> messages = ValidationLoggingUtil.getMessages();
		// schedule tasks execution
		for (FileDescriptor dmsFileDescriptor : definitions) {
			if (acceptDefinition(dmsFileDescriptor)) {
				DefinitionFetchTask<E> task = new DefinitionFetchTask<E>(dmsFileDescriptor,
						xmlType, targetClass, jaxbClass, callback, updateDefProps, messages);
				tasks.add(task);
				pool.execute(task);
			}
		}

		List<E> result = new LinkedList<E>();
		List<Label> labels = new LinkedList<Label>();
		List<FilterDefinition> filterDefinitions = new LinkedList<FilterDefinition>();

		// here we wait for the tasks to finish
		while (!tasks.isEmpty()) {
			// wait some time for the tasks to finish
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				LOGGER.warn("Definition loading was interrupted");
			}

			// check the remaining tasks
			for (Iterator<ForkJoinTask<Triplet<E, List<Label>, List<FilterDefinition>>>> it = tasks
					.iterator(); it.hasNext();) {
				ForkJoinTask<Triplet<E, List<Label>, List<FilterDefinition>>> forkJoinTask = it
						.next();

				// if tasks completed then we removed it from the wait list
				if (forkJoinTask.isDone()) {
					it.remove();
				}
				if (forkJoinTask.isCompletedNormally()) {
					Triplet<E, List<Label>, List<FilterDefinition>> pair = forkJoinTask
							.getRawResult();
					result.add(pair.getFirst());

					labels.addAll(pair.getSecond());
					filterDefinitions.addAll(pair.getThird());
				} else {
					Throwable exception = forkJoinTask.getException();
					if (exception instanceof CancellationException) {
						LOGGER.warn("Asynchronous task was cancelled!");
					} else if (exception instanceof DefinitionValidationException) {
						LOGGER.error(exception.getMessage());
						ValidationLoggingUtil.addErrorMessage(exception.getMessage());
					} else if (exception != null) {
						String message = "Failed to process " + callback.getCallbackName()
								+ " definition due to " + exception.getMessage();
						LOGGER.error(message);
						ValidationLoggingUtil.addErrorMessage(message);
						if (DEBUG) {
							LOGGER.debug("", exception);
						}
					}
				}
			}
		}

		// if we have any labels save them
		if (!labels.isEmpty()) {
			pool.execute(new LabelsSaveTask(labels, callback));
		}

		if (!filterDefinitions.isEmpty()) {
			pool.execute(new FiltersSaveTask(filterDefinitions, callback));
		}

		if (DEBUG) {
			LOGGER.debug("Loaded " + result.size() + " " + callback.getCallbackName()
					+ " definition files for " + timeTracker.stopInSeconds() + " s");
		}
		return result;
	}

	/**
	 * Check if the definition represented by the given file descriptor is acceptable for loading
	 * based on the source container.
	 *
	 * @param dmsFileDescriptor
	 *            the file descriptor
	 * @return true, if accepted
	 */
	private boolean acceptDefinition(FileDescriptor dmsFileDescriptor) {
		// by default we accept all definitions
		if (Boolean.FALSE.equals(initializeDefaultContainer)) {
			return true;
		}
		String containerId = dmsFileDescriptor.getContainerId();
		return (containerId == null)
				|| EqualsHelper.nullSafeEquals(containerId, defaultContainer, true);
	}

	/**
	 * Load the given file from DMS, perform a XSD verification and converts it to internal domain
	 * model.
	 *
	 * @param <E>
	 *            the target type
	 * @param location
	 *            the location to the file
	 * @param xmlType
	 *            the xml type to validate against
	 * @param jaxbClass
	 *            the jaxb class
	 * @param callback
	 *            the callback implementation that can handle the given file
	 * @param targetClass
	 *            the target class
	 * @param updateDefProps
	 *            the update def props
	 * @param errors
	 *            the errors
	 * @return the pair that contains the parsed top level definition from the file and the list of
	 *         label data that need to be processed
	 */
	@Secure(runAsSystem = true)
	@SuppressWarnings("unchecked")
	public <E extends TopLevelDefinition> Triplet<E, List<Label>, List<FilterDefinition>> loadFile(
			FileDescriptor location, XmlSchemaProvider xmlType, Class<?> jaxbClass,
			DefinitionCompilerCallback<TopLevelDefinition> callback, Class<E> targetClass,
			boolean updateDefProps, List<VerificationMessage> errors) {
		File file = null;
		try {

			file = contentService.getContent(location);
			if (file == null) {
				throw new DefinitionValidationException("Failed to download location: "
						+ location);
			}
			if (!validateDefinition(file, xmlType, errors)) {
				throw new DefinitionValidationException("Failed to validate file with ID: "
						+ location.getId());
			}
			Object intermidiate = load(file, jaxbClass);
			if (intermidiate == null) {
				throw new DefinitionValidationException("Failed to convert the file with ID: "
						+ location.getId() + " to Java class");
			}

			List<Label> labels = (List<Label>) callback.getLabelDefinitions(intermidiate);
			List<FilterDefinition> filterDefinitions = (List<FilterDefinition>) callback
					.getFilterDefinitions(intermidiate);

			E definition = convert(intermidiate, targetClass);
			if (definition == null) {
				throw new DefinitionValidationException("Failed to parse definition: "
						+ location);
			}
			definition.setDmsId(location.getId());
			definition.setContainer(location.getContainerId());

			if (updateDefProps) {
				String definitionId = callback.extractDefinitionId(definition);
				if (definitionId == null) {
					throw new DefinitionValidationException(
							"Invalid definition. NO definition ID! ->" + definition);
				}
				definition.setIdentifier(definitionId);
			}
			return new Triplet<E, List<Label>, List<FilterDefinition>>(definition, labels,
					filterDefinitions);
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	/**
	 * Convert.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param file
	 *            the file
	 * @param src
	 *            the src
	 * @param destClass
	 *            the dest class
	 * @return the d
	 */
	public <S, D> D convert(File file, Class<S> src, Class<D> destClass) {
		S sourceData = load(file, src);
		if (sourceData == null) {
			return null;
		}

		D result = dozerMapper.getMapper().map(sourceData, destClass);
		return result;
	}

	/**
	 * Convert.
	 *
	 * @param <S>
	 *            the generic type
	 * @param <D>
	 *            the generic type
	 * @param src
	 *            the src
	 * @param destClass
	 *            the dest class
	 * @return the d
	 */
	public <S, D> D convert(S src, Class<D> destClass) {
		if (src == null) {
			return null;
		}

		D result = dozerMapper.getMapper().map(src, destClass);
		return result;
	}

	/**
	 * Convert.
	 *
	 * @param <S>
	 *            the generic type
	 * @param file
	 *            the file
	 * @param src
	 *            the src
	 * @return the d
	 */
	@SuppressWarnings("unchecked")
	public <S> S load(File file, Class<S> src) {
		JAXBContext context;
		try (InputStreamReader streamReader = new InputStreamReader(new FileInputStream(file),
				"UTF-8")) {
			context = JAXBContext.newInstance(src);
			Unmarshaller um = context.createUnmarshaller();
			S result = (S) um.unmarshal(streamReader);
			return result;
		} catch (JAXBException e) {
			throw new DefinitionValidationException("Error while converting file "
					+ file.getAbsolutePath() + " to " + src, e);
		} catch (FileNotFoundException e) {
			LOGGER.warn("File not found " + file.getAbsolutePath(), e);
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Problem with encoding", e);
		} catch (IOException e) {
			LOGGER.warn("Problem with encoding", e);
		}
		return null;
	}

	/**
	 * Validate definition.
	 *
	 * @param file
	 *            the file
	 * @param xmlType
	 *            the xml type
	 * @param messages
	 *            the messages
	 * @return true, if successful
	 */
	public boolean validateDefinition(File file, XmlSchemaProvider xmlType,
			List<VerificationMessage> messages) {
		FileInputStream xml = null;
		try {
			xml = new FileInputStream(file);
			List<XmlError> errors = CmfXmlValidator.resolveErrors(xml,
					xmlType);
			if (errors.isEmpty()) {
				return true;
			}
			StringBuilder msg = new StringBuilder();
			StringBuilder err = new StringBuilder();
			msg.append("\n=======================================================================\n");
			err.append("\tFound errors while validating XML type ").append(xmlType);
			for (XmlError xmlError : errors) {
				err.append("\n").append(xmlError.getFormattedMessage());
			}
			msg.append(err);
			msg.append("\n=======================================================================");
			ValidationLoggingUtil.addMessage(MessageType.ERROR, err.toString(), messages);
			LOGGER.error(msg.toString());
		} catch (XmlValidatorError e) {
			String message = "Error validating XML type " + xmlType;
			LOGGER.error(message, e);
			ValidationLoggingUtil.addMessage(MessageType.ERROR, message, messages);
		} catch (FileNotFoundException e) {
			String message = "Failed to read the sourse XML file";
			LOGGER.error(message, e);
			ValidationLoggingUtil.addMessage(MessageType.ERROR, message, messages);
		} finally {
			if (xml != null) {
				try {
					xml.close();
				} catch (IOException e) {
					// not interested
				}
			}
		}
		return false;
	}

	/**
	 * Save the list of labels.
	 *
	 * @param list
	 *            the list
	 * @param callback
	 *            the callback
	 */
	void saveLabels(List<Label> list, DefinitionCompilerCallback<?> callback) {
		List<LabelDefinition> definitions = new ArrayList<LabelDefinition>(list.size());

		for (Label label : list) {
			LabelImpl impl = new LabelImpl();
			impl.setIdentifier(label.getId());
			List<LabelValue> values = label.getValue();
			Map<String, String> map = new LinkedHashMap<String, String>(
					(int) (values.size() * 1.2), 0.95f);
			for (LabelValue value : values) {
				map.put(value.getLang(), value.getValue());
			}
			impl.setLabels(map);

			boolean valid = true;
			for (DefinitionValidator validator : validators) {
				valid &= validator.validate(impl);
			}
			if (valid) {
				definitions.add(impl);
			} else {
				LOGGER.warn("Skipped invalid label with id: " + impl.getIdentifier());
			}
		}

		if (!definitions.isEmpty()) {
			if (!labelService.saveLabels(definitions)) {
				LOGGER.warn("Failed to persist label data from " + callback.getCallbackName());
			}
		}
	}

	/**
	 * Save filters.
	 *
	 * @param list
	 *            the list
	 * @param callback
	 *            the callback
	 */
	void saveFilters(List<FilterDefinition> list, DefinitionCompilerCallback<?> callback) {
		List<com.sirma.itt.emf.filter.Filter> definitions = new ArrayList<com.sirma.itt.emf.filter.Filter>(
				list.size());

		for (FilterDefinition filter : list) {
			FilterDefinitionImpl convert = convert(filter, FilterDefinitionImpl.class);
			definitions.add(convert);
		}

		if (!definitions.isEmpty()) {
			if (!filterService.saveFilters(definitions)) {
				LOGGER.warn("Failed to persist filter data from " + callback.getCallbackName());
			}
		}
	}

	/**
	 * Normalize fields.
	 *
	 * @param fields
	 *            the fields
	 * @param pathElement
	 *            the path element
	 * @param clearFieldId
	 *            the clear field id
	 * @param container
	 *            the container
	 */
	public void normalizeFields(List<PropertyDefinition> fields, PathElement pathElement,
			boolean clearFieldId, String container) {
		if (fields == null) {
			return;
		}
		for (PropertyDefinition definition : fields) {
			if (clearFieldId) {
				((WritablePropertyDefinition) definition).setId(null);
			}
			normalizeFields((WritablePropertyDefinition) definition, pathElement, container);

			// normalize control fields after the current field
			if (definition.getControlDefinition() != null) {
				normalizeFields(definition.getControlDefinition().getFields(),
						definition.getControlDefinition(), clearFieldId, container);
			}
		}
	}

	/**
	 * Normalize fields.
	 *
	 * @param fieldDefinitionImpl
	 *            the field definition
	 * @param pathElement
	 *            is the containing path element
	 * @param container
	 *            the container
	 */
	public void normalizeFields(WritablePropertyDefinition fieldDefinitionImpl,
			PathElement pathElement, String container) {
		fieldDefinitionImpl.setContainer(container);
		fieldDefinitionImpl.setParentPath(PathHelper.getPath(pathElement));
		String type = fieldDefinitionImpl.getType();
		if ((fieldDefinitionImpl.getDataType() == null) && StringUtils.isNotNullOrEmpty(type)) {
			TypeParser parser = TypeParser.parse(type);
			// updates the type name
			fieldDefinitionImpl.setType(parser.type);
			String definitionName = parser.getDataTypeDefinitionName();
			DataTypeDefinition dataTypeDefinition = dictionaryService
					.getDataTypeDefinition(definitionName);
			if (dataTypeDefinition == null) {
				String message = "No definition found for " + definitionName;
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
				// this should not happen if the type definitions are successful
				return;
			}
			fieldDefinitionImpl.setDataType(dataTypeDefinition);
			if (parser.alpha || parser.numeric) {
				fieldDefinitionImpl.setMaxLength(parser.maxLength);
			}
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param documentDefinition
	 *            the document definition
	 * @param revision
	 *            the revision
	 */
	public void setPropertyRevision(DefinitionModel documentDefinition, Long revision) {
		if (documentDefinition.getFields() != null) {
			for (PropertyDefinition definition : documentDefinition.getFields()) {
				((WritablePropertyDefinition) definition).setRevision(revision);
				if (definition.getControlDefinition() != null) {
					setPropertyRevision(definition.getControlDefinition(), revision);
				}
			}
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param transitional
	 *            the transitional
	 * @param revision
	 *            the revision
	 */
	public void setTransactionalPropertyRevision(Transitional transitional, Long revision) {
		for (TransitionDefinition transitionDefinition : transitional.getTransitions()) {
			setPropertyRevision(transitionDefinition, revision);
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param documentDefinition
	 *            the document definition
	 * @param revision
	 *            the revision
	 */
	public void setPropertyRevision(RegionDefinitionModel documentDefinition, Long revision) {
		if (documentDefinition.getFields() != null) {
			for (PropertyDefinition definition : documentDefinition.getFields()) {
				setPropertyRevision(definition, revision);
			}
		}
		for (RegionDefinition definition : documentDefinition.getRegions()) {
			setPropertyRevision(definition, revision);
			if (definition.getControlDefinition() != null) {
				setPropertyRevision(definition.getControlDefinition(), revision);
			}
		}
	}

	/**
	 * Sets the property revision.
	 *
	 * @param definition
	 *            the definition
	 * @param revision
	 *            the revision
	 */
	public void setPropertyRevision(PropertyDefinition definition, Long revision) {
		WritablePropertyDefinition impl = (WritablePropertyDefinition) definition;
		impl.setRevision(revision);
		if (impl.getControlDefinition() != null) {
			setPropertyRevision(impl.getControlDefinition(), revision);
		}
	}

	/**
	 * Execute validators on the given model.
	 *
	 * @param model
	 *            the model
	 * @return true, if successful
	 */
	public boolean executeValidators(RegionDefinitionModel model) {
		boolean valid = true;
		for (DefinitionValidator validator : validators) {
			boolean isValid = validator.validate(model);
			if (!isValid) {
				String message = "Failed validator " + validator.getClass().getSimpleName()
						+ " on region model " + model.getIdentifier();
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
			}
			valid &= isValid;
		}
		return valid;
	}

	/**
	 * Execute validators on the given model.
	 *
	 * @param model
	 *            the model
	 * @return true, if successful
	 */
	public boolean executeValidators(DefinitionModel model) {
		boolean valid = true;
		for (DefinitionValidator validator : validators) {
			boolean isValid = validator.validate(model);
			if (!isValid) {
				String message = "Failed validator " + validator.getClass().getSimpleName()
						+ " on definition model " + model.getIdentifier();
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
			}
			valid &= isValid;
		}
		return valid;
	}

	/**
	 * Removes all regions that are mark as DisplayType = SYSTEM.
	 *
	 * @param model
	 *            the region model definition
	 */
	public void synchRegionProperties(RegionDefinitionModel model) {
		for (Iterator<RegionDefinition> regionIt = model.getRegions().iterator(); regionIt
				.hasNext();) {
			RegionDefinition region = regionIt.next();
			if (region.getDisplayType() == DisplayType.SYSTEM) {
				regionIt.remove();
				LOGGER.warn("Removing disabled region [{}] from [{}]", region.getIdentifier(),
						PathHelper.getPath((PathElement) model));
			}
		}

		Map<String, PropertyInfo> fieldsInfo = collectFieldsInfo(model);
		for (PropertyInfo info : fieldsInfo.values()) {
			if (info.getVisible().isEmpty()) {
				if (info.getSystem().size() == 1) {
					// we have only one field so we a good to go and no need to continue
					continue;
				}
				// no visible fields found - so all are system we could check if all of them are
				// identical and leave one
				int hash = 0;
				boolean allEquals = true;
				for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
					Integer currentHash = hashCalculator.computeHash(pair.getFirst());
					if (hash == 0) {
						hash = currentHash.intValue();
					} else if (hash != currentHash.intValue()) {
						// found non equal field and no need to continue
						// the definition will be marked as invalid later in the validation
						allEquals = false;
						break;
					}
				}
				if (!allEquals) {
					printErrorMessagesForSystemFields(info);
					continue;
				}
				List<Pair<PropertyDefinition, DefinitionModel>> list = info.getSystem();
				removeFields(list.subList(1, list.size()));
			} else if (info.getVisible().size() == 1) {
				// remove all system fields we have only one visible
				removeFields(info.getSystem());
			} else {
				for (Pair<PropertyDefinition, DefinitionModel> pair : info.getVisible()) {
					String message = null;
					if (pair.getSecond() instanceof RegionDefinition) {
						message = "Found duplicate VISIBLE field ["
								+ pair.getFirst().getIdentifier() + "] in ["
								+ PathHelper.getPath((PathElement) pair.getSecond()) + "/"
								+ pair.getSecond().getIdentifier() + "]";
					} else {
						message = "Found duplicate VISIBLE field ["
								+ pair.getFirst().getIdentifier() + "] from ["
								+ PathHelper.getPath((PathElement) pair.getSecond()) + "]";
					}
					ValidationLoggingUtil.addErrorMessage(message);
					LOGGER.error(message);
				}
				printErrorMessagesForSystemFields(info);
			}
			// more then one visible we cannot handle them here - the definition is invalid
		}
	}

	/**
	 * Prints the error messages for system fields.
	 * 
	 * @param info
	 *            the info
	 */
	private void printErrorMessagesForSystemFields(PropertyInfo info) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
			String message = null;
			if (pair.getSecond() instanceof RegionDefinition) {
				message = "Found duplicate field [" + pair.getFirst().getIdentifier() + "] in ["
						+ PathHelper.getPath((PathElement) pair.getSecond())
						+ "/" + pair.getSecond().getIdentifier()
						+ "] that cannot be auto removed because both are system fields!";
			} else {
				message = "Found duplicate field [" + pair.getFirst().getIdentifier() + "] from ["
						+ PathHelper.getPath((PathElement) pair.getSecond())
						+ "] that cannot be auto removed because both are system fields!";
			}
			ValidationLoggingUtil.addWarningMessage(message);
			LOGGER.warn(message);
		}
	}

	/**
	 * Removes the fields.
	 * 
	 * @param subList
	 *            the sub list
	 */
	private void removeFields(Collection<Pair<PropertyDefinition, DefinitionModel>> subList) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : subList) {
			boolean removed = pair.getSecond().getFields().remove(pair.getFirst());
			String path;
			if (pair.getSecond() instanceof RegionDefinition) {
				path = pair.getFirst().getIdentifier() + "] from ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "/"
						+ pair.getSecond().getIdentifier() + "]";
			} else {
				path = pair.getFirst().getIdentifier() + "] from ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "]";
			}
			if (removed) {
				LOGGER.warn("Removed duplicate field [" + path);
			} else {
				LOGGER.error("Failed to remove field [" + path);
			}
		}
	}


		/**
	 * Collect fields info.
	 *
	 * @param model
	 *            the model
	 * @param mapping
	 *            the mapping
	 * @return the map
	 */
	private Map<String, PropertyInfo> collectFieldsInfo(DefinitionModel model,
			Map<String, PropertyInfo> mapping) {
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			PropertyInfo info = mapping.get(propertyDefinition.getIdentifier());
			if (info == null) {
				info = new PropertyInfo();
				mapping.put(propertyDefinition.getIdentifier(), info);
			}
			Pair<PropertyDefinition, DefinitionModel> pair = new Pair<PropertyDefinition, DefinitionModel>(
					propertyDefinition, model);
			if (propertyDefinition.getDisplayType() == DisplayType.SYSTEM) {
				info.getSystem().add(pair);
			} else {
				info.getVisible().add(pair);
			}
		}
		return mapping;
	}

	/**
	 * Collect fields info.
	 *
	 * @param model
	 *            the model
	 * @return the map
	 */
	private Map<String, PropertyInfo> collectFieldsInfo(RegionDefinitionModel model) {
		Map<String, PropertyInfo> mapping = new HashMap<>();
		collectFieldsInfo(model, mapping);
		for (RegionDefinition regionDefinition : model.getRegions()) {
			collectFieldsInfo(regionDefinition, mapping);
		}
		return mapping;
	}

	/**
	 * Sets the default properties to the region.
	 *
	 * @param regionModel
	 *            the new default properties
	 * @param modelPrefix
	 *            the model prefix
	 * @param setToAll
	 *            sets the default values to all fields
	 */
	public void setDefaultProperties(RegionDefinitionModel regionModel, String modelPrefix,
			boolean setToAll) {
		if (setToAll) {
			setDefaultProperties(regionModel, modelPrefix);
		}
		for (RegionDefinition definition : regionModel.getRegions()) {
			RegionDefinitionImpl impl = (RegionDefinitionImpl) definition;
			impl.setDefaultProperties();
			if (setToAll) {
				setDefaultProperties(definition, modelPrefix);
				if (definition.getControlDefinition() != null) {
					setDefaultProperties(definition.getControlDefinition(), modelPrefix);
				}
			}
		}
	}

	/**
	 * Sets the default properties.
	 *
	 * @param definition
	 *            the new default properties
	 * @param modelPrefix
	 *            the model prefix
	 */
	public void setDefaultProperties(DefinitionModel definition, String modelPrefix) {
		for (PropertyDefinition propertyDefinition : definition.getFields()) {
			setDefaultProperties(propertyDefinition, modelPrefix);
		}
	}

	/**
	 * Sets the default properties.
	 *
	 * @param definition
	 *            the new default properties
	 * @param modelPrefix
	 *            the model prefix
	 */
	public void setDefaultProperties(PropertyDefinition definition, String modelPrefix) {
		WritablePropertyDefinition impl = (WritablePropertyDefinition) definition;
		if ((modelPrefix != null) && StringUtils.isNullOrEmpty(impl.getDmsType())) {
			impl.setDmsType(modelPrefix + impl.getName());
		}
		impl.setDefaultProperties();
		if (impl.getControlDefinition() != null) {
			setDefaultProperties(impl.getControlDefinition(), modelPrefix);
		}
	}

	/**
	 * Validate expression based on the given region definition model. If all fields defined in the
	 * expression are editable in the given definition model then the expression is valid.
	 *
	 * @param model
	 *            the model
	 * @param expression
	 *            the expression
	 * @return true, if valid
	 */
	public boolean validateExpression(DefinitionModel model, String expression) {
		if (expression == null) {
			return true;
		}
		// if the expression is not present then is not valid
		// if the target model does not have any fields or regions then we have
		// nothing to do for the expression
		// we also validate the expression for non ASCII characters
		boolean hasRegions = (model instanceof RegionDefinitionModel) && ((RegionDefinitionModel)model).getRegions().isEmpty();
		if (StringUtils.isNullOrEmpty(expression) || (model.getFields().isEmpty() && !hasRegions)) {
			LOGGER.warn("No fields in the target model to support the expression: " + expression);
			return false;
		}
		if (!ASCII_CHARACTER_PATTERN.matcher(expression).matches()) {
			LOGGER.warn("Found cyrillic characters in the expression: " + expression);
			return false;
		}
		// we first collect all fields from the expression
		Set<String> fields = new LinkedHashSet<String>();
		Matcher matcher = FIELD_PATTERN.matcher(expression);
		while (matcher.find()) {
			fields.add(matcher.group(1));
		}
		// if no fields are found then the expression is not valid
		if (fields.isEmpty()) {
			LOGGER.warn("No fields found into the expression: " + expression);
			// TODO: Fix FIELD_PATTERN for accepting new condition
			// +n[i-documentType], +n[o-documentType] and negative values
			//return false;
		}
		Set<String> modelFields = new LinkedHashSet<String>();
		Set<DisplayType> allowedTypes = new LinkedHashSet<DisplayType>();
		allowedTypes.add(DisplayType.EDITABLE);
		allowedTypes.add(DisplayType.READ_ONLY);
		allowedTypes.add(DisplayType.HIDDEN);
		allowedTypes.add(DisplayType.SYSTEM);
		collectFieldNames(model, allowedTypes, modelFields);
		// if the expression contains all fields then it's OK
		boolean containsAll = modelFields.containsAll(fields);
		if (!containsAll) {
			// we will check which fields are missing
			fields.removeAll(modelFields);
			LOGGER.warn("The fields " + fields + " in the expression " + expression
					+ " are not found into the target model!");
		}
		return containsAll;
	}

	/**
	 * Collect field names from the given region definition model and that match the given display
	 * type.
	 *
	 * @param model
	 *            the model
	 * @param allowedTypes
	 *            the list display types to collect
	 * @param fields
	 *            the fields
	 */
	private void collectFieldNames(RegionDefinitionModel model, Set<DisplayType> allowedTypes,
			Set<String> fields) {
		for (RegionDefinition regionDefinition : model.getRegions()) {
			collectFieldNames(regionDefinition, allowedTypes, fields);
		}
	}

	/**
	 * Collect field names from the given definition model and that match the given display type.
	 *
	 * @param model
	 *            the model
	 * @param allowedTypes
	 *            the list display types to collect
	 * @param fields
	 *            the fields
	 */
	private void collectFieldNames(DefinitionModel model, Set<DisplayType> allowedTypes,
			Set<String> fields) {
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			if (allowedTypes.contains(propertyDefinition.getDisplayType())) {
				fields.add(propertyDefinition.getName());
			}
			if (propertyDefinition.getControlDefinition() != null) {
				collectFieldNames(propertyDefinition.getControlDefinition(), allowedTypes, fields);
			}
		}
		if (model instanceof RegionDefinitionModel) {
			collectFieldNames((RegionDefinitionModel) model, allowedTypes, fields);
		}
	}

	/**
	 * Validate expressions. against the given target model. If any of conditions is invalid it will
	 * be removed from the list of conditions
	 *
	 * @param targetModel
	 *            the target model
	 * @param conditions
	 *            the conditions
	 */
	public void validateExpressions(DefinitionModel targetModel, Conditional conditions) {
		if ((conditions.getConditions() == null) || conditions.getConditions().isEmpty()) {
			return;
		}
		for (Iterator<Condition> it = conditions.getConditions().iterator(); it.hasNext();) {
			Condition condition = it.next();
			if ((condition.getExpression() == null)
					|| !validateExpression(targetModel, condition.getExpression())) {
				it.remove();

				LOGGER.warn(" !!! Expression in condition: " + condition
						+ " is not valid and will be removed !!!");
			} else {
				// the expression is valid so we will check the white space
				Matcher matcher = SP_CHARACTER_PATTERN.matcher(condition.getExpression());
				String updatedExpression = matcher.replaceAll(" ");
				if (TRACE && !condition.getExpression().equals(updatedExpression)) {
					LOGGER.trace("Updated expression to: " + updatedExpression);
				}
				if (condition instanceof ConditionDefinitionImpl) {
					((ConditionDefinitionImpl) condition).setExpression(updatedExpression);
				}
			}
		}
	}

	/**
	 * Validate model conditions.
	 *
	 * @param targetModel
	 *            the target model
	 * @param model
	 *            the model
	 */
	public void validateModelConditions(RegionDefinitionModel targetModel,
			RegionDefinitionModel model) {
		if ((targetModel == null) || (model == null)) {
			return;
		}
		validateModelConditions(targetModel, (DefinitionModel) model);
		for (RegionDefinition regionDefinition : model.getRegions()) {
			validateExpressions(targetModel, regionDefinition);
			validateModelConditions(targetModel, regionDefinition);
		}
	}

	/**
	 * Validate model conditions.
	 *
	 * @param targetModel
	 *            the target model
	 * @param model
	 *            the model
	 */
	public void validateModelConditions(RegionDefinitionModel targetModel, DefinitionModel model) {
		if ((targetModel == null) || (model == null)) {
			return;
		}
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			validateExpressions(targetModel, propertyDefinition);
			validateModelConditions(targetModel, propertyDefinition.getControlDefinition());
		}
	}

	/**
	 * Save properties of the given region definition model. First saves the root properties then
	 * for each region
	 *
	 * @param model
	 *            the model to iterate
	 * @param refModel
	 *            the reference model
	 */
	public void saveProperties(RegionDefinitionModel model, RegionDefinitionModel refModel) {
		dbDao.invokeInNewTx(new SaveDefinitionProperties<DefinitionModel>(model, refModel, true));
	}

	/**
	 * Save properties of the given region definition model. First saves the root properties then
	 * for each region
	 *
	 * @param model
	 *            the model to iterate
	 * @param refModel
	 *            the reference model
	 */
	private void savePropertiesInternal(RegionDefinitionModel model, RegionDefinitionModel refModel) {
		savePropertiesInternal((DefinitionModel) model, (DefinitionModel) refModel);
		for (RegionDefinition regionDefinition : model.getRegions()) {
			savePropertiesInternal(regionDefinition,
					refModel != null ? PathHelper.find(refModel.getRegions(),
							regionDefinition.getIdentifier()) : null);
		}
	}

	/**
	 * Save properties of the given model using the second model as reference.
	 *
	 * @param model
	 *            the model to save
	 * @param refModel
	 *            the reference model to use if any
	 */
	public void saveProperties(DefinitionModel model, DefinitionModel refModel) {
		dbDao.invokeInNewTx(new SaveDefinitionProperties<DefinitionModel>(model, refModel, false));
	}

	/**
	 * Save properties of the given model using the second model as reference.
	 *
	 * @param model
	 *            the model to save
	 * @param refModel
	 *            the reference model to use if any
	 */
	private void savePropertiesInternal(DefinitionModel model, DefinitionModel refModel) {
		// he we collect all fields that we processes so later we can set them
		List<PropertyDefinition> updatedFields = new ArrayList<PropertyDefinition>(model
				.getFields().size());

		for (PropertyDefinition definition : model.getFields()) {
			PropertyDefinition oldProperty = null;
			if (refModel != null) {
				oldProperty = PathHelper.find(refModel.getFields(),
						definition.getIdentifier());
			}
			// first we check if we have any control to process before we continue to saving the
			// actual definition
			if (definition.getControlDefinition() != null) {
				ControlDefinition refControl = null;
				if (oldProperty != null) {
					refControl = oldProperty.getControlDefinition();
				}
				saveProperties(definition.getControlDefinition(), refControl);
				if (definition.getControlDefinition() instanceof BidirectionalMapping) {
					((BidirectionalMapping) definition.getControlDefinition()).initBidirection();
				}
			}
			((PropertyDefinitionProxy) definition).setBaseDefinition(null);
			// save or update property if needed
			PropertyDefinition changed = mutableDictionaryService.savePropertyIfChanged(definition,
					oldProperty);
			updatedFields.add(changed);
		}
		// we update all references from the list
		model.getFields().clear();
		model.getFields().addAll(updatedFields);
	}

	/**
	 * Sorts the given list of sortables.
	 *
	 * @param sortables
	 *            the sortables to sort.
	 */
	public void sort(List<? extends Sortable> sortables) {
		Collections.sort(sortables, SORTABLE_COMPARATOR);
	}

	/**
	 * ForkJoinTask implementation to download a XML definition from DMS, to validate it and convert
	 * it to Java Object using JaXB.
	 *
	 * @author BBonev
	 * @param <E>
	 *            the element type
	 */
	class DefinitionFetchTask<E extends TopLevelDefinition> extends
			RecursiveTask<Triplet<E, List<Label>, List<FilterDefinition>>> {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 6450448258259721559L;
		/** The location. */
		private FileDescriptor location;
		/** The xml type. */
		private XmlSchemaProvider xmlType;
		/** The jaxb class. */
		private Class<?> jaxbClass;
		/** The callback. */
		private DefinitionCompilerCallback<TopLevelDefinition> callback;
		/** The target class. */
		private Class<E> targetClass;
		/** The update def props. */
		private boolean updateDefProps;
		/** The errors. */
		private List<VerificationMessage> errors;

		/**
		 * Instantiates a new definition fetch task.
		 *
		 * @param location
		 *            the location
		 * @param xmlType
		 *            the xml type
		 * @param targetClass
		 *            the target class
		 * @param jaxbClass
		 *            the jaxb class
		 * @param callback
		 *            the callback
		 * @param updateDefProps
		 *            the update def props
		 * @param errors
		 *            the errors
		 */
		public DefinitionFetchTask(FileDescriptor location, XmlSchemaProvider xmlType,
				Class<E> targetClass, Class<?> jaxbClass,
				DefinitionCompilerCallback<TopLevelDefinition> callback, boolean updateDefProps,
				List<VerificationMessage> errors) {
			this.location = location;
			this.xmlType = xmlType;
			this.jaxbClass = jaxbClass;
			this.callback = callback;
			this.targetClass = targetClass;
			this.updateDefProps = updateDefProps;
			this.errors = errors;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Triplet<E, List<Label>, List<FilterDefinition>> compute() {
			Triplet<E, List<Label>, List<FilterDefinition>> triplet = loadFile(location, xmlType,
					jaxbClass, callback, targetClass, updateDefProps, errors);
			return triplet;
		}
	}

	/**
	 * Task for storing the label definitions in DB.
	 *
	 * @author BBonev
	 */
	class LabelsSaveTask extends RecursiveTask<Void> {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -2514215006959823187L;
		/** The labels. */
		private List<Label> labels;
		/** The callback. */
		private DefinitionCompilerCallback<?> callback;

		/**
		 * Instantiates a new labels save task.
		 *
		 * @param labels
		 *            the labels
		 * @param callback
		 *            the callback
		 */
		public LabelsSaveTask(List<Label> labels, DefinitionCompilerCallback<?> callback) {
			this.labels = labels;
			this.callback = callback;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Void compute() {
			saveLabels(labels, callback);
			return null;
		}
	}

	/**
	 * Task for storing the label definitions in DB.
	 *
	 * @author BBonev
	 */
	class FiltersSaveTask extends RecursiveTask<Void> {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -428828306930920342L;
		/** The labels. */
		private List<FilterDefinition> filterDefinitions;
		/** The callback. */
		private DefinitionCompilerCallback<?> callback;

		/**
		 * Instantiates a new labels save task.
		 *
		 * @param filterDefinitions
		 *            the filter definitions
		 * @param callback
		 *            the callback
		 */
		public FiltersSaveTask(List<FilterDefinition> filterDefinitions,
				DefinitionCompilerCallback<?> callback) {
			this.filterDefinitions = filterDefinitions;
			this.callback = callback;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Void compute() {
			saveFilters(filterDefinitions, callback);
			return null;
		}
	}

	/**
	 * The Class SaveDefinitionProperties.
	 *
	 * @author BBonev
	 * @param <V>
	 *            the value type
	 */
	class SaveDefinitionProperties<V extends DefinitionModel> implements Callable<Void> {

		/** The model. */
		private V model;
		/** The ref model. */
		private V refModel;
		/** The is region. */
		private boolean isRegion;

		/**
		 * Instantiates a new save definition properties.
		 *
		 * @param model
		 *            the model
		 * @param refModel
		 *            the ref model
		 * @param isRegion
		 *            the is region
		 */
		public SaveDefinitionProperties(V model, V refModel, boolean isRegion) {
			this.model = model;
			this.refModel = refModel;
			this.isRegion = isRegion;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Void call() throws Exception {
			if (isRegion) {
				savePropertiesInternal((RegionDefinitionModel) model,
						(RegionDefinitionModel) refModel);
			} else {
				savePropertiesInternal(model, refModel);
			}
			return null;
		}

	}

	/**
	 * Class used for collecting and storing field visibility information.
	 * 
	 * @author bbonev
	 */
	private static class PropertyInfo
			extends
			Pair<List<Pair<PropertyDefinition, DefinitionModel>>, List<Pair<PropertyDefinition, DefinitionModel>>> {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = 6298677791297526462L;

		/**
		 * Instantiates a new property info.
		 */
		public PropertyInfo() {
			super(new LinkedList<Pair<PropertyDefinition, DefinitionModel>>(),
					new LinkedList<Pair<PropertyDefinition, DefinitionModel>>());
		}

		/**
		 * Gets the visible fields.
		 *
		 * @return the visible
		 */
		public List<Pair<PropertyDefinition, DefinitionModel>> getVisible() {
			return getFirst();
		}

		/**
		 * Gets the system fields.
		 *
		 * @return the system
		 */
		public List<Pair<PropertyDefinition, DefinitionModel>> getSystem() {
			return getSecond();
		}
	}
}
