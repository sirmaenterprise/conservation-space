package com.sirmaenterprise.sep.bpm.camunda.configuration;

import javax.inject.Singleton;

import org.camunda.bpm.engine.cdi.CdiExpressionManager;

/**
 * Add CDI support for {@link CdiExpressionManager}
 * 
 * @author Hristo Lungov
 */
@Singleton
public class SepCdiExpressionManager extends CdiExpressionManager {

	// override default cdi expression manager so we have access through cdi.

}
