/**
 *
 */
package com.sirma.itt.seip.expressions;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Expression evaluator that can invoke a JavaScript code.The code should be wrapped in the expression id so not to be
 * interpreted <br>
 * <code>${script($$2+2$$)}<code> will result in 4<br>
 * <code>#{script(##2+2##)}<code> This is the same as above but using the lazy expression identifier
 *
 * @author BBonev
 */
@Singleton
public class ScriptExpressionEvaluator extends BaseEvaluator {

	private static final long serialVersionUID = -5380921969869905169L;
	private static final Pattern SCRIPT = Pattern.compile(
			EXPRESSION_START + "\\{script\\(" + EXPRESSION_START + "{2}(.+)" + EXPRESSION_START + "{2}\\)\\}",
			Pattern.DOTALL);

	@Inject
	private ScriptEvaluator scriptEvaluator;

	@Override
	protected Pattern getPattern() {
		return SCRIPT;
	}

	@Override
	public String getExpressionId() {
		return "script";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String script = matcher.group(1);
		Map<String, Object> binding = CollectionUtils.createHashMap(2);
		Serializable currentInstance = getCurrentInstance(context, values);
		ScriptInstance rootNode = converter.convert(ScriptInstance.class, currentInstance);
		CollectionUtils.addNonNullValue(binding, "root", rootNode);
		Object eval = scriptEvaluator.eval(script, binding);
		if (eval instanceof Serializable) {
			return (Serializable) eval;
		}
		return null;
	}

}
