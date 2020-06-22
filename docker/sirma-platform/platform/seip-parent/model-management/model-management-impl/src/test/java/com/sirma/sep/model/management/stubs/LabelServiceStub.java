package com.sirma.sep.model.management.stubs;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.mockito.invocation.InvocationOnMock;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.definition.label.LabelDefinition;
import com.sirma.itt.seip.definition.label.LabelService;
import com.sirma.itt.seip.definition.model.LabelImpl;

/**
 * Stubs the behaviour of a {@link LabelService} {@link org.mockito.Mock}.
 *
 * @author Mihail Radkov
 */
public class LabelServiceStub {

	// TODO: move rest of the stubs in this package

	private final Map<String, LabelDefinition> labels;

	/**
	 * Initializes a stub with the provided label service mock.
	 *
	 * @param labelServiceMock {@link org.mockito.Mock} of {@link LabelService}
	 */
	public LabelServiceStub(LabelService labelServiceMock) {
		this.labels = new HashMap<>();
		when(labelServiceMock.getLabel(any())).then(this::getLabel);
		when(labelServiceMock.getLabelsDefinedIn(any())).then(this::getDefinedIn);
		when(labelServiceMock.saveLabel(any())).then(this::save);
	}

	public void withLabelDefinition(LabelDefinition labelDefinition) {
		labels.put(labelDefinition.getIdentifier(), labelDefinition);
	}

	public Map<String, LabelDefinition> getLabelsMap() {
		return labels;
	}

	public static LabelDefinition build(String id, Map<String, String> labels) {
		return build(id, null, labels);
	}

	public static LabelDefinition build(String id, String definedIn, Map<String, String> labels) {
		LabelImpl labelDefinition = new LabelImpl();
		labelDefinition.setIdentifier(id);
		labelDefinition.addDefinedIn(definedIn);
		labelDefinition.setLabels(labels);
		return labelDefinition;
	}

	private Object getLabel(InvocationOnMock invocation) {
		String labelId = invocation.getArgumentAt(0, String.class);
		// Copying to avoid modification outside of LabelService.save()
		return copy(labels.get(labelId));
	}

	private Object getDefinedIn(InvocationOnMock invocation) {
		String definitionId = invocation.getArgumentAt(0, String.class);
		return labels.values().stream().filter(label ->
				!CollectionUtils.isEmpty(label.getDefinedIn()) && label.getDefinedIn().contains(definitionId)).collect(Collectors.toList());
	}

	private Object save(InvocationOnMock invocation) {
		LabelDefinition label = invocation.getArgumentAt(0, LabelDefinition.class);
		labels.put(label.getIdentifier(), label);
		return true;
	}

	private static LabelDefinition copy(LabelDefinition label) {
		if (label == null) {
			return null;
		}
		String definedIn = CollectionUtils.isNotEmpty(label.getDefinedIn()) ? label.getDefinedIn().iterator().next() : null;
		return build(label.getIdentifier(), definedIn, new HashMap<>(label.getLabels()));
	}
}
