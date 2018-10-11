package com.sirma.itt.seip.rule.matchers;

import javax.inject.Named;

import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.domain.instance.Instance;

/**
 * Rule that always returns true. Can be used when not matching is needed and should always run the operations
 *
 * @author BBonev
 */
@Named(MatchAll.MATCH_ALL_NAME)
public class MatchAll extends BaseRuleMatcher {
	
	public static final String MATCH_ALL_NAME = "matchAll";

	@Override
	public boolean match(Context<String, Object> processingContext, Instance instanceToMatch,
			Context<String, Object> context) {
		return true;
	}

	@Override
	public String getPrimaryOperation() {
		return MATCH_ALL_NAME;
	}

	@Override
	public String getName() {
		return MATCH_ALL_NAME;
	}
}
