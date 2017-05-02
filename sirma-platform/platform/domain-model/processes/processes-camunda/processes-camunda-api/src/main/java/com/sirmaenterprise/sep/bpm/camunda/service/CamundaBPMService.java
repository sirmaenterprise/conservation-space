package com.sirmaenterprise.sep.bpm.camunda.service;

import static com.sirmaenterprise.sep.bpm.model.ProcessConstants.ACTIVITY_ID;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.camunda.bpm.engine.form.TaskFormData;
import org.camunda.bpm.engine.task.Task;

import com.sirma.itt.seip.domain.instance.Instance;
import com.sirmaenterprise.sep.bpm.exception.BPMRuntimeException;

/**
 * Generic Camunda related service, that handles logic for BPMN, CMMN, DMN modules. All common logic should is placed in
 * this service.
 * 
 * @author bbanchev
 */
public interface CamundaBPMService {
	/**
	 * Check if instance is part of process
	 * 
	 * @param activity
	 *            the instance to check
	 * @return true if it is part of process
	 */
	static boolean isActivity(Instance activity) {
		Objects.requireNonNull(activity, "Instance is a required parameter to check if it is activity!");
		return activity.isValueNotNull(ACTIVITY_ID);
	}

	/**
	 * Check if instance is part of process
	 * 
	 * @param activity
	 *            the instance to check
	 * @return true if it is part of process
	 */
	static String getActivityId(Instance activity) {
		Objects.requireNonNull(activity, "Instance is a required parameter to get activity id from!");
		return activity.getAsString(ACTIVITY_ID);
	}

	/**
	 * Gets the Camunda task - {@link Task} that is the representation of activity in the process engine.
	 *
	 * @param activity
	 *            the activity part of some existing process.
	 * @return the {@link Task} for the given activity or or {@link Optional#empty()} if completed/not found or this not
	 *         a bpm activity
	 */
	Optional<Task> loadTask(Instance activity);

	/**
	 * Loads an activity details based on the provided activity. If activity is not a Camunda activity or it is a
	 * process {@link Optional#empty()} is returned. If Camunda activity is completed a historic version is returned.
	 * 
	 * @param activity
	 *            is the source activity to collect Camunda details
	 * @return the built {@link ActivityDetails} or {@link Optional#empty()} if no details found or this not a bpm
	 *         activity. Might generate runtime exceptions if data is invalid
	 */
	Optional<ActivityDetails> getActivityDetails(Instance activity);

	/***
	 * Retrieves a single element of collection if the collection contains only one element. If result contains more than
	 * one element if errorSupplier is provided, the supplied exception is thrown. In all other cases null is returned.
	 * 
	 * @param data
	 *            is the data source
	 * @param errorSupplier
	 *            optional error supplier
	 * @return the contained element or null of collections is empty
	 */
	static <T> T getSingleValue(Collection<T> data, Supplier<BPMRuntimeException> errorSupplier) {
		if (data.size() == 1) {
			return data.iterator().next();
		}
		if (data.size() > 1 && errorSupplier != null) {
			throw errorSupplier.get();
		}
		return null;
	}

	/**
	 * Gets from the {@link Instance} camunda task id, first loads the Camunda {@link Task} to verify it is existing and
	 * after that calls for {@link TaskFormData}.
	 * 
	 * @param activity
	 *            is the {@link Instance} to get {@link #getActivityId(Instance)} and load Camunda {@link TaskFormData}
	 * @return the {@link TaskFormData} or {@link Optional#empty()} if completed/not found or this not a bpm activity
	 */
	Optional<TaskFormData> getTaskFormData(Instance activity);

}
