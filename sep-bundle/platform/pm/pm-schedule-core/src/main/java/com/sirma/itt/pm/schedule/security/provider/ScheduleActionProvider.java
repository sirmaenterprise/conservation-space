package com.sirma.itt.pm.schedule.security.provider;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.sirma.itt.emf.domain.Pair;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.security.ActionProvider;
import com.sirma.itt.emf.security.model.Action;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.security.ScheduleActions;

/**
 * Action provider for schedule entry actions.
 * 
 * @author BBonev
 */
@ApplicationScoped
@Extension(target = ActionProvider.TARGET_NAME, order = 120)
public class ScheduleActionProvider implements ActionProvider {

	/** The data. */
	private Map<Pair<Class<?>, String>, Action> data;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Pair<Class<?>, String>, Action> provide() {
		if (data == null) {
			data = CollectionUtils.createLinkedHashMap(ScheduleActions.values().length);

			for (ScheduleActions action : ScheduleActions.values()) {
				data.put(new Pair<Class<?>, String>(ScheduleEntry.class, action.getActionId()),
						action);
			}
		}
		return data;
	}

}
