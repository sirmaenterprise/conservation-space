package com.sirma.itt.seip.instance.dao;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.VirtualDbQueryParser;
import com.sirma.itt.seip.model.InstanceEntity;
import com.sirma.itt.seip.plugin.Extension;

/**
 * Virtual db query parser that handles queries about {@link InstanceEntity} when loading instances from relational db.
 *
 * @author BBonev
 */
@Extension(target = VirtualDbQueryParser.PLUGIN_NAME, order = 20)
public class InstanceEntityVirtualDbQueryParser implements VirtualDbQueryParser {

	@Override
	public <R, E extends Pair<String, Object>> Optional<Collection<R>> parse(String query, List<E> params) {
		return Optional.empty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> Optional<Collection<R>> parseNamed(String namedQuery, List<E> params) {
		if (nullSafeEquals(namedQuery, InstanceEntity.QUERY_INSTANCE_ENTITIES_BY_ID_KEY)) {
			return params.stream().filter(pair -> nullSafeEquals(pair.getFirst(), "id")).findAny().map(
					pair -> (Collection<R>) pair.getSecond());
		}
		return Optional.empty();
	}

}
