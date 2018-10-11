package com.sirma.itt.seip.expressions;

import static com.sirma.itt.seip.util.EqualsHelper.equalsTo;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import com.sirma.itt.seip.collections.ComputationChain;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.util.InstanceUtil;
import com.sirma.itt.seip.expressions.ExpressionContext;
import com.sirma.itt.seip.instance.relation.LinkReference;
import com.sirma.itt.seip.instance.relation.LinkService;

/**
 * Expression evaluator that fetches instances relative to the current instance.
 *
 * @author BBonev
 */
@Singleton
public class FromEvaluator extends BaseEvaluator {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static final long serialVersionUID = 6124103474961197919L;
	private static final String SUPPORTED_PATTERN = "current|context|rootContext|owningInstance|parent|link\\\\((?<link>[\\\\w:]+?)\\\\)";
	private static final Pattern PATTERN = Pattern
			.compile(EXPRESSION_START + "\\{(?:\\.?from\\((?<" + FROM_GROUP + ">" + SUPPORTED_PATTERN + ")\\))?\\}");

	@Inject
	private LinkService linkService;

	private ComputationChain<String, BiFunction<Instance, String, Serializable>> chain;

	private final Lock lock = new ReentrantLock();

	@Override
	protected Pattern getPattern() {
		return PATTERN;
	}

	@Override
	public String getExpressionId() {
		return "from";
	}

	@Override
	protected Serializable evaluateInternal(Matcher matcher, ExpressionContext context, Serializable... values) {
		String from = matcher.group(FROM_GROUP);
		String link = getAdditionalArg(matcher);

		Instance current = null;
		Serializable serializable = getCurrentInstance(context, values);
		if (serializable instanceof Instance) {
			current = (Instance) serializable;
		}
		if (current == null) {
			return null;
		}
		ComputationChain<String, BiFunction<Instance, String, Serializable>> computationChain = getComputationChain();
		BiFunction<Instance, String, Serializable> biFunction = computationChain.execute(from);
		return biFunction.apply(current, link);
	}

	/**
	 * Gets the additional argument from the matcher it will be passed as a second argument to the function from the
	 * computation chain.
	 *
	 * @param matcher
	 *            the matcher
	 * @return the additional arg
	 */
	@SuppressWarnings("static-method")
	protected String getAdditionalArg(Matcher matcher) {
		return matcher.group("link");
	}

	/**
	 * Gets the computation chain.
	 *
	 * @return the computation chain
	 */
	protected ComputationChain<String, BiFunction<Instance, String, Serializable>> getComputationChain() {
		if (chain == null) {
			lock.lock();
			try {
				if (chain == null) {
					ComputationChain<String, BiFunction<Instance, String, Serializable>> aChain = new ComputationChain<>();
					buildComputationChain(aChain);
					// the default value will prevent a NPE then executing the chain
					aChain.addDefault((ints, link) -> null);
					// when the chain is initialize assign it
					chain = aChain;
				}
			} finally {
				lock.unlock();
			}
		}
		return chain;
	}

	/**
	 * The method should populate the given {@link ComputationChain} with operation steps. This method is called when
	 * initialization should be done.
	 *
	 * @param aChain
	 *            the a chain to build
	 */
	protected void buildComputationChain(ComputationChain<String, BiFunction<Instance, String, Serializable>> aChain) {
		aChain.addStep(equalsTo("current"), (inst, link) -> inst);
		aChain.addStep(equalsTo("context"), (inst, link) -> InstanceUtil.getDirectParent(inst));
		aChain.addStep(equalsTo("rootContext"), (inst, link) -> InstanceUtil.getRootInstance(inst));
		aChain.addStep(equalsTo("owningInstance"), (inst, link) -> InstanceUtil.getDirectParent(inst));
		aChain.addStep(equalsTo("parent"), (inst, link) -> InstanceUtil.getDirectParent(inst));
		aChain.addStep(key -> key.startsWith("link"), (inst, link) -> getFromLink(inst, link));
	}

	private Serializable getFromLink(Instance current, String linkId) {
		if (StringUtils.isBlank(linkId)) {
			LOGGER.warn("Invalid link expression! No link is defined");
			return null;
		}
		List<LinkReference> link = linkService.getLinks(current.toReference(), linkId);
		if (!link.isEmpty()) {
			return linkService.convertToLinkInstance(link.get(0));
		}
		// no such link
		return null;
	}
}
