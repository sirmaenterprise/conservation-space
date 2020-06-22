package com.sirma.itt.seip.permissions.action;

import static com.sirma.itt.seip.collections.CollectionUtils.createHashMap;
import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.lang.invoke.MethodHandles;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.definition.util.PathHelper;
import com.sirma.itt.seip.domain.PathElement;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.GenericDefinition;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.permissions.role.RoleActionEvaluatorContext;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.script.ScriptEvaluator;
import com.sirma.itt.seip.script.ScriptException;
import com.sirma.itt.seip.script.ScriptInstance;

/**
 * Role filter provider that allow added filters via script definitions.
 * <p>
 * The filter must be defined in a generic definition of type script in the field's value and must have a control with
 * id {@code action_filter}. The script must return boolean value when evaluated. <br>
 * Example:
 *
 * <pre>
 * <code>
 * &lt;field name=&quot;isOwner&quot; type=&quot;an..200&quot;&gt;
 *    &lt;value&gt;users.isCurrentUser(root.get('owner'))&lt;/value&gt;
 *    &lt;control id=&quot;ACTION_FILTER&quot;&gt;&lt;/control&gt;
 * &lt;/field&gt;
 * </code>
 * </pre>
 *
 * @author BBonev
 */
@Extension(target = RoleActionFilterProvider.TARGET_NAME, order = 10)
public class ScriptRoleActionFilterProvider implements RoleActionFilterProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private static final String BINDINGS = "bindings";

	@Inject
	private DefinitionService definitionService;
	@Inject
	private ScriptEvaluator scriptEvaluator;
	@Inject
	private TypeConverter typeConverter;

	@Override
	public Map<String, Predicate<RoleActionEvaluatorContext>> provideFilters() {
		return definitionService
				.getAllDefinitions(GenericDefinition.class)
					.stream()
					.filter(model -> nullSafeEquals("script", model.getType(), true))
					.flatMap(DefinitionModel::fieldsStream)
					.filter(PropertyDefinition.hasValue())
					.filter(PropertyDefinition.hasControl("action_filter"))
					.distinct()
					.collect(Collectors.toMap(PropertyDefinition::getIdentifier, this::compileScript));
	}

	private Predicate<RoleActionEvaluatorContext> compileScript(PropertyDefinition property) {
		String path = PathHelper.getPath(property) + PathElement.PATH_SEPARATOR + property.getIdentifier();
		LOGGER.trace("Building scripted action filter from field: {}", path);

		// do not reference the current class in the returned predicate
		TypeConverter converter = typeConverter;

		// compile script in advance because is much faster than normal execution
		Predicate<Map<String, Object>> compiledScript = compile(property);

		return context -> {
			Map<String, Object> bindings = context.getIfSameType(BINDINGS, Map.class);
			if (bindings == null) {
				bindings = createHashMap(3);
				bindings.put("root", converter.convert(ScriptInstance.class, context.getTarget()));
				bindings.put("authority", converter.convert(ScriptInstance.class, context.getAuthority()));
				bindings.put("role", context.getCalculatedRole().getRoleId().getIdentifier());
				// store the bindings for the current context execution so not to create them multiple times
				context.put(BINDINGS, bindings);
			}
			return executeScript(context, bindings, compiledScript, path);
		};
	}

	private Predicate<Map<String, Object>> compile(PropertyDefinition property) {
		try {
			return scriptEvaluator.createScriptedPredicate(property.getDefaultValue());
		} catch (ScriptException e) {
			LOGGER.warn("Could not register script filter {}. The script is:\n{}\nThe problem is: ",
					PathHelper.getPath(property), property.getDefaultValue(), e);
			// no need to break something because of invalid script
			return context -> false;
		}
	}

	private static boolean executeScript(RoleActionEvaluatorContext context, Map<String, Object> bindings,
			Predicate<Map<String, Object>> predicate, String path) {
		// script evaluation is relatively expensive operation
		// takes 10-20+ milliseconds but may be called multiple times during single instance evaluation
		// script filters are stateless and are bound to the context so we can actually cache the result for a
		// single context so multiple actions can reuse the filter results for a single instance.
		if (context.containsKey(path)) {
			Object value = context.get(path);
			if (value instanceof Boolean) {
				return ((Boolean) value).booleanValue();
			}
		}

		try {
			boolean scriptResult = predicate.test(bindings);
			context.put(path, Boolean.valueOf(scriptResult));
			return scriptResult;
		} catch (ScriptException e) {
			LOGGER.warn("Could not evaluate script filter {} : ", path, e);
			// need to break UI when there is invalid script
			return false;
		}
	}
}
