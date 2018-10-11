package com.sirma.itt.seip.script;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.collections.CollectionUtils;
import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.domain.instance.InstanceReference;
import com.sirma.itt.seip.tasks.SchedulerActionAdapter;
import com.sirma.itt.seip.tasks.SchedulerContext;
import com.sirma.itt.seip.time.TimeTracker;

/**
 * Executor action that can run scripts
 *
 * @author BBonev
 */
@Named(ScriptSchedulerExecutor.NAME)
public class ScriptSchedulerExecutor extends SchedulerActionAdapter {
	public static final String NAME = "scriptSchedulerExecutor";

	private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static final String PARAM_SCRIPT = "script";
	public static final String PARAM_BINDINGS = "bindings";
	public static final String PARAM_LANGUAGE = "language";

	private static final List<Pair<String, Class<?>>> PARAMS = Collections.singletonList(new Pair<>(PARAM_SCRIPT, String.class));

	@Inject
	private ScriptEvaluator scriptEvaluator;

	@Inject
	private TypeConverter typeConverter;

	@Override
	protected List<Pair<String, Class<?>>> validateInput() {
		return PARAMS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void execute(SchedulerContext context) throws Exception {
		String script = context.getIfSameType(PARAM_SCRIPT, String.class);
		String language = context.getIfSameType(PARAM_LANGUAGE, String.class, ScriptEvaluator.DEFAULT_LANGUAGE);
		Map<String, Object> bindings = context.getIfSameType(PARAM_BINDINGS, Map.class);
		if (bindings != null) {
			bindings = materializeInstanceReferences(bindings);
		}
		TimeTracker timeTracker = TimeTracker.createAndStart();
		try {
			scriptEvaluator.eval(language, script, bindings);
		} finally {
			LOGGER.debug("Script execution took {} s", timeTracker.stopInSeconds());
		}
	}

	/**
	 * Materializes instance references based on the provided map with bindings.
	 *
	 * @param bindings
	 *            - the provided bindings map
	 * @return - new map with the same set of keys but materializes references for values
	 */
	private Map<String, Object> materializeInstanceReferences(Map<String, Object> bindings) {
		Map<String, Object> copy = CollectionUtils.createHashMap(bindings.size());
		for (Entry<String, Object> entry : bindings.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof InstanceReference) {
				value = typeConverter.convert(ScriptInstance.class, ((InstanceReference) value).toInstance());
			}
			copy.put(entry.getKey(), value);
		}
		return copy;
	}
}
