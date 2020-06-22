package com.sirma.itt.sep.instance.unique.persistence;

import com.sirma.itt.seip.Pair;
import com.sirma.itt.seip.db.DbDao;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Unit tests for {@link UniqueValueDao}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueDaoTest {

    private static final String INSTANCE_ID = "emf:0007";
    private static final String DEFINITION_ID = "DT0007";
    private static final String FIELD_URI = "emf:title";
    private static final String PROPERTY_VALUE = "value of property";

    @Mock
    private DbDao dbDao;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private UniqueValueDao uniqueValueDao;

    @Test
    public void should_BuildInsertQuery_When_MethodIsCalledForTwoRows() {
        Query query = uniqueValueDao.buildNativeInsertUniqueValuesQuery(2, 3423222L);

        ArgumentCaptor<String> queryArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(entityManager).createNativeQuery(queryArgumentCaptor.capture());
        Assert.assertEquals("INSERT INTO sep_unique_values (value, instance_id, unique_field_id)  VALUES(?1, ?2, 3423222),(?3, ?4, 3423222)", queryArgumentCaptor.getValue());
    }

    @Test
    public void should_BuildInsertQuery_When_MethodIsCalledForOneRow() {
        Query query = uniqueValueDao.buildNativeInsertUniqueValuesQuery(1, 3423222L);

        ArgumentCaptor<String> queryArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(entityManager).createNativeQuery(queryArgumentCaptor.capture());
        Assert.assertEquals("INSERT INTO sep_unique_values (value, instance_id, unique_field_id)  VALUES(?1, ?2, 3423222)", queryArgumentCaptor.getValue());
    }

    @Test
    public void should_ReturnIdOfRegisteredUniqueField() {
        Long uniqueFieldId = 2L;
        UniqueFieldEntity uniqueFieldEntity = Mockito.mock(UniqueFieldEntity.class);
        Mockito.when(uniqueFieldEntity.getId()).thenReturn(uniqueFieldId);
        Mockito.when(dbDao.fetchWithNamed(Matchers.any(), Matchers.anyList())).thenReturn(Arrays.asList(uniqueFieldEntity));

        Assert.assertEquals(uniqueFieldId, uniqueValueDao.getUniqueFieldId("", ""));
    }

    @Test
    public void should_ReturnNull_When_UniqueFieldIsNotRegistered() {
        Assert.assertNull(uniqueValueDao.getUniqueFieldId("", ""));
    }

    @Test
    public void should_DeleteEntity_When_MethodUnregisterValueIsCalled() {
        String query = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_DEFINITION_ID_AND_FIELD_URI;
        Long entityId = 1L;
        UniqueValueEntity uniqueValueEntity = Mockito.mock(UniqueValueEntity.class);
        Mockito.when(uniqueValueEntity.getId()).thenReturn(entityId);
        List<UniqueValueEntity> queryResult = new ArrayList<>(1);
        queryResult.add(uniqueValueEntity);

        Mockito.when(dbDao.fetchWithNamed(Matchers.eq(query), Matchers.anyList())).thenReturn(queryResult);

        uniqueValueDao.unregisterValue(INSTANCE_ID, DEFINITION_ID, FIELD_URI);

        Mockito.verify(dbDao).delete(Matchers.eq(UniqueValueEntity.class), Matchers.eq(entityId));
    }

    @Test
    public void should_NotCallDelete_When_UniqueFieldConstraintEntityNotFound() {
        uniqueValueDao.unregisterValue(INSTANCE_ID, DEFINITION_ID, FIELD_URI);

        Mockito.verify(dbDao, Mockito.never()).delete(Matchers.any(), Matchers.any());
    }

    @Test
    public void should_DeleteEntity_When_MethodUnregisterUniqueValuesForInstanceIsCalled() {
        String query = UniqueValueEntity.QUERY_NAME_DELETE_UNIQUE_VALUES_BY_INSTANCE_ID;

        uniqueValueDao.unregisterAllUniqueValuesForInstance(INSTANCE_ID);

        ArgumentCaptor<List<Pair<String, Object>>> queryArgCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(dbDao).executeUpdate(Matchers.eq(query), queryArgCaptor.capture());
        Map<String, Object> expectedQueryArgs = new HashMap<>();
        expectedQueryArgs.put("instanceId", INSTANCE_ID);
        assertQueryPairs(queryArgCaptor.getValue(), expectedQueryArgs);
    }

    @Test
    public void should_UpdateEntity_When_ValueIsAlreadyRegistered() {
        String queryUniqueFieldEntity = UniqueFieldEntity.QUERY_NAME_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI;
        UniqueFieldEntity uniqueFieldEntity = Mockito.mock(UniqueFieldEntity.class);
        Mockito.when(uniqueFieldEntity.getDefinitionId()).thenReturn(DEFINITION_ID);
        Mockito.when(uniqueFieldEntity.getFieldUri()).thenReturn(FIELD_URI);
        List<UniqueFieldEntity> queryResult = new ArrayList<>();
        queryResult.add(uniqueFieldEntity);
        Mockito.when(dbDao.fetchWithNamed(Matchers.eq(queryUniqueFieldEntity), Matchers.anyList()))
                .thenReturn(queryResult);

        String queryUniqueFieldConstraintEntity = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_AND_UNIQUE_FIELD_ID;
        UniqueValueEntity uniqueValueEntity = Mockito.mock(UniqueValueEntity.class);
        List<UniqueValueEntity> queryUniqueValueEntityResult = new ArrayList<>(1);
        queryUniqueValueEntityResult.add(uniqueValueEntity);
        Mockito.when(dbDao.fetchWithNamed(Matchers.eq(queryUniqueFieldConstraintEntity), Matchers.anyList()))
                .thenReturn(queryUniqueValueEntityResult);

        uniqueValueDao.registerOrUpdateUniqueValue(INSTANCE_ID, DEFINITION_ID, FIELD_URI, PROPERTY_VALUE);
        Mockito.verify(uniqueValueEntity).setValue(PROPERTY_VALUE);
        Mockito.verify(dbDao).saveOrUpdate(uniqueValueEntity);

    }

    @Test
    public void should_CreateEntity_When_NotRegisteredValue() {
        String query = UniqueFieldEntity.QUERY_NAME_SELECT_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI;
        UniqueFieldEntity uniqueFieldEntity = Mockito.mock(UniqueFieldEntity.class);
        Mockito.when(uniqueFieldEntity.getDefinitionId()).thenReturn(DEFINITION_ID);
        Mockito.when(uniqueFieldEntity.getFieldUri()).thenReturn(FIELD_URI);
        List<UniqueFieldEntity> queryResult = new ArrayList<>();
        queryResult.add(uniqueFieldEntity);
        Mockito.when(dbDao.fetchWithNamed(Matchers.eq(query), Matchers.anyList())).thenReturn(queryResult);

        uniqueValueDao.registerOrUpdateUniqueValue(INSTANCE_ID, DEFINITION_ID, FIELD_URI, PROPERTY_VALUE);

        ArgumentCaptor<UniqueValueEntity> saveOrUpdateArgCapture = ArgumentCaptor.forClass(
                UniqueValueEntity.class);
        Mockito.verify(dbDao).saveOrUpdate(saveOrUpdateArgCapture.capture());

        UniqueValueEntity createdEntity = saveOrUpdateArgCapture.getValue();
        Assert.assertEquals(INSTANCE_ID, createdEntity.getInstanceId());
        Assert.assertEquals(PROPERTY_VALUE, createdEntity.getValue());

        UniqueFieldEntity uniqueField = createdEntity.getUniqueField();
        Assert.assertEquals(DEFINITION_ID, uniqueField.getDefinitionId());
        Assert.assertEquals(FIELD_URI, uniqueField.getFieldUri());
    }

    @Test
    public void should_DoNothing_When_UniqueFieldNotRegistered() {
        uniqueValueDao.registerOrUpdateUniqueValue(INSTANCE_ID, DEFINITION_ID, FIELD_URI, PROPERTY_VALUE);

        Mockito.verify(dbDao, Mockito.never())
                .fetchWithNamed(Matchers.eq(
                        UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_INSTANCE_ID_AND_UNIQUE_FIELD_ID),
                                Matchers.anyList());
    }

    @Test
    public void should_AddAllUniqueFields_When_MethodRegisterIsCalled() {

        UniqueField firstUniqueField = Mockito.mock(UniqueField.class);
        Mockito.when(firstUniqueField.getDefinitionId()).thenReturn("firstDefinitionId");
        Mockito.when(firstUniqueField.getFieldUri()).thenReturn("firstFieldUri");
        UniqueFieldEntity firstUniqueFieldEntity = new UniqueFieldEntity();
        firstUniqueFieldEntity.setDefinitionId("firstDefinitionId");
        firstUniqueFieldEntity.setFieldUri("firstFieldUri");

        UniqueField secondUniqueField = Mockito.mock(UniqueField.class);
        Mockito.when(secondUniqueField.getDefinitionId()).thenReturn("secondDefinitionId");
        Mockito.when(secondUniqueField.getFieldUri()).thenReturn("secondFieldUri");
        UniqueFieldEntity secondUniqueFieldEntity = new UniqueFieldEntity();
        secondUniqueFieldEntity.setDefinitionId("secondDefinitionId");
        secondUniqueFieldEntity.setFieldUri("secondFieldUri");

        List<UniqueField> tobeRemoved = new ArrayList<>(2);
        tobeRemoved.add(firstUniqueField);
        tobeRemoved.add(secondUniqueField);

        uniqueValueDao.register(tobeRemoved);

        Mockito.verify(dbDao).saveOrUpdate(firstUniqueFieldEntity);
        Mockito.verify(dbDao).saveOrUpdate(secondUniqueFieldEntity);
    }

    @Test
    public void should_DeleteAllUniqueFields_When_UnRegisterMethodIsCalled() {

        String uniqueFieldDefinitionId = "uniqueFieldDefinitionId";
        String uniqueFieldUri = "uniqueFieldUri";
        UniqueField uniqueField = Mockito.mock(UniqueField.class);
        Mockito.when(uniqueField.getFieldUri()).thenReturn(uniqueFieldUri);
        Mockito.when(uniqueField.getDefinitionId()).thenReturn(uniqueFieldDefinitionId);
        String secondUniqueFieldDefinitionId = "secondUniqueFieldDefinitionId";
        String secondFieldUri = "secondFieldUri";
        UniqueField secondUniqueField = Mockito.mock(UniqueField.class);
        Mockito.when(secondUniqueField.getFieldUri()).thenReturn(secondFieldUri);
        Mockito.when(secondUniqueField.getDefinitionId()).thenReturn(secondUniqueFieldDefinitionId);

        uniqueValueDao.unRegister(Arrays.asList(uniqueField, secondUniqueField));
        ArgumentCaptor<List<Pair<String, Object>>> uniqueFieldArgCapture = ArgumentCaptor.forClass(List.class);

        //Verifying query arguments.
        Mockito.verify(dbDao, Mockito.times(2))
                .executeUpdate(Matchers.eq(UniqueFieldEntity.QUERY_NAME_DELETE_UNIQUE_FIELD_BY_DEFINITION_ID_AND_FIELD_URI),
                               uniqueFieldArgCapture.capture());

        List<List<Pair<String, Object>>> actualQueryArgs = uniqueFieldArgCapture.getAllValues();

        //verifying query arguments of first invocation
        Map<String, Object> expectedArgumentsOfFirstInvocation = new HashMap<>(2);
        expectedArgumentsOfFirstInvocation.put("definitionId", uniqueFieldDefinitionId);
        expectedArgumentsOfFirstInvocation.put("fieldUri", uniqueFieldUri);
        assertQueryPairs(actualQueryArgs.get(0), expectedArgumentsOfFirstInvocation);

        //verifying query arguments of second invocation
        Map<String, Object> expectedArgumentsForSecondInvocation = new HashMap<>(2);
        expectedArgumentsForSecondInvocation.put("definitionId", secondUniqueFieldDefinitionId);
        expectedArgumentsForSecondInvocation.put("fieldUri", secondFieldUri);
        assertQueryPairs(actualQueryArgs.get(1), expectedArgumentsForSecondInvocation);

    }

    @Test
    public void getAllUniqueFieldsTest() {
        uniqueValueDao.getAllUniqueFields();
        Mockito.verify(dbDao).fetchWithNamed(UniqueFieldEntity.QUERY_NAME_SELECT_ALL_UNIQUE_FIELDS, Collections.emptyList());
    }

    @Test
    public void should_NotBeNull_When_FoundRegistrationOfValue() {

        String query = UniqueValueEntity.QUERY_NAME_SELECT_UNIQUE_VALUE_BY_VALUE_DEFINITION_ID_AND_FIELD_URI_CASE_INSENSITIVE;
        UniqueValueEntity uniqueValueEntity = new UniqueValueEntity();
        uniqueValueEntity.setInstanceId(INSTANCE_ID);
        List<UniqueValueEntity> searchResult = new ArrayList<>(1);
        searchResult.add(uniqueValueEntity);

        Mockito.when(dbDao.fetchWithNamed(Matchers.eq(query), Matchers.anyList())).thenReturn(searchResult);
        Assert.assertEquals(INSTANCE_ID, uniqueValueDao.getInstanceId(DEFINITION_ID, FIELD_URI, PROPERTY_VALUE));

        ArgumentCaptor<List<Pair<String, Object>>> queryArgCapture = ArgumentCaptor.forClass(List.class);

        //verifying query arguments
        Mockito.verify(dbDao).fetchWithNamed(Matchers.eq(query), queryArgCapture.capture());
        Map<String, Object> expectedArgumentsForQuery = new HashMap<>(3);
        expectedArgumentsForQuery.put("definitionId", DEFINITION_ID);
        expectedArgumentsForQuery.put("value", PROPERTY_VALUE);
        expectedArgumentsForQuery.put("fieldUri", FIELD_URI);
        assertQueryPairs(queryArgCapture.getValue(), expectedArgumentsForQuery);

    }

    @Test
    public void should_BeNull_When_NotFoundRegistrationOfValue() {
        Assert.assertNull(uniqueValueDao.getInstanceId(null, null, null));
    }

    /**
     * Iterate ove <codde>expected</codde> and check if there is such pair key -> value.
     *
     * @param pairs
     *         - the pairs to be checked.
     * @param expected
     *         - the expected key value.
     */
    private void assertQueryPairs(List<Pair<String, Object>> pairs, Map<String, Object> expected) {
        if (pairs.size() != expected.size()) {
            Assert.fail();
        }
        expected.forEach((k, v) -> {
            Optional<Boolean> result = pairs.stream()
                    .filter(pair -> pair.getFirst().equals(k))
                    .findFirst()
                    .map(pair -> pair.getSecond().equals(v));

            if (result.isPresent()) {
                if (!result.get()) {
                    Assert.fail();
                }
            } else {
                Assert.fail();
            }

        });
    }
}