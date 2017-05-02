package com.sirma.itt.emf.rule;

import com.sirma.itt.seip.json.JsonRepresentable;

/**
 * Interface to identify a rule state. The implementation should provide the {@link #toJSONObject()} method implemented
 * to represent the state. The implementation is free to decide how to store the state of the rule and what to store.
 *
 * @author BBonev
 */
public interface RuleState extends JsonRepresentable {
	// nothing more to add for now
}
