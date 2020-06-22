package com.sirma.itt.seip.instance.script;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.expressions.ExpressionsManager;
import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.GlobalBindingsExtension;

/**
 * Script provider for evaluating emf expression via server side JS.
 *
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = GlobalBindingsExtension.TARGET_NAME, order = 2.01)
public class ScriptExpressionProvider implements GlobalBindingsExtension {

	public static final String EVAL_SCRIPT = "expressions.js";
	@Inject
	private ExpressionsManager expressionsManager;

	@Override
	public Map<String, Object> getBindings() {
		return Collections.<String, Object> singletonMap("_eval_", this);
	}

	@Override
	public Collection<String> getScripts() {
		return ResourceLoadUtil.loadResources(getClass(), EVAL_SCRIPT);
	}

	/**
	 * Evaluate the given expression
	 *
	 * @param expression
	 *            the expression
	 * @return the serializable
	 */
	public Serializable eval(String expression) {
		if (StringUtils.isBlank(expression)) {
			return expression;
		}
		if (!expressionsManager.isExpression(expression)) {
			return expression;
		}
		return expressionsManager.evaluateRule(expression, Serializable.class, new ExpressionContext());
	}

	/**
	 * Evaluate over node.
	 *
	 * @param expression
	 *            the expression
	 * @param node
	 *            the node
	 * @return the serializable
	 */
	public Serializable evalOverNode(String expression, ScriptNode node) {
		if (StringUtils.isBlank(expression) || node == null) {
			return expression;
		}
		if (!expressionsManager.isExpression(expression)) {
			return expression;
		}
		return expressionsManager.evaluateRule(expression, Serializable.class,
				expressionsManager.createDefaultContext(node.getTarget(), null, null));
	}

}
