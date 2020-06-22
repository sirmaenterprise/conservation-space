package com.sirma.sep.model.management.deploy.definition;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;

import com.sirma.itt.seip.domain.validation.ValidationMessage;
import com.sirma.sep.model.management.Path;
import com.sirma.sep.model.management.deploy.ChangeSetAggregator;
import com.sirma.sep.model.management.deploy.ModelChangeSetExtension;
import com.sirma.sep.model.management.deploy.definition.steps.DefinitionChangeSetPayload;
import com.sirma.sep.model.management.deploy.definition.steps.DefinitionChangeSetStep;
import com.sirma.sep.model.management.operation.ModelChangeSetInfo;

/**
 * Manager for applying {@link ModelChangeSetInfo} upon given {@link com.sirma.sep.model.management.ModelDefinition} and
 * {@link com.sirma.itt.seip.domain.definition.GenericDefinition}.
 * <p>
 * Changes are handled by using different {@link DefinitionChangeSetStep} which are resolved according to the specific
 * {@link ModelChangeSetInfo} {@link Path}.
 * <p>
 * Before handling the changes, they are aggregated to {@link ModelChangeSetExtension} to skip any intermediate ones.
 *
 * @author Mihail Radkov
 * @see ChangeSetAggregator
 * @see ModelChangeSetExtension
 */
class DefinitionChangeSetManager {

	@Inject
	private Instance<DefinitionChangeSetStep> steps;

	/**
	 * Validates the provided {@link DefinitionDeploymentRequest} if there are steps that support
	 * {@link DefinitionChangeSetStep#validate(DefinitionChangeSetPayload)}.
	 *
	 * @param request the request to process and validate
	 * @return list of validation messages, if empty the request is valid
	 */
	public List<ValidationMessage> validate(DefinitionDeploymentRequest request) {
		List<ValidationMessage> validationMessages = new LinkedList<>();
		process(request, (step, payload) -> {
			List<ValidationMessage> messages = step.validate(payload);
			validationMessages.addAll(messages);
		});
		return validationMessages;
	}

	/**
	 * Processes the provided {@link DefinitionDeploymentRequest}
	 *
	 * @param request the definition deployment request to process
	 */
	@Transactional(REQUIRES_NEW)
	public void apply(DefinitionDeploymentRequest request) {
		// New transaction is needed because the steps could perform database modifications that need to be flushed
		process(request, DefinitionChangeSetStep::handle);
	}

	private void process(DefinitionDeploymentRequest request,
			BiConsumer<DefinitionChangeSetStep, DefinitionChangeSetPayload> stepConsumer) {
		Map<String, List<ModelChangeSetExtension>> pathToChangesMap = ChangeSetAggregator.aggregate(request.getChanges());

		pathToChangesMap.forEach((key, list) -> {
			ModelChangeSetExtension changeSetExtension = list.get(0);
			Optional<DefinitionChangeSetStep> changeSetStep = getStep(changeSetExtension.getPath());
			changeSetStep.ifPresent(step -> stepConsumer.accept(step, buildStepPayload(request, list)));
		});
	}

	private Optional<DefinitionChangeSetStep> getStep(Path path) {
		for (DefinitionChangeSetStep step : steps) {
			if (step.canHandle(path)) {
				return Optional.of(step);
			}
		}
		return Optional.empty();
	}

	private static DefinitionChangeSetPayload buildStepPayload(DefinitionDeploymentRequest request,
			List<ModelChangeSetExtension> changes) {
		return new DefinitionChangeSetPayload(request.getModels(), request.getDefinition(),
				request.getGenericDefinition(), request.getContext(), changes);
	}

}
