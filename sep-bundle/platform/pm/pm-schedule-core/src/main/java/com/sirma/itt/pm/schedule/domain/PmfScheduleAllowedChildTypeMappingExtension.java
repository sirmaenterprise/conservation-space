package com.sirma.itt.pm.schedule.domain;

import java.util.Collections;
import java.util.Map;

import com.sirma.itt.emf.definition.dao.AllowedChildTypeMappingExtension;
import com.sirma.itt.emf.domain.model.DefinitionModel;
import com.sirma.itt.emf.instance.model.Instance;
import com.sirma.itt.emf.plugin.Extension;
import com.sirma.itt.emf.util.CollectionUtils;
import com.sirma.itt.pm.schedule.model.ScheduleEntry;
import com.sirma.itt.pm.schedule.model.ScheduleInstance;

/**
 * Default type mappings for PMF classes as schedule.
 * 
 * @author BBonev
 */
@Extension(target = AllowedChildTypeMappingExtension.TARGET_NAME, order = 100)
public class PmfScheduleAllowedChildTypeMappingExtension implements AllowedChildTypeMappingExtension {

	/** The Constant instanceMapping. */
	private static final Map<String, Class<? extends Instance>> instanceMapping;

	static {
		instanceMapping = CollectionUtils.createHashMap(3);
		instanceMapping.put(ObjectTypesPms.SCHEDULE, ScheduleInstance.class);
		instanceMapping.put(ObjectTypesPms.SCHEDULE_ENTRY, ScheduleEntry.class);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends DefinitionModel>> getDefinitionMapping() {
		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Class<? extends Instance>> getInstanceMapping() {
		return instanceMapping;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, String> getTypeMapping() {
		return Collections.emptyMap();
	}

}
