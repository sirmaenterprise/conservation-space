package com.sirma.itt.seip.definition.compile;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashSet;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.commons.utils.string.StringUtils;
import com.sirma.itt.seip.Message;
import com.sirma.itt.seip.MessageType;
import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.concurrent.GenericAsyncTask;
import com.sirma.itt.seip.concurrent.TaskExecutor;
import com.sirma.itt.seip.definition.DictionaryService;
import com.sirma.itt.seip.definition.MutableDictionaryService;
import com.sirma.itt.seip.definition.jaxb.FilterDefinition;
import com.sirma.itt.seip.definition.jaxb.JAXBHelper;
import com.sirma.itt.seip.definition.jaxb.Label;
import com.sirma.itt.seip.definition.jaxb.LabelValue;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.ConditionDefinitionImpl;
import com.sirma.itt.seip.definition.model.FilterDefinitionImpl;
import com.sirma.itt.seip.definition.model.LabelImpl;
import com.sirma.itt.seip.definition.model.PropertyDefinitionProxy;
import com.sirma.itt.seip.definition.model.RegionDefinitionImpl;
import com.sirma.itt.seip.definition.util.DefinitionUtil;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil;
import com.sirma.itt.seip.definition.util.ValidationLoggingUtil.ValidationMessageHolder;
import com.sirma.itt.seip.definition.util.hash.HashCalculator;
import com.sirma.itt.seip.domain.BidirectionalMapping;
import com.sirma.itt.seip.domain.Identity;
import com.sirma.itt.seip.domain.Ordinal;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.Condition;
import com.sirma.itt.seip.domain.definition.Conditional;
import com.sirma.itt.seip.domain.definition.ControlDefinition;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.DisplayType;
import com.sirma.itt.seip.domain.definition.Mergeable;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinition;
import com.sirma.itt.seip.domain.definition.RegionDefinitionModel;
import com.sirma.itt.seip.domain.definition.StateTransition;
import com.sirma.itt.seip.domain.definition.StateTransitionalModel;
import com.sirma.itt.seip.domain.definition.TopLevelDefinition;
import com.sirma.itt.seip.domain.definition.TransitionDefinition;
import com.sirma.itt.seip.domain.definition.Transitional;
import com.sirma.itt.seip.domain.definition.WritablePropertyDefinition;
import com.sirma.itt.seip.domain.definition.label.Displayable;
import com.sirma.itt.seip.domain.exceptions.DefinitionValidationException;
import com.sirma.itt.seip.domain.filter.FilterService;
import com.sirma.itt.seip.domain.xml.XmlSchemaProvider;
import com.sirma.itt.seip.io.FileDescriptor;
import com.sirma.itt.seip.io.TempFileProvider;
import com.sirma.itt.seip.mapping.ObjectMapper;
import com.sirma.itt.seip.time.TimeTracker;
import com.sirma.itt.seip.tx.TransactionSupport;

/**
 * Helper class for loading, validating and converting objects. Contains methods mostly used by concrete implementations
 * of the {@link DefinitionCompilerCallback}.
 *
 * @author BBonev
 */
@ApplicationScoped
public class DefinitionCompilerHelper {

	private static final Pattern ASCII_CHARACTER_PATTERN = Pattern.compile("^[\\x20-\\x7E\\r\\n\\t]+$");
	private static final Pattern SP_CHARACTER_PATTERN = Pattern.compile("[\\s][\\s]+");

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	private LabelService labelService;

	@Inject
	private FilterService filterService;

	@Inject
	private ObjectMapper dozerMapper;

	@Inject
	private TempFileProvider tempFileProvider;

	@Inject
	private MutableDictionaryService mutableDictionaryService;

	@Inject
	private DictionaryService dictionaryService;

	@Inject
	@Any
	private Instance<DefinitionValidator> validators;

	@Inject
	private TransactionSupport dbDao;

	@Inject
	private HashCalculator hashCalculator;

	@Inject
	private TaskExecutor taskExecutor;

	/**
	 * Submit task.
	 *
	 * @param task
	 *            the task
	 */
	public void submitTask(GenericAsyncTask task) {
		taskExecutor.submit(Arrays.asList(task), true);
	}

	/**
	 * Load files content and convert them to top level definition objects and fills the default properties. The loading
	 * is done in parallel.
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
	 * @return the result mapping: definitionIdentifier-&gt;definition
	 */
	public <E extends TopLevelDefinition> List<E> loadFiles(List<FileDescriptor> definitions, Class<E> targetClass,
			Class<?> jaxbClass, XmlSchemaProvider xmlType, boolean updateDefProps,
			DefinitionCompilerCallback<TopLevelDefinition> callback) {
		if (definitions == null || definitions.isEmpty()) {
			return Collections.emptyList();
		}

		TimeTracker timeTracker = TimeTracker.createAndStart();

		List<DefinitionFetchTask<E>> tasks = new LinkedList<>();

		List<Message> messages = ValidationLoggingUtil.getMessages();
		// schedule tasks execution
		for (FileDescriptor dmsFileDescriptor : definitions) {
			DefinitionFetchTask<E> task = new DefinitionFetchTask<>(dmsFileDescriptor, xmlType, targetClass, jaxbClass,
					callback, updateDefProps, messages);
			tasks.add(task);
		}

		List<Label> labels = new LinkedList<>();
		List<FilterDefinition> filterDefinitions = new LinkedList<>();

		// here we wait for the tasks to finish
		List<E> result = waitForDefinitionDownload(callback, tasks, labels, filterDefinitions);

		// if we have any labels save them
		if (!labels.isEmpty()) {
			submitTask(new LabelsSaveTask(labels, callback, ValidationLoggingUtil.acquireMessageHolder()));
		}

		if (!filterDefinitions.isEmpty()) {
			submitTask(new FiltersSaveTask(filterDefinitions, callback));
		}

		LOGGER.debug("Loaded {} {} definition files for {} s", result.size(), callback.getCallbackName(),
				timeTracker.stopInSeconds());
		return result;
	}

	/**
	 * Wait for definition download.
	 *
	 * @param <E>
	 *            the element type
	 * @param callback
	 *            the callback
	 * @param tasks
	 *            the tasks
	 * @param labels
	 *            the labels
	 * @param filterDefinitions
	 *            the filter definitions
	 * @return the list
	 */
	private <E extends TopLevelDefinition> List<E> waitForDefinitionDownload(
			DefinitionCompilerCallback<TopLevelDefinition> callback, List<DefinitionFetchTask<E>> tasks,
			List<Label> labels, List<FilterDefinition> filterDefinitions) {
		List<E> result = new LinkedList<>();
		taskExecutor.execute(tasks);
		collectDataFromFinishedTasks(callback, tasks, result, labels, filterDefinitions);
		return result;
	}

	/**
	 * Collect data from finished tasks.
	 *
	 * @param <E>
	 *            the element type
	 * @param callback
	 *            the callback
	 * @param tasks
	 *            the tasks
	 * @param result
	 *            the result
	 * @param labels
	 *            the labels
	 * @param filterDefinitions
	 *            the filter definitions
	 */
	@SuppressWarnings("unchecked")
	private static <E extends TopLevelDefinition> void collectDataFromFinishedTasks(
			DefinitionCompilerCallback<TopLevelDefinition> callback, List<DefinitionFetchTask<E>> tasks, List<E> result,
			List<Label> labels, List<FilterDefinition> filterDefinitions) {
		for (Iterator<DefinitionFetchTask<E>> it = tasks.iterator(); it.hasNext();) {
			DefinitionFetchTask<E> forkJoinTask = it.next();

			// if tasks completed then we removed it from the wait list
			if (forkJoinTask.isDone()) {
				it.remove();
			}
			if (forkJoinTask.isCompletedNormally()) {
				DefinitionLoadResult definitionData = forkJoinTask.getLoadedDefinition();
				// the triplet could be null if the xsd validation fails
				if (definitionData != null) {
					result.add((E) definitionData.getDefinition());

					labels.addAll(definitionData.getLabels());
					filterDefinitions.addAll(definitionData.getFilters());
				}
			} else {
				handleAbnormalTaskCompletion(callback, forkJoinTask);
			}
		}
	}

	/**
	 * Handle abnormal task completion.
	 *
	 * @param <E>
	 *            the element type
	 * @param callback
	 *            the callback
	 * @param forkJoinTask
	 *            the fork join task
	 */
	private static <E extends TopLevelDefinition> void handleAbnormalTaskCompletion(
			DefinitionCompilerCallback<TopLevelDefinition> callback, DefinitionFetchTask<E> forkJoinTask) {
		Throwable exception = forkJoinTask.getException();
		if (exception instanceof CancellationException) {
			LOGGER.warn("Asynchronous task was cancelled!");
		} else if (exception instanceof DefinitionValidationException) {
			LOGGER.error(exception.getMessage());
			ValidationLoggingUtil.addErrorMessage(exception.getMessage());
		} else if (exception != null) {
			String message = "Failed to process " + callback.getCallbackName() + " definition due to "
					+ exception.getMessage();
			LOGGER.error(message);
			ValidationLoggingUtil.addErrorMessage(message);
			LOGGER.debug("", exception);
		}
	}

	/**
	 * Load the given file from DMS, perform a XSD verification and converts it to internal domain model.
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
	 * @return the pair that contains the parsed top level definition from the file and the list of label data that need
	 *         to be processed
	 */
	@SuppressWarnings("unchecked")
	public <E extends TopLevelDefinition> DefinitionLoadResult loadFile(FileDescriptor location,
			XmlSchemaProvider xmlType, Class<?> jaxbClass,
			DefinitionCompilerCallback<TopLevelDefinition> callback, Class<E> targetClass, boolean updateDefProps,
			List<Message> errors) {
		File file = null;
		try {

			file = getContent(location);
			if (file == null) {
				ValidationLoggingUtil.addMessage(MessageType.ERROR,
						"Failed to download location: " + (location == null ? "undefined" : location.getId()), errors);
				return null;
			}
			if (!validateDefinition(file, xmlType, errors)) {
				ValidationLoggingUtil.addMessage(MessageType.ERROR,
						"Failed to validate file with ID: " + location.getId(), errors);
				return null;
			}
			Object intermidiate = load(file, jaxbClass);
			if (intermidiate == null) {
				ValidationLoggingUtil.addMessage(MessageType.ERROR,
						"Failed to convert the file with ID: " + location.getId() + " to Java class", errors);
				return null;
			}

			List<Label> labels = (List<Label>) callback.getLabelDefinitions(intermidiate);
			List<FilterDefinition> filterDefinitions = (List<FilterDefinition>) callback
					.getFilterDefinitions(intermidiate);

			TopLevelDefinition definition = convert(intermidiate, targetClass);
			if (definition == null) {
				ValidationLoggingUtil.addMessage(MessageType.ERROR, "Failed to parse definition: " + location.getId(),
						errors);
				return null;
			}
			definition.setDmsId(location.getId());
			definition.setContainer(location.getContainerId());

			if (updateDefProps) {
				String definitionId = callback.extractDefinitionId(definition);
				if (definitionId == null) {
					ValidationLoggingUtil.addMessage(MessageType.ERROR,
							"Invalid definition. NO definition ID! in " + location.getId(), errors);
					return null;
				}
				definition.setIdentifier(definitionId);
			}
			return new DefinitionLoadResult(definition, labels, filterDefinitions);
		} finally {
			if (file != null && !file.delete()) {
				LOGGER.warn("Failed to clean the downloaded definition file: {}", file);
			}
		}
	}

	private File getContent(FileDescriptor location) {
		File tempFile = tempFileProvider.createTempFile("definition", null);
		try {
			location.writeTo(tempFile);
		} catch (IOException e) {
			LOGGER.warn("Could not download definition: {}", location.getId(), e);
			tempFileProvider.deleteFile(tempFile);
			tempFile = null;
		}
		return tempFile;
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

		return dozerMapper.map(sourceData, destClass);
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

		return dozerMapper.map(src, destClass);
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

	@SuppressWarnings("static-method")
	public <S> S load(File file, Class<S> src) {
		return JAXBHelper.load(file, src);
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
	@SuppressWarnings("static-method")
	public boolean validateDefinition(File file, XmlSchemaProvider xmlType, List<Message> messages) {
		return JAXBHelper.validateFile(file, xmlType, messages);
	}

	/**
	 * Save the list of labels.
	 *
	 * @param list
	 *            the list
	 * @param callback
	 *            the callback
	 * @param messageHolder
	 *            the message holder
	 */
	void saveLabels(List<Label> list, DefinitionCompilerCallback<?> callback, ValidationMessageHolder messageHolder) {
		List<LabelDefinition> definitions = new ArrayList<>(list.size());
		TimeTracker tracker = TimeTracker.createAndStart();
		ValidationLoggingUtil.initialize(messageHolder);
		Set<String> processedLabels = createHashSet(list.size());
		try {
			for (Label label : list) {
				if (label.getId() == null) {
					LOGGER.warn("Found label without identifier!");
					continue;
				}
				if (!processedLabels.add(label.getId())) {
					LOGGER.warn("Found duplicate label {}", label.getId());
				}
				LabelImpl impl = new LabelImpl();
				impl.setIdentifier(label.getId());
				List<LabelValue> values = label.getValue();
				Map<String, String> map = new LinkedHashMap<>((int) (values.size() * 1.2), 0.95f);
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
		} finally {
			ValidationLoggingUtil.releaseMessageHolder(messageHolder);
		}

		if (!definitions.isEmpty() && !labelService.saveLabels(definitions)) {
			LOGGER.warn("Failed to persist label data from " + callback.getCallbackName());
		}
		LOGGER.debug("Finished label save for {} and took {} ms", callback.getCallbackName(), tracker.stop());

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
		List<com.sirma.itt.seip.domain.filter.Filter> definitions = new ArrayList<>(list.size());

		for (FilterDefinition filter : list) {
			FilterDefinitionImpl convert = convert(filter, FilterDefinitionImpl.class);
			definitions.add(convert);
		}

		if (!definitions.isEmpty() && !filterService.saveFilters(definitions)) {
			LOGGER.warn("Failed to persist filter data from " + callback.getCallbackName());
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
	public void normalizeFields(List<PropertyDefinition> fields, PathElement pathElement, boolean clearFieldId,
			String container) {
		if (fields == null) {
			return;
		}
		DefinitionUtil.sort(fields);
		for (PropertyDefinition definition : fields) {
			if (clearFieldId) {
				((WritablePropertyDefinition) definition).setId(null);
			}
			normalizeFields((WritablePropertyDefinition) definition, pathElement, container);

			// normalize control fields after the current field
			if (definition.getControlDefinition() != null) {
				normalizeFields(definition.getControlDefinition().getFields(), definition.getControlDefinition(),
						clearFieldId, container);
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
	public void normalizeFields(WritablePropertyDefinition fieldDefinitionImpl, PathElement pathElement,
			String container) {
		// clear any extra white spaces in the value
		if (fieldDefinitionImpl.getDefaultValue() != null) {
			fieldDefinitionImpl.setValue(fieldDefinitionImpl.getDefaultValue().trim());
		}
		fieldDefinitionImpl.setContainer(container);
		fieldDefinitionImpl.setParentPath(PathHelper.getPath(pathElement));
		String type = fieldDefinitionImpl.getType();
		if (fieldDefinitionImpl.getDataType() == null && StringUtils.isNotNullOrEmpty(type)) {
			TypeParser parser = TypeParser.parse(type);
			// updates the type name
			fieldDefinitionImpl.setType(parser.type);
			String definitionName = parser.getDataTypeDefinitionName();
			DataTypeDefinition dataTypeDefinition = dictionaryService.getDataTypeDefinition(definitionName);
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
				String message = "Failed validator " + validator.getClass().getSimpleName() + " on region model "
						+ model.getIdentifier();
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
				String message = "Failed validator " + validator.getClass().getSimpleName() + " on definition model "
						+ model.getIdentifier();
				LOGGER.error(message);
				ValidationLoggingUtil.addErrorMessage(message);
			}
			valid &= isValid;
		}
		return valid;
	}

	/**
	 * Removes the deleted elements. Removes elements that implement displayable and have display type set to
	 * {@link DisplayType#DELETE}. The method processed the {@link RegionDefinitionModel}, {@link DefinitionModel} and
	 * {@link Transitional} interfaces.
	 *
	 * @param base
	 *            the base
	 */
	public void removeDeletedElements(Object base) {
		if (base instanceof RegionDefinitionModel) {
			removeDeletedElements(((RegionDefinitionModel) base).getRegions());
		}
		if (base instanceof DefinitionModel) {
			removeDeletedElements(((DefinitionModel) base).getFields());
		}
		if (base instanceof Transitional) {
			removeDeletedElements(((Transitional) base).getTransitions());
		}
	}

	/**
	 * Removes elements from the given collection that are marked as {@link DisplayType#DELETE}.
	 *
	 * @param <E>
	 *            the collection type
	 * @param collection
	 *            the collection to process
	 */
	private <E extends Identity> void removeDeletedElements(Collection<E> collection) {
		for (Iterator<E> it = collection.iterator(); it.hasNext();) {
			E definition = it.next();
			if (canRemoveElement(definition)) {
				it.remove();
				LOGGER.warn("Removing element of type {} and id=[{}] due to it was marked for deletion: displayType={}",
						definition.getClass(), definition.getIdentifier(), DisplayType.DELETE);
			} else {
				removeDeletedElements(definition);
			}
		}
	}

	/**
	 * Checks if can remove element. Only if it's of type {@link DisplayType} and has property
	 * {@link DisplayType#DELETE}.
	 *
	 * @param object
	 *            the object to check
	 * @return true, if should remove the element
	 */
	private static boolean canRemoveElement(Object object) {
		return object instanceof Displayable && ((Displayable) object).getDisplayType() == DisplayType.DELETE;
	}

	/**
	 * Removes all regions that are mark as DisplayType = SYSTEM.
	 *
	 * @param model
	 *            the region model definition
	 */
	public void synchRegionProperties(RegionDefinitionModel model) {
		for (Iterator<RegionDefinition> regionIt = model.getRegions().iterator(); regionIt.hasNext();) {
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
				noVisible(info);
			} else if (info.getVisible().size() == 1) {
				oneVisible(info);
			} else {
				moreThanOneVisible(info);
			}
			// more then one visible we cannot handle them here - the definition is invalid
		}
	}

	private void noVisible(PropertyInfo info) {
		if (info.getSystem().size() == 1) {
			// we have only one field so we a good to go and no need to continue
			return;
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
			return;
		}
		List<Pair<PropertyDefinition, DefinitionModel>> list = info.getSystem();
		removeFields(list.subList(1, list.size()));
	}

	@SuppressWarnings("unchecked")
	private static void oneVisible(PropertyInfo info) {
		// we can copy all data from the system field to the visible one except the
		// visibility
		PropertyDefinition propertyDefinition = info.getVisible().get(0).getFirst();
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
			if (propertyDefinition instanceof Mergeable) {
				((Mergeable<PropertyDefinition>) propertyDefinition).mergeFrom(pair.getFirst());
			}
		}
		// remove all system fields we have only one visible
		removeFields(info.getSystem());
	}

	private static void moreThanOneVisible(PropertyInfo info) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getVisible()) {
			String message = null;
			if (pair.getSecond() instanceof RegionDefinition) {
				message = "Found duplicate VISIBLE field [" + pair.getFirst().getIdentifier() + "] in ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "/" + pair.getSecond().getIdentifier()
						+ "]";
			} else {
				message = "Found duplicate VISIBLE field [" + pair.getFirst().getIdentifier() + "] from ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "]";
			}
			ValidationLoggingUtil.addErrorMessage(message);
			LOGGER.error(message);
		}
		printErrorMessagesForSystemFields(info);
	}

	/**
	 * Prints the error messages for system fields.
	 *
	 * @param info
	 *            the info
	 */
	private static void printErrorMessagesForSystemFields(PropertyInfo info) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : info.getSystem()) {
			String message = null;
			if (pair.getSecond() instanceof RegionDefinition) {
				message = "Found duplicate field [" + pair.getFirst().getIdentifier() + "] in ["
						+ PathHelper.getPath((PathElement) pair.getSecond()) + "/" + pair.getSecond().getIdentifier()
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
	private static void removeFields(Collection<Pair<PropertyDefinition, DefinitionModel>> subList) {
		for (Pair<PropertyDefinition, DefinitionModel> pair : subList) {
			boolean removed = pair.getSecond().getFields().remove(pair.getFirst());
			String path;
			if (pair.getSecond() instanceof RegionDefinition) {
				path = pair.getFirst().getIdentifier() + "] from [" + PathHelper.getPath((PathElement) pair.getSecond())
						+ "/" + pair.getSecond().getIdentifier() + "]";
			} else {
				path = pair.getFirst().getIdentifier() + "] from [" + PathHelper.getPath((PathElement) pair.getSecond())
						+ "]";
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
	private static Map<String, PropertyInfo> collectFieldsInfo(DefinitionModel model,
			Map<String, PropertyInfo> mapping) {
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			PropertyInfo info = mapping.get(propertyDefinition.getIdentifier());
			if (info == null) {
				info = new PropertyInfo();
				mapping.put(propertyDefinition.getIdentifier(), info);
			}
			Pair<PropertyDefinition, DefinitionModel> pair = new Pair<>(propertyDefinition, model);
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
	private static Map<String, PropertyInfo> collectFieldsInfo(RegionDefinitionModel model) {
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
	public void setDefaultProperties(RegionDefinitionModel regionModel, String modelPrefix, boolean setToAll) {
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
		// do not set DMS type to fields that are defined with prefix already
		if (modelPrefix != null && StringUtils.isNullOrEmpty(impl.getDmsType()) && !impl.getName().contains(":")) {
			impl.setDmsType(modelPrefix + impl.getName());
		}
		impl.setDefaultProperties();
		if (impl.getControlDefinition() != null) {
			setDefaultProperties(impl.getControlDefinition(), modelPrefix);
		}
	}

	/**
	 * Validate expression based on the given region definition model. If all fields defined in the expression are
	 * editable in the given definition model then the expression is valid.
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
		boolean hasRegions = model instanceof RegionDefinitionModel
				&& ((RegionDefinitionModel) model).getRegions().isEmpty();
		if (StringUtils.isNullOrEmpty(expression) || model.getFields().isEmpty() && !hasRegions) {
			LOGGER.warn("No fields in the target model to support the expression: " + expression);
			return false;
		}
		if (!ASCII_CHARACTER_PATTERN.matcher(expression).matches()) {
			LOGGER.warn("Found cyrillic characters in the expression: " + expression);
			return false;
		}
		// we first collect all fields from the expression
		Set<String> fields = new LinkedHashSet<>(DefinitionUtil.getRncFields(expression));
		// if no fields are found then the expression is not valid
		if (fields.isEmpty()) {
			LOGGER.warn("No fields found into the expression: " + expression);
			// TODO: Fix FIELD_PATTERN for accepting new condition
			// +n[i-documentType], +n[o-documentType] and negative values
		}
		Set<DisplayType> allowedTypes = EnumSet.of(DisplayType.EDITABLE, DisplayType.READ_ONLY, DisplayType.HIDDEN,
				DisplayType.SYSTEM);
		Set<String> modelFields = collectFieldNames(model, allowedTypes);
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
	 * Collect field names from the given definition model and that match the given display type.
	 *
	 * @param model
	 *            the model
	 * @param allowedTypes
	 *            the list display types to collect
	 */
	private static Set<String> collectFieldNames(DefinitionModel model, Set<DisplayType> allowedTypes) {
		return model
				.fieldsStream()
					.flatMap(PropertyDefinition::stream)
					.filter(property -> allowedTypes.contains(property.getDisplayType()))
					.map(PropertyDefinition::getName)
					.collect(Collectors.toSet());
	}

	/**
	 * Validate expressions. against the given target model. If any of conditions is invalid it will be removed from the
	 * list of conditions
	 *
	 * @param targetModel
	 *            the target model
	 * @param conditions
	 *            the conditions
	 */
	public void validateExpressions(DefinitionModel targetModel, Conditional conditions) {
		if (conditions.getConditions() == null || conditions.getConditions().isEmpty()) {
			return;
		}
		for (Iterator<Condition> it = conditions.getConditions().iterator(); it.hasNext();) {
			Condition condition = it.next();
			if (condition.getExpression() == null || !validateExpression(targetModel, condition.getExpression())) {
				it.remove();

				LOGGER.warn(" !!! Expression in condition: " + condition + " is not valid and will be removed !!!");
			} else {
				// the expression is valid so we will check the white space
				Matcher matcher = SP_CHARACTER_PATTERN.matcher(condition.getExpression());
				String updatedExpression = matcher.replaceAll(" ");
				if (LOGGER.isTraceEnabled() && !condition.getExpression().equals(updatedExpression)) {
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
	public void validateModelConditions(RegionDefinitionModel targetModel, RegionDefinitionModel model) {
		if (targetModel == null || model == null) {
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
		if (targetModel == null || model == null) {
			return;
		}
		for (PropertyDefinition propertyDefinition : model.getFields()) {
			validateExpressions(targetModel, propertyDefinition);
			validateModelConditions(targetModel, propertyDefinition.getControlDefinition());
		}
	}

	/**
	 * Save properties of the given region definition model. First saves the root properties then for each region
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
	 * Save properties of the given region definition model. First saves the root properties then for each region
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
					refModel != null ? PathHelper.find(refModel.getRegions(), regionDefinition.getIdentifier()) : null);
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
		dbDao.invokeInNewTx(new SaveDefinitionProperties<>(model, refModel, false));
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
		List<PropertyDefinition> updatedFields = new ArrayList<>(model.getFields().size());

		for (PropertyDefinition definition : model.getFields()) {
			PropertyDefinition oldProperty = null;
			if (refModel != null) {
				oldProperty = PathHelper.find(refModel.getFields(), definition.getIdentifier());
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
			PropertyDefinition changed = mutableDictionaryService.savePropertyIfChanged(definition, oldProperty);
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
	@SuppressWarnings("static-method")
	public void sort(List<? extends Ordinal> sortables) {
		DefinitionUtil.sort(sortables);
	}

	/**
	 * Optimize state transitions. The method should remove any duplicate transitions and sort the transitions with
	 * conditions and the one without
	 *
	 * @param definition
	 *            the definition
	 */
	@SuppressWarnings("static-method")
	public void optimizeStateTransitions(StateTransitionalModel definition) {
		List<StateTransition> stateTransitions = definition.getStateTransitions();
		List<StateTransition> sortedTransitions = new ArrayList<>(stateTransitions.size());
		Map<String, List<StateTransition>> transitionsMapping = CollectionUtils
				.createLinkedHashMap(stateTransitions.size());
		for (StateTransition stateTransition : stateTransitions) {
			String identifier = stateTransition.getFromState() + "|" + stateTransition.getTransitionId();
			CollectionUtils.addValueToMap(transitionsMapping, identifier, stateTransition);
		}

		for (Entry<String, List<StateTransition>> entry : transitionsMapping.entrySet()) {
			List<StateTransition> list = entry.getValue();
			if (list.size() == 1) {
				sortedTransitions.add(list.get(0));
			} else {
				List<StateTransition> sorted = sortAndRemoveDuplicateStateTransitions(list);
				if (list.size() != sorted.size()) {
					LOGGER.debug("Removed duplicate state transition {} from [{}]", sorted.get(0),
							definition.getIdentifier());
					LOGGER.trace("For [{}] the old transitions are {} and the new are {}", definition.getIdentifier(),
							list, sorted);
				}
				sortedTransitions.addAll(sorted);
			}
		}
		if (LOGGER.isTraceEnabled() && !definition.getStateTransitions().equals(sortedTransitions)) {
			LOGGER.trace(
					"Performed transition optimizations on definition [{}]. Results are:\n\tbefore:{}\n\tafter :{}",
					definition.getIdentifier(), definition.getStateTransitions(), sortedTransitions);
		}
		definition.getStateTransitions().clear();
		definition.getStateTransitions().addAll(sortedTransitions);

	}

	/**
	 * Sort and remove duplicate state transitions.
	 *
	 * @param list
	 *            the list
	 * @return the list
	 */
	private static List<StateTransition> sortAndRemoveDuplicateStateTransitions(List<StateTransition> list) {
		List<StateTransition> sorted = new LinkedList<>();
		Map<String, StateTransition> withNoConditions = new LinkedHashMap<>();
		for (StateTransition transition : list) {
			if (transition.getConditions().isEmpty()) {
				if (!withNoConditions.containsKey(transition.getToState())) {
					withNoConditions.put(transition.getToState(), transition);
				}
			} else {
				sorted.add(transition);
			}
		}
		// add all operations without conditions at the end
		sorted.addAll(withNoConditions.values());
		return sorted;
	}

	/**
	 * ForkJoinTask implementation to download a XML definition from DMS, to validate it and convert it to Java Object
	 * using JaXB.
	 *
	 * @author BBonev
	 * @param <E>
	 *            the element type
	 */
	class DefinitionFetchTask<E extends TopLevelDefinition> extends GenericAsyncTask {

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
		private List<Message> errors;
		private DefinitionLoadResult loadedDefinition;

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
		public DefinitionFetchTask(FileDescriptor location, XmlSchemaProvider xmlType, Class<E> targetClass,
				Class<?> jaxbClass, DefinitionCompilerCallback<TopLevelDefinition> callback, boolean updateDefProps,
				List<Message> errors) {
			super();
			this.location = location;
			this.xmlType = xmlType;
			this.jaxbClass = jaxbClass;
			this.callback = callback;
			this.targetClass = targetClass;
			this.updateDefProps = updateDefProps;
			this.errors = errors;
		}

		@Override
		protected boolean executeTask() throws Exception {
			loadedDefinition = loadFile(location, xmlType, jaxbClass, callback, targetClass, updateDefProps, errors);
			return loadedDefinition != null;
		}

		/**
		 * Gets the loaded definition.
		 *
		 * @return the loaded definition
		 */
		public DefinitionLoadResult getLoadedDefinition() {
			return loadedDefinition;
		}
	}

	/**
	 * Represents a result of asynchronous definition load
	 *
	 * @author BBonev
	 */
	static class DefinitionLoadResult {
		private TopLevelDefinition definition;
		private List<Label> labels;
		private List<FilterDefinition> filters;

		/**
		 * Instantiates a new definition load result.
		 *
		 * @param definition
		 *            the definition
		 * @param labels
		 *            the labels
		 * @param filters
		 *            the filters
		 */
		DefinitionLoadResult(TopLevelDefinition definition, List<Label> labels, List<FilterDefinition> filters) {
			this.definition = definition;
			this.labels = labels == null ? Collections.emptyList() : labels;
			this.filters = filters == null ? Collections.emptyList() : filters;
		}

		TopLevelDefinition getDefinition() {
			return definition;
		}

		List<Label> getLabels() {
			return labels;
		}

		List<FilterDefinition> getFilters() {
			return filters;
		}
	}

	/**
	 * Task for storing the label definitions in DB.
	 *
	 * @author BBonev
	 */
	class LabelsSaveTask extends GenericAsyncTask {

		/**
		 * Comment for serialVersionUID.
		 */
		private static final long serialVersionUID = -2514215006959823187L;
		/** The labels. */
		private List<Label> labels;
		/** The callback. */
		private DefinitionCompilerCallback<?> callback;
		private ValidationMessageHolder messages;

		/**
		 * Instantiates a new labels save task.
		 *
		 * @param labels
		 *            the labels
		 * @param callback
		 *            the callback
		 * @param messages
		 *            the messages
		 */
		public LabelsSaveTask(List<Label> labels, DefinitionCompilerCallback<?> callback,
				ValidationMessageHolder messages) {
			this.labels = labels;
			this.callback = callback;
			this.messages = messages;
		}

		@Override
		protected boolean executeTask() throws Exception {
			try {
				saveLabels(labels, callback, messages);
			} catch (RuntimeException e) {
				LOGGER.error("Error while saving {} labels", callback.getCallbackName(), e);
			}
			return true;
		}

	}

	/**
	 * Task for storing the label definitions in DB.
	 *
	 * @author BBonev
	 */
	class FiltersSaveTask extends GenericAsyncTask {

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
		public FiltersSaveTask(List<FilterDefinition> filterDefinitions, DefinitionCompilerCallback<?> callback) {
			this.filterDefinitions = filterDefinitions;
			this.callback = callback;
		}

		@Override
		protected boolean executeTask() throws Exception {
			try {
				saveFilters(filterDefinitions, callback);
			} catch (RuntimeException e) {
				LOGGER.error("Error while saving {} filters", callback.getCallbackName(), e);
			}
			return false;
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
				savePropertiesInternal((RegionDefinitionModel) model, (RegionDefinitionModel) refModel);
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
	private static class PropertyInfo extends
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
