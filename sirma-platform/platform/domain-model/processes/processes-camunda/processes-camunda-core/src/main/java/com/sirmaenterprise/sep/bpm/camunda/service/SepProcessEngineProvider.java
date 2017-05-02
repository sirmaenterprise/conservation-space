/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.sirmaenterprise.sep.bpm.camunda.service;

import java.util.Set;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.camunda.bpm.engine.rest.spi.ProcessEngineProvider;

import com.sirma.itt.seip.security.context.SecurityContext;
import com.sirmaenterprise.sep.bpm.camunda.configuration.CamundaConfiguration;

/**
 * Uses the {@link ProcessEngines} to provide the correct instance of {@link ProcessEngine}. No default engine is
 * returned since engine retrieval should be explicit parameters.
 *
 * @author bbanchev
 */
public class SepProcessEngineProvider implements ProcessEngineProvider {

	@Override
	public ProcessEngine getDefaultProcessEngine() {
		String tenantId = SecurityContext.getDefaultTenantId();
		if (tenantId == null) {
			return null;
		}
		return ProcessEngines.getProcessEngine(CamundaConfiguration.getEngineName(tenantId));
	}

	@Override
	public ProcessEngine getProcessEngine(String name) {
		return ProcessEngines.getProcessEngine(name);
	}

	@Override
	public Set<String> getProcessEngineNames() {
		return ProcessEngines.getProcessEngines().keySet();
	}

}
