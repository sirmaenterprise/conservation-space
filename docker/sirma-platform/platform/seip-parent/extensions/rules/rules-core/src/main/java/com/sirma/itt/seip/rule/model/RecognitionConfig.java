package com.sirma.itt.seip.rule.model;

import java.util.Collection;
import java.util.LinkedList;

import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.emf.rule.RuleOperation;
import com.sirma.itt.emf.rule.RulePrecondition;
import com.sirma.itt.seip.domain.util.DependencyResolver;

/**
 * Represents a entity recognition configuration.
 *
 * @author BBonev
 */
public class RecognitionConfig {

	private Collection<RulePrecondition> fastPreconditions = new LinkedList<>();
	private Collection<RulePrecondition> slowPreconditions = new LinkedList<>();
	private Collection<DependencyResolver> dataProviders;
	private Collection<RuleMatcher> matchers;
	private Collection<RuleOperation> operations;

	private ParallelismMode parallelism = ParallelismMode.NONE;

	/**
	 * Should execute data providers in parallel.
	 *
	 * @return true, if successful
	 */
	public boolean shouldExecuteDataProvidersInParallel() {
		return getDataProviders().size() > 1 && getParallelism() == ParallelismMode.DATA_PROVIDER;
	}

	/**
	 * Getter method for dataProviders.
	 *
	 * @return the dataProviders
	 */
	public Collection<DependencyResolver> getDataProviders() {
		return dataProviders;
	}

	/**
	 * Setter method for dataProviders.
	 *
	 * @param dataProviders
	 *            the dataProviders to set
	 */
	public void setDataProviders(Collection<DependencyResolver> dataProviders) {
		this.dataProviders = dataProviders;
	}

	/**
	 * Getter method for matchers.
	 *
	 * @return the matchers
	 */
	public Collection<RuleMatcher> getMatchers() {
		return matchers;
	}

	/**
	 * Setter method for matchers.
	 *
	 * @param matchers
	 *            the matchers to set
	 */
	public void setMatchers(Collection<RuleMatcher> matchers) {
		this.matchers = matchers;
	}

	/**
	 * Getter method for operations.
	 *
	 * @return the operations
	 */
	public Collection<RuleOperation> getOperations() {
		return operations;
	}

	/**
	 * Setter method for operations.
	 *
	 * @param operations
	 *            the operations to set
	 */
	public void setOperations(Collection<RuleOperation> operations) {
		this.operations = operations;
	}

	/**
	 * Getter method for parallelism.
	 *
	 * @return the parallelism
	 */
	public ParallelismMode getParallelism() {
		return parallelism;
	}

	/**
	 * Setter method for parallelism.
	 *
	 * @param parallelism
	 *            the parallelism to set
	 */
	public void setParallelism(ParallelismMode parallelism) {
		this.parallelism = parallelism;
	}

	/**
	 * Sets the preconditions.
	 *
	 * @param preconditions
	 *            the new preconditions
	 */
	public void setPreconditions(Collection<RulePrecondition> preconditions) {
		fastPreconditions = new LinkedList<>();
		slowPreconditions = new LinkedList<>();

		if (preconditions == null) {
			return;
		}

		for (RulePrecondition precondition : preconditions) {
			if (precondition.isAsyncSupported()) {
				slowPreconditions.add(precondition);
			} else {
				fastPreconditions.add(precondition);
			}
		}
	}

	/**
	 * Getter method for fastPreconditions.
	 *
	 * @return the fastPreconditions
	 */
	public Collection<RulePrecondition> getFastPreconditions() {
		return fastPreconditions;
	}

	/**
	 * Getter method for slowPreconditions.
	 *
	 * @return the slowPreconditions
	 */
	public Collection<RulePrecondition> getSlowPreconditions() {
		return slowPreconditions;
	}

}