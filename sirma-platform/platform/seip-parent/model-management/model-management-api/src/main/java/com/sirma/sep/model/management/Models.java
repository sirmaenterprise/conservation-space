package com.sirma.sep.model.management;

import com.sirma.itt.seip.Copyable;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.hierarchy.ModelHierarchyClass;
import com.sirma.sep.model.management.meta.ModelsMetaInfo;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Used to hold converted models - semantic class & property models, definition models and their hierarchy.
 *
 * @author Mihail Radkov
 */
public class Models implements Walkable, Copyable<Models> {

	private Map<String, ModelClass> classes;

	private Map<String, ModelProperty> properties;

	private Map<String, ModelDefinition> definitions;

	private List<ModelHierarchyClass> modelHierarchy;

	private ModelsMetaInfo modelsMetaInfo;

	private DetachedModelNodesStore detachedModelNodesStore = new DetachedModelNodesStore();

	private long version;

	public Map<String, ModelClass> getClasses() {
		return classes;
	}

	public Map<String, ModelProperty> getProperties() {
		return properties;
	}

	public Map<String, ModelDefinition> getDefinitions() {
		return definitions;
	}

	public void setClasses(Map<String, ModelClass> classes) {
		this.classes = classes;
		this.classes.values().forEach(assignDetachedStore());
	}

	public void setProperties(Map<String, ModelProperty> properties) {
		this.properties = properties;
		this.properties.values().forEach(assignDetachedStore());
	}

	public void setDefinitions(Map<String, ModelDefinition> definitions) {
		this.definitions = definitions;
		this.definitions.values().forEach(assignDetachedStore());
	}

	private <M extends AbstractModelNode<M, ? extends ModelNode>> Consumer<M> assignDetachedStore() {
		return node -> node.setDetachedModelNodesStore(detachedModelNodesStore);
	}

	public List<ModelHierarchyClass> getModelHierarchy() {
		return modelHierarchy;
	}

	public void setModelHierarchy(List<ModelHierarchyClass> modelHierarchy) {
		this.modelHierarchy = modelHierarchy;
	}

	public ModelsMetaInfo getModelsMetaInfo() {
		return modelsMetaInfo;
	}

	public void setModelsMetaInfo(ModelsMetaInfo modelsMetaInfo) {
		this.modelsMetaInfo = modelsMetaInfo;
	}

	public long getVersion() {
		return version;
	}

	void setVersion(long modelVersion) {
		this.version = modelVersion;
	}

	/**
	 * Notify the model that some change has occurred.
	 *
	 * @return the new model version after the update
	 */
	public long modelUpdated() {
		return ++version;
	}

	/**
	 * Determines if it is holding any models or not.
	 *
	 * @return true if there are no models or false otherwise
	 */
	public boolean isEmpty() {
		return classes == null || classes.size() < 1;
	}

	/**
	 * Retrieves a node from the store that matches the path.
	 *
	 * @param path the path to the node to return
	 * @return the found node or empty optional
	 */
	public Optional<ModelNode> getDetachedNode(Path path) {
		return detachedModelNodesStore.getDetachedNode(path);
	}

	/**
	 * Retrieves an attribute from the store that matches the path.
	 *
	 * @param path the path to the requested attribute to return if present
	 * @return the found attribute or empty optional
	 */
	public Optional<ModelAttribute> getDetachedAttribute(Path path) {
		return detachedModelNodesStore.getDetachedAttribute(path);
	}

	@Override
	public Object walk(Path step) {
		switch (step.getName()) {
			case ModelClass.MODEL_TYPE:
				return step.proceed(classes.computeIfAbsent(step.getValue(), this::createNewModelClass));
			case ModelProperty.MODEL_TYPE:
				return step.proceed(properties.computeIfAbsent(step.getValue(), this::createModelProperty));
			case ModelDefinition.MODEL_TYPE:
				return step.proceed(definitions.computeIfAbsent(step.getValue(), this::createModelDefinition));
			default:
				throw new IllegalArgumentException("Invalid step " + step);
		}
	}

	private ModelClass createNewModelClass(String id) {
		return new ModelClass()
				.setId(id)
				.setModelsMetaInfo(modelsMetaInfo)
				.setDetachedModelNodesStore(detachedModelNodesStore);
	}

	private ModelProperty createModelProperty(String id) {
		return new ModelProperty()
				.setId(id)
				.setModelsMetaInfo(modelsMetaInfo)
				.setDetachedModelNodesStore(detachedModelNodesStore);
	}

	private ModelDefinition createModelDefinition(String id) {
		return new ModelDefinition()
				.setId(id)
				.setDetachedModelNodesStore(detachedModelNodesStore)
				.setModelsMetaInfo(modelsMetaInfo);
	}

	@Override
	public Models createCopy() {
		Models copy = new Models();
		copy.setClasses(copy(classes, ModelClass::getId));
		copy.setDefinitions(copy(definitions, ModelDefinition::getId));
		copy.setProperties(copy(properties, ModelProperty::getId));
		// models meta info should not be editable in the first place
		copy.setModelsMetaInfo(modelsMetaInfo);
		copy.version = version;
		return copy;
	}

	private <C extends Copyable<C>> Map<String, C> copy(Map<String, C> toCopy, Function<C, String> idResolver) {
		return toCopy.values()
				.stream()
				.map(Copyable::createCopy)
				.collect(CollectionUtils.toIdentityMap(idResolver));
	}

	/**
	 * Check if the given path points a class or class attribute
	 *
	 * @param path the path to check
	 * @return true if the path will access a model class if resolved
	 */
	public static boolean isClass(Path path) {
		return path.getName().equals(ModelClass.MODEL_TYPE);
	}

	/**
	 * Check if the given path points a definition or definition field or attribute
	 *
	 * @param path the path to check
	 * @return true if the path will access a model definition if resolved
	 */
	public static boolean isDefinition(Path path) {
		return path.getName().equals(ModelDefinition.MODEL_TYPE);
	}

	/**
	 * Check if the given path points a semantic property or property attribute
	 *
	 * @param path the path to check
	 * @return true if the path will access a model property if resolved
	 */
	public static boolean isProperty(Path path) {
		return path.getName().equals(ModelProperty.MODEL_TYPE);
	}

	/**
	 * Creates a path to the given class.
	 *
	 * @param value the class name to use for the path building
	 * @return a path instance to the given class
	 */
	public static Path createClassPath(String value) {
		Objects.requireNonNull(value, "Cannot create path to a null class");
		return Path.create(ModelClass.MODEL_TYPE, value);
	}

	/**
	 * Creates a path to the given definition.
	 *
	 * @param value the definition id to use for the path building
	 * @return a path instance to the given definition
	 */
	public static Path createDefinitionPath(String value) {
		Objects.requireNonNull(value, "Cannot create path to a null definition");
		return Path.create(ModelDefinition.MODEL_TYPE, value);
	}

	/**
	 * Creates a path to the given property.
	 *
	 * @param value the class property to use for the path building
	 * @return a path instance to the given property
	 */
	public static Path createPropertyPath(String value) {
		Objects.requireNonNull(value, "Cannot create path to a null property");
		return Path.create(ModelProperty.MODEL_TYPE, value);
	}
}
