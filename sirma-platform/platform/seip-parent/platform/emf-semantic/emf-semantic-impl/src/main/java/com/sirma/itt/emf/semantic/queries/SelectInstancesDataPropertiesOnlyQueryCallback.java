package com.sirma.itt.emf.semantic.queries;

import com.sirma.itt.seip.io.ResourceLoadUtil;
import com.sirma.itt.seip.plugin.Extension;
import com.sirma.itt.seip.search.NamedQueries;
import com.sirma.itt.semantic.queries.QueryBuilderCallback;

/**
 * Query builder extension for semantic query that allows loading one or more instances with populated data properties only.
 *
 * @author Boyan Tonchev.
 */
@Extension(target = QueryBuilderCallback.PLUGIN_NAME, order = 15)
public class SelectInstancesDataPropertiesOnlyQueryCallback extends AbstractSelectInstanceQueryCallback {
    /**
     * <pre>
     * SELECT DISTINCT ?uri ?propertyName ?propertyValue
     * WHERE {
     * {
     *     ?uri ?propertyName ?propertyValue .
     *     ?uri  emf:isDeleted "false"^^xsd:boolean .
     *     FILTER EXISTS {
     *         ?propertyName a emf:DefinitionDataProperty.
     *     }
     * } FILTER(
     * </pre>
     */

    private static final String SELECT_MULTIPLE_START = ResourceLoadUtil.loadResource(SelectInstancesQueryCallback.class,
                                                                              NamedQueries.SELECT_DATA_PROPERTIES_BY_IDS
                                                                                      + ".sparql");

    @Override
    public String getName() {
        return NamedQueries.SELECT_DATA_PROPERTIES_BY_IDS;
    }

    public String getMultipleStart() {
        return SELECT_MULTIPLE_START;
    }
}
