package com.sirma.itt.sep.instance.unique.persistence;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link UniqueValueEntity}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class UniqueValueEntityTest {

    private static final String DEFINITION_ID_ONE = "definitionIdOne";
    private static final String DEFINITION_ID_TWO = "definitionIdTwo";
    private static final String FIELD_URI_ONE = "fieldUriOne";
    private static final String FIELD_URI_TWO = "fieldUriTwo";


    private static final String INSTANCE_ONE_ID = "emf:001";
    private static final String INSTANCE_TWO_ID = "emf:002";

    private static final String VALUE_ONE = "value one";
    private static final String VALUE_TWO = "value two";

    @Test
    @UseDataProvider("notEqualsDP")
    public void should_NotBeEquals_When(UniqueValueEntity entity, Object secondEntity, String errorMessage) {
        Assert.assertFalse(errorMessage, entity.equals(secondEntity));
    }

    @DataProvider
    public static Object[][] notEqualsDP() {
        UniqueValueEntity entity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        return new Object[][] {
                {entity, createEntity(DEFINITION_ID_TWO, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE), "Equal different unique field failed!"},
                {entity, createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_TWO_ID, VALUE_ONE), "Equal different instanceId failed!"},
                {entity, createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_TWO), "Equal different value failed!"},
                {entity, createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_TWO_ID, VALUE_TWO), "Equal different instance and value failed!"},
                {entity, new Object(), "Equal different type objects"}
        };
    }

    @Test
    @UseDataProvider("equalsDP")
    public void should_BeEquals_When(UniqueValueEntity entity, UniqueValueEntity secondEntity, String errorMessage) {
        Assert.assertTrue(errorMessage, entity.equals(secondEntity));
    }

    @DataProvider
    public static Object[][] equalsDP() {
        UniqueValueEntity entity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        UniqueValueEntity secondEntity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        return new Object[][] {
                {entity, entity, "Equal same object"},
                {entity, secondEntity, "Equal different objects failed!"}
        };
    }

    @Test
    @UseDataProvider("hasCodeEqualsDP")
    public void should_HashCodeBeEqual_When(UniqueValueEntity entity, UniqueValueEntity secondEntity, String errorMessage) {
        Assert.assertTrue(errorMessage, entity.hashCode() == secondEntity.hashCode());
    }

    @DataProvider
    public static Object[][] hasCodeEqualsDP() {
        UniqueValueEntity entity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        return new Object[][] {
                {entity, entity, "Hash code with same object  failed!"}
        };
    }

    @Test
    @UseDataProvider("hasCodeNotEqualsDP")
    public void should_HashCodeBeDifferent_When(UniqueValueEntity entity, UniqueValueEntity secondEntity, String errorMessage) {
        Assert.assertFalse(errorMessage, entity.hashCode() == secondEntity.hashCode());
    }

    @DataProvider
    public static Object[][] hasCodeNotEqualsDP() {
        //Tests different definitionId
        UniqueValueEntity entityTestOne = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        UniqueValueEntity secondEntityTestOne = createEntity(DEFINITION_ID_TWO, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        String testOneErrorMessage = "Hash code with different definitionId  failed!";

        //Tests different fieldUri
        UniqueValueEntity entityTestTwo = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        UniqueValueEntity secondEntityTestTwo = createEntity(DEFINITION_ID_ONE, FIELD_URI_TWO, INSTANCE_ONE_ID, VALUE_ONE);
        String testTwoErrorMessage = "Hash code with different fieldUri  failed!";

        //Tests different value
        UniqueValueEntity entityTestTree = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        UniqueValueEntity secondEntityTestTree = createEntity(DEFINITION_ID_ONE, FIELD_URI_TWO, INSTANCE_ONE_ID, VALUE_TWO);
        String testTreeErrorMessage = "Hash code with different value  failed!";

        //Tests different instanceId
        UniqueValueEntity entityTestFour = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_ONE_ID, VALUE_ONE);
        UniqueValueEntity secondEntityTestFour = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE, INSTANCE_TWO_ID, VALUE_TWO);
        String testFourErrorMessage = "Hash code with different instanceId  failed!";

        return new Object[][] {
                {entityTestOne, secondEntityTestOne, testOneErrorMessage},
                {entityTestTwo, secondEntityTestTwo, testTwoErrorMessage},
                {entityTestTree, secondEntityTestTree, testTreeErrorMessage},
                {entityTestFour, secondEntityTestFour, testFourErrorMessage}
        };
    }

    private static UniqueValueEntity createEntity(String definitionId, String fieldUri, String instanceId, String value) {
        UniqueValueEntity entity = new UniqueValueEntity();
        entity.setValue(value);
        entity.setInstanceId(instanceId);
        UniqueFieldEntity fieldEntity = new UniqueFieldEntity();
        fieldEntity.setDefinitionId(definitionId);
        fieldEntity.setFieldUri(fieldUri);
        entity.setUniqueField(fieldEntity);
        return entity;
    }
}