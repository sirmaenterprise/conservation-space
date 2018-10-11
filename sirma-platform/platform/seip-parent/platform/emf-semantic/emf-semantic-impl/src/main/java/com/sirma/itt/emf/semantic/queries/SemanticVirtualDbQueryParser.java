package com.sirma.itt.emf.semantic.queries;

import static com.sirma.itt.seip.util.EqualsHelper.nullSafeEquals;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.VirtualDbQueryParser;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;

/**
 * Parser for some semantic queries
 * 
 * @author BBonev
 */
@Extension(target = VirtualDbQueryParser.PLUGIN_NAME, order = 10)
public class SemanticVirtualDbQueryParser implements VirtualDbQueryParser {

	@Override
	public <R, E extends Pair<String, Object>> Optional<Collection<R>> parse(String query, List<E> params) {
		return Optional.empty();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R, E extends Pair<String, Object>> Optional<Collection<R>> parseNamed(String namedQuery, List<E> params) {
		// // currently this only handles the batch entity load for semantic search
		if (nullSafeEquals(NamedQueries.SELECT_BY_IDS, namedQuery)) {
			return params
					.stream()
						.filter(pair -> nullSafeEquals(pair.getFirst(), NamedQueries.Params.URIS))
						.findAny()
						.map(pair -> (Collection<R>) pair.getSecond());
		}
		return Optional.empty();
	}

}
