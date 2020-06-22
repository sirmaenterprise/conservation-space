package com.sirmaenterprise.sep.properties.value;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Hold processed data needed for expression template response.
 *
 * @author Boyan Tonchev.
 */
public class PropertiesValuesEvaluatorResponse {

    private List<Map<String, Object>> instancesData = new LinkedList<>();

    /**
     * Added processed instance data.
     *
     * @param instanceData
     *         - instance data.
     */
    public void addInstanceData(Map<String, Object> instanceData) {
        instancesData.add(instanceData);
    }

    /**
     * Fetch processed instances data.
     *
     * @return instances data.
     */
    public List<Map<String, Object>> getInstancesData() {
        return instancesData;
    }
}
