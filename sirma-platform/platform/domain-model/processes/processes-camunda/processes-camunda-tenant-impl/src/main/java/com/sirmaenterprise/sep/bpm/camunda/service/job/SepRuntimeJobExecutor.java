package com.sirmaenterprise.sep.bpm.camunda.service.job;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.impl.jobexecutor.RuntimeContainerJobExecutor;

import com.sirmaenterprise.sep.bpm.camunda.service.SecureProcessEngine;
import com.sirmaenterprise.sep.bpm.camunda.tenant.service.SepRuntimeCamundaContainerDelegate;

/**
 * Container delegate for runtime execution of jobs. Based on {@link RuntimeContainerJobExecutor} with
 * {@link SepRuntimeCamundaContainerDelegate} as delegate
 * 
 * @author bbanchev
 */
@Singleton
public class SepRuntimeJobExecutor extends RuntimeContainerJobExecutor {

	@Inject
	private SepRuntimeCamundaContainerDelegate runtimeCamundaContainerDelegate;

	@SecureProcessEngine
	@Override
	protected RuntimeContainerDelegate getRuntimeContainerDelegate() {
		return runtimeCamundaContainerDelegate;
	}
}
