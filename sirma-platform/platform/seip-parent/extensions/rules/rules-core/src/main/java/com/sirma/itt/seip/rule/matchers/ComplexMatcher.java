package com.sirma.itt.seip.rule.matchers;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.RuleMatcher;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.rule.model.EntityRecognitionConfigBuilder;

/**
 * The Class ComplexMatcher.
 *
 * @author Hristo Lungov
 */
@Named(ComplexMatcher.COMPLEX_MATCHER_NAME)
public class ComplexMatcher extends BaseRuleMatcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComplexMatcher.class);

	public static final String COMPLEX_MATCHER_NAME = "complexMatcher";
	protected static final String MATCHERS = "matchers";

	@Inject
	private EntityRecognitionConfigBuilder entityRecognitionConfigBuilder;

	private Collection<RuleMatcher> subMatchers;

	private boolean disabled = false;

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			disabled = true;
			return false;
		}
		subMatchers = entityRecognitionConfigBuilder.buildSubElements(MATCHERS, RuleMatcher.class, configuration);
		if (CollectionUtils.isEmpty(subMatchers)) {
			LOGGER.warn(
					"ComplexMatcher will be disabled because no sub-matchers to match are found in the configuration. Configuration is {}",
					configuration);
			disabled = true;
		} else {
			disabled = false;
		}
		return !disabled;
	}

	@Override
	public boolean match(Context<String, Object> processingContext, Instance instanceToMatch,
			Context<String, Object> context) {
		for (RuleMatcher ruleMatcher : subMatchers) {
			if (!ruleMatcher.match(processingContext, instanceToMatch, context)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String getPrimaryOperation() {
		return COMPLEX_MATCHER_NAME;
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		if (disabled) {
			return false;
		}
		return super.isApplicable(context);
	}

	@Override
	public String getName() {
		return COMPLEX_MATCHER_NAME;
	}

}
