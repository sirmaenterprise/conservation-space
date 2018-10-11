package com.sirmaenterprise.sep.properties.value;

import com.sirma.itt.seip.rest.utils.JsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link PropertiesValuesEvaluatorResponseBodyWriter} class.
 * @author Boyan Tonchev.
 */
public class PropertiesValuesEvaluatorResponseBodyWriterTest {

    private PropertiesValuesEvaluatorResponseBodyWriter writer = new PropertiesValuesEvaluatorResponseBodyWriter();

    @Test
    public void should_CreateExpressionTemplateResponse_When_MethodsCalled() throws Exception {
        String instanceId = "instanceId";
        String propertyName = "identifier";
        String propertyValue = "value of property identifier";
        PropertiesValuesEvaluatorResponse expressionTemplateResponse = getExpressionTemplateResponse(instanceId, propertyName,
                                                                                                     propertyValue);
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {

            writer.writeTo(expressionTemplateResponse, null, null, null, null, null, stream);

            JSONObject result = new JSONObject(new String(stream.toByteArray()));
            assertResult(instanceId, propertyName, propertyValue, result);
        }
    }

    private void assertResult(String instanceId, String propertyName, String propertyValue, JSONObject result)
            throws JSONException {
        JSONArray data = result.getJSONArray("data");
        Assert.assertTrue(data.length() == 1);
        JSONObject jsonObject = data.getJSONObject(0);
        Assert.assertEquals(jsonObject.getString(JsonKeys.ID), instanceId);
        JSONArray properties = jsonObject.getJSONArray(JsonKeys.PROPERTIES);
        Assert.assertTrue(properties.length() == 1);
        JSONObject property = properties.getJSONObject(0);
        Assert.assertEquals(property.getString(JsonKeys.PROPERTY_NAME), propertyName);
        Assert.assertEquals(property.getString(JsonKeys.PROPERTY_VALUE), propertyValue);
    }

    private PropertiesValuesEvaluatorResponse getExpressionTemplateResponse(String instanceId, String propertyName,
            String propertyValue) {
        Map<String, Serializable> instanceProperty = new HashMap<>(1);
        instanceProperty.put(JsonKeys.PROPERTY_NAME, propertyName);
        instanceProperty.put(JsonKeys.PROPERTY_VALUE, propertyValue);
        List<Map<String, Serializable>> instanceProperties = new LinkedList<>();
        instanceProperties.add(instanceProperty);

        Map<String, Object> instanceData = new HashMap<>();
        instanceData.put(JsonKeys.ID, instanceId);
        instanceData.put(JsonKeys.PROPERTIES, instanceProperties);

        PropertiesValuesEvaluatorResponse expressionTemplateResponse = new PropertiesValuesEvaluatorResponse();
        expressionTemplateResponse.addInstanceData(instanceData);
        return expressionTemplateResponse;
    }

    @Test
    public void should_NotBeWritable_When_TypeIsNotExpressionTemplateResponse() {
        Assert.assertFalse(writer.isWriteable(PropertiesValuesEvaluatorResponseBodyWriterTest.class, null, null, null));
    }

    @Test
    public void should_BeWritable_When_TypeIsExpressionTemplateResponse() {
        Assert.assertTrue(writer.isWriteable(PropertiesValuesEvaluatorResponse.class, null, null, null));
    }
}