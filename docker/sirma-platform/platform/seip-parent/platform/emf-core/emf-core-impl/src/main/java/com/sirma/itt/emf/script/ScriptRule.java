package com.sirma.itt.emf.script;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.emf.rule.BaseDynamicInstanceRule;
import com.sirma.itt.emf.rule.DynamicInstanceRule;
import com.sirma.itt.emf.rule.RuleContext;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.context.Context;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.instance.script.ScriptNode;
import com.sirma.itt.seip.script.ScriptEvaluator;

/**
 * Dynamic rule that handle running JavaScripts via the rule engine.
 *
 * @author BBonev
 */
@Named(ScriptRule.NAME)
public class ScriptRule extends BaseDynamicInstanceRule implements DynamicInstanceRule {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	public static final String NAME = "scriptRule";
	/**
	 * The script to run
	 */
	private String script;
	@Inject
	private ScriptEvaluator evaluator;
	@Inject
	private TypeConverter typeConverter;
	private String ruleName;

	@Override
	public String getPrimaryOperation() {
		return NAME;
	}

	@Override
	public boolean configure(Context<String, Object> configuration) {
		if (!super.configure(configuration)) {
			return false;
		}
		script = StringUtils.trimToEmpty(configuration.getIfSameType("script", String.class));
		if (StringUtils.isEmpty(script)) {
			LOGGER.warn("Initialized script rule without defined script! Rule is defined in {}",
					configuration.get(DEFINED_IN));
		}
		ruleName = configuration.getIfSameType(DEFINED_IN, String.class);
		return StringUtils.isNotBlank(script);
	}

	@Override
	public String getRuleInstanceName() {
		return ruleName;
	}

	@Override
	public boolean isApplicable(Context<String, Object> context) {
		return !StringUtils.isEmpty(script) && super.isApplicable(context);
	}

	@Override
	public void execute(RuleContext context) {
		ScriptNode node = typeConverter.convert(ScriptNode.class, context.getTriggerInstance());
		ScriptNode oldVersionNode = typeConverter.convert(ScriptNode.class, context.getPreviousInstanceVersion());
		Map<String, Object> binding = new HashMap<>(5);
		CollectionUtils.addNonNullValue(binding, "current", node);
		CollectionUtils.addNonNullValue(binding, "oldNode", oldVersionNode);
		CollectionUtils.addNonNullValue(binding, "operation", context.getOperation());
		// set the configuration from the definition into the binding so it could be available
		String configuration = StringUtils
				.trimToNull(getConfiguration().getIfSameType("nonParsableConfiguration", String.class));
		CollectionUtils.addNonNullValue(binding, "configuration", configuration);
		evaluator.eval(script, binding);
	}

}
