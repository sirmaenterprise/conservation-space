package com.sirma.sep.model.management.deploy.definition.steps;

import static com.sirma.sep.model.management.converter.ModelConverterUtilities.normalizeLabelsMap;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import com.sirma.sep.model.ModelNode;
import com.sirma.sep.model.management.ModelAttribute;
import com.sirma.sep.model.management.ModelDefinition;
import com.sirma.sep.model.management.Models;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.definition.LabelProvider;

/**
 * Base step for updating different types of labels of {@link ModelNode} via {@link LabelProvider}.
 *
 * @author Mihail Radkov
 */
public abstract class DefinitionNodeLabelsUpdateStep extends DefinitionChangeSetStep {

	private final LabelProvider labelProvider;

	protected DefinitionNodeLabelsUpdateStep(LabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Returns the name of the model attribute holding the actual labels.
	 * E.g. {@link com.sirma.sep.model.management.definition.DefinitionModelAttributes#LABEL}
	 *
	 * @return the label attribute name
	 */
	protected abstract String getLabelAttributeName();

	/**
	 * Returns the name of the model attribute holding the labels identifier.
	 * E.g. {@link com.sirma.sep.model.management.definition.DefinitionModelAttributes#LABEL_ID}
	 *
	 * @return the label key attribute
	 */
	protected abstract String getLabelAttributeKey();

	@Override
	protected boolean checkPath(Path path) {
		Path head = path.head();
		Path next = head.next();
		Path tail = path.tail();
		// Should not match DefinitionLabelsUpdateStep which uses the code lists provider
		return getLabelAttributeName().equals(tail.getValue()) && !tail.equals(next);
	}

	@Override
	public void handle(DefinitionChangeSetPayload definitionChangeSetPayload) {
		Models models = definitionChangeSetPayload.getModels();
		ModelDefinition modelDefinition = definitionChangeSetPayload.getDefinition();
		Path path = definitionChangeSetPayload.getLastChange().getPath();

		ModelAttribute labelAttribute = getLabelAttribute(models, path);
		ModelNode owningNode = getOwningNode(labelAttribute);
		// if the node or the attribute are detached, we have nothing to do more
		// except to remove the label if applicable
		if (owningNode.isDetached() || labelAttribute.isDetached()) {
			removeLabels(models, modelDefinition, owningNode);
			return;
		}

		// Using current value because the change set could be for partial modification of the labels map.
		Object currentValue = labelAttribute.getValue();
		if (currentValue instanceof Map) {
			if (((Map) currentValue).isEmpty()) {
				removeLabels(models, modelDefinition, owningNode);
			} else {
				updateLabels(models, modelDefinition, owningNode, (Map) currentValue);
			}
		} else if (currentValue == null) {
			removeLabels(models, modelDefinition, owningNode);
		} else {
			throw new IllegalArgumentException("Unsupported label value type " + currentValue.getClass().getSimpleName());
		}
	}

	private void updateLabels(Models models, ModelDefinition modelDefinition, ModelNode owningNode, Map newValue) {
		// Get or create new label ID; if defined in another definition -> create new label id
		String labelId = getLabelId(models, owningNode)
				.filter(definedInCurrentModel(modelDefinition))
				.orElseGet(generateLabelId(modelDefinition, owningNode));
		Map<String, String> labels = normalizeLabelsMap(newValue);
		// Reassigning the label ID in case it was generated
		owningNode.addAttribute(getLabelAttributeKey(), labelId);
		labelProvider.saveLabels(labelId, modelDefinition.getId(), labels);
	}

	private void removeLabels(Models models, ModelDefinition modelDefinition,
			ModelNode owningNode) {
		Optional<String> labelId = getLabelId(models, owningNode);
		labelId.filter(definedInCurrentModel(modelDefinition))
				.ifPresent(labelProvider::removeLabels);
		owningNode.removeAttribute(getLabelAttributeKey());
	}

	private static ModelAttribute getLabelAttribute(Models models, Path path) {
		Optional<ModelAttribute> detached = models.getDetachedAttribute(path);
		if (detached.isPresent()) {
			return detached.get();
		}
		Object node = models.walk(path);
		if (node instanceof ModelAttribute) {
			return (ModelAttribute) node;
		} else if (node == null) {
			throw new IllegalArgumentException("Missing model attribute for " + path.prettyPrint());
		} else {
			throw new IllegalArgumentException("Expected model attribute type, got " + node.getClass().getSimpleName());
		}
	}

	private static ModelNode getOwningNode(ModelAttribute labelAttribute) {
		if (labelAttribute.getContext() != null) {
			return labelAttribute.getContext();
		}
		throw new IllegalArgumentException("Missing owning node for attribute " + labelAttribute.getName());
	}

	private Optional<String> getLabelId(Models models, ModelNode modelNode) {
		Path labelAttributePath = ModelAttribute.createPath(getLabelAttributeKey());
		Path absoluteLabelAttributePath = modelNode.getPath().append(labelAttributePath);

		Optional<ModelAttribute> detachedLabelAttribute = models.getDetachedAttribute(absoluteLabelAttributePath);

		Optional<String> detachedLabelId = detachedLabelAttribute.map(ModelAttribute::getValue)
				.map(Object::toString)
				.filter(StringUtils::isNotBlank);
		if (detachedLabelId.isPresent()) {
			return detachedLabelId;
		}
		Object attribute = modelNode.walk(labelAttributePath);
		return Optional.ofNullable((ModelAttribute) attribute)
				.map(ModelAttribute::getValue)
				.map(Object::toString)
				.filter(StringUtils::isNotBlank);
	}

	private Predicate<String> definedInCurrentModel(ModelDefinition modelDefinition) {
		return labelId -> labelProvider.definedIn(labelId).contains(modelDefinition.getId());
	}

	private Supplier<String> generateLabelId(ModelDefinition modelDefinition, ModelNode modelNode) {
		return () -> modelDefinition.getId() + "." + modelNode.getId() + "." + getLabelAttributeName();
	}

}
