package com.sirma.itt.sep.instance.unique.persistence;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit tests for {@link UniqueFieldEntity}
 *
 * @author Boyan Tonchev.
 */
@RunWith(DataProviderRunner.class)
public class UniqueFieldEntityTest {

    private static final String DEFINITION_ID_ONE = "definitionIdOne";
    private static final String DEFINITION_ID_TWO = "definitionIdTwo";
    private static final String FIELD_URI_ONE = "fieldUriOne";
    private static final String FIELD_URI_TWO = "fieldUriTwo";

    @Test
    @UseDataProvider("notEqualsDP")
    public void should_NotBeEquals_When(UniqueFieldEntity entity, Object secondEntity, String errorMessage) {
        Assert.assertFalse(errorMessage, entity.equals(secondEntity));
    }

    @DataProvider
    public static Object[][] notEqualsDP() {
        UniqueFieldEntity entity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE);
        return new Object[][] {
                {entity, createEntity(DEFINITION_ID_TWO, FIELD_URI_TWO), "Equal different differentId failed!"},
                {entity, createEntity(DEFINITION_ID_ONE, FIELD_URI_TWO), "Equal different fieldUri failed!"},
                {entity, createEntity(DEFINITION_ID_TWO, FIELD_URI_ONE), "Equal different differentId and fieldUri failed!"},
                {entity, new Object(), "Equal different type objects"}
        };
    }

    @Test
    @UseDataProvider("equalsDP")
    public void should_BeEquals_When(UniqueFieldEntity entity, UniqueFieldEntity secondEntity, String errorMessage) {
        Assert.assertTrue(errorMessage, entity.equals(secondEntity));
    }

    @DataProvider
    public static Object[][] equalsDP() {
        UniqueFieldEntity entity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE);
        UniqueFieldEntity secondEntity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE);
        return new Object[][] {
                {entity, entity, "Equal same object"},
                {entity, secondEntity, "Equal different objects with same definitionId and fieldUri failed!"}
        };
    }

    @Test
    public void should_HaveSameHashCode_When_DefinitionIdAndFieldUriAreSame() {
        UniqueFieldEntity entity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE);
        UniqueFieldEntity secondEntity = createEntity(DEFINITION_ID_ONE, FIELD_URI_ONE);
        Assert.assertTrue(entity.hashCode() == secondEntity.hashCode());
    }

    @Test
    @UseDataProvider("hasCodeNotEqualsDP")
    public void should_HashCodeBeDifferent_When(String definitionId, String fieldUri, String secondDefinitionId, String secondFieldUri, String errorMessage) {
        UniqueFieldEntity entity = createEntity(definitionId, fieldUri);
        UniqueFieldEntity secondEntity = createEntity(secondDefinitionId, secondFieldUri);
        Assert.assertFalse(errorMessage, entity.hashCode() == secondEntity.hashCode());
    }

    @DataProvider
    public static Object[][] hasCodeNotEqualsDP() {
        return new Object[][] {
                {DEFINITION_ID_ONE, FIELD_URI_ONE, DEFINITION_ID_TWO, FIELD_URI_ONE, "HashCode different definitionId failed!"},
                {DEFINITION_ID_ONE, FIELD_URI_ONE, DEFINITION_ID_ONE, FIELD_URI_TWO, "HashCode different fieldUri failed!"},
                {DEFINITION_ID_ONE, FIELD_URI_ONE, DEFINITION_ID_TWO, FIELD_URI_TWO, "HashCode different definitionId, fieldUri failed!"}
        };
    }

    private static UniqueFieldEntity createEntity(String definitionId, String fieldUri) {
        UniqueFieldEntity entity = new UniqueFieldEntity();
        entity.setDefinitionId(definitionId);
        entity.setFieldUri(fieldUri);
        return entity;
    }
}