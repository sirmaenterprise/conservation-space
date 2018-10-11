package com.sirma.itt.sep.instance.unique;

import com.sirma.itt.seip.convert.TypeConverter;
import com.sirma.itt.seip.definition.DefinitionService;
import com.sirma.itt.seip.domain.definition.DataTypeDefinition;
import com.sirma.itt.seip.domain.definition.DefinitionModel;
import com.sirma.itt.seip.domain.definition.PropertyDefinition;
import com.sirma.itt.seip.domain.instance.Instance;
import com.sirma.itt.seip.domain.search.SearchArguments;
import com.sirma.itt.seip.search.ResultItem;
import com.sirma.itt.seip.search.ResultItemTransformer;
import com.sirma.itt.seip.search.SearchService;
import com.sirma.itt.seip.testutil.fakes.TransactionSupportFake;
import com.sirma.itt.seip.tx.TransactionSupport;
import com.sirma.itt.sep.instance.unique.persistence.UniqueValueDao;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Tests for {@link UniqueValueValidationServiceImpl#registerOldUniqueValues(String, String)}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueValidationServiceImplRegistrationOfOldValuesTest {

    private static final String DEFINITION_ID_OF_MISSING_DEFINITION_MODEL = "definition-id-of-missing-definition-model";
    private static final String DEFINITION_ID = "definition-id";

    private static final String DATE_PROPERTY_NAME = "date-property-name";
    private static final String DATE_PROPERTY_FIELD_URI = "date-property-field-uri";
    private static final Long DATE_PROPERTY_UNIQUE_FIELD_ID = 1l;

    private static final String TEXT_PROPERTY_NAME = "text-property-name";
    private static final String TEXT_PROPERTY_FIELD_URI = "text-property-field-uri";
    private static final Long TEXT_PROPERTY_UNIQUE_FIELD_ID = 2l;

    private static final String TEXT_PROPERTY_NAME_NOT_REGISTERED = "text-property-name-not-registered";
    private static final String TEXT_PROPERTY_FIELD_URI_NOT_REGISTERED = "text-property-field-uri-not-registered";

    private static final int SEMANTIC_SEARCH_RESULT_COUNT = 3;
    private static final String INSTANCE_ONE_ID = "emf:one";
    private static final String INSTANCE_ONE_VALUE = "valueOne";
    private static final String INSTANCE_ONE_CONVERTED_VALUE = "convertedValueOne";

    private static final String INSTANCE_TWO_ID = "emf:two";
    private static final String INSTANCE_TWO_VALUE = "valueTwo";
    private static final String INSTANCE_TWO_CONVERTED_VALUE = "convertedValueTwo";

    private static final String INSTANCE_THREE_ID = "emf:two";
    private static final String INSTANCE_THREE_VALUE = "valueTwo";
    private static final String INSTANCE_THREE_CONVERTED_VALUE = "convertedValueTwo";

    @Mock
    private SearchService searchService;

    @Mock
    private DefinitionService definitionService;

    @Mock
    private UniqueValueDao uniqueValueDao;

    @Spy
    private TransactionSupport transactionSupport = new TransactionSupportFake();

    @Mock
    private TypeConverter typeConverter;

    @InjectMocks
    private UniqueValueValidationServiceImpl uniqueValueValidationService;

    @Test(expected = PersistenceException.class)
    public void should_ThrowException_When_InsertOneByOneAndExceptionIsNotConstraintViolationException()
            throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();
        setupSearch();

        // set first insert to throw ConstraintViolationException
        Query nativeQuery = Mockito.mock(Query.class);
        PersistenceException constraintViolationException = createConstraintViolationException();
        Mockito.when(nativeQuery.executeUpdate()).thenThrow(constraintViolationException);
        // set second insert to throw PersistenceException
        Query oneByOneQuery = Mockito.mock(Query.class);
        Mockito.when(oneByOneQuery.executeUpdate()).thenThrow(PersistenceException.class);

        Mockito.when(uniqueValueDao.buildNativeInsertUniqueValuesQuery(Matchers.anyInt(),
                                                                       Matchers.eq(TEXT_PROPERTY_UNIQUE_FIELD_ID)))
                .thenReturn(nativeQuery)
                .thenReturn(oneByOneQuery);
        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI);
    }

    @Test(expected = PersistenceException.class)
    public void should_ThrowException_When_ExceptionIsNotConstraintViolationException() throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();
        setupSearch();
        // set insert to throw PersistenceException
        Query nativeQuery = Mockito.mock(Query.class);
        Mockito.when(nativeQuery.executeUpdate()).thenThrow(PersistenceException.class);
        Mockito.when(uniqueValueDao.buildNativeInsertUniqueValuesQuery(SEMANTIC_SEARCH_RESULT_COUNT,
                                                                       TEXT_PROPERTY_UNIQUE_FIELD_ID))
                .thenReturn(nativeQuery);

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI);
    }

    @Test
    public void should_InsertUniqueValuesWithOneByOneQuery_When_ThereIsConstraintViolationException() throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();
        setupSearch();
        // mock first insert when try to insert instanceOne and instanceTwo with one insert.
        Query multiValueNativeQuery = Mockito.mock(Query.class);
        Query firstInstanceNativeQuery = Mockito.mock(Query.class);
        Query secondInstanceNativeQuery = Mockito.mock(Query.class);
        Query thirdInstanceNativeQuery = Mockito.mock(Query.class);
        PersistenceException constraintViolationException = createConstraintViolationException();
        Mockito.when(multiValueNativeQuery.executeUpdate()).thenThrow(constraintViolationException);

        Mockito.when(uniqueValueDao.buildNativeInsertUniqueValuesQuery(Matchers.anyInt(),
                                                                       Matchers.eq(TEXT_PROPERTY_UNIQUE_FIELD_ID)))
                .thenReturn(multiValueNativeQuery)
                .thenReturn(firstInstanceNativeQuery)
                .thenReturn(secondInstanceNativeQuery)
                .thenReturn(thirdInstanceNativeQuery);

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI);
        // Verify that the three queries has been executed.
        // First query try to execute instanceOne and instanceTwo at once.
        Mockito.verify(multiValueNativeQuery).executeUpdate();
        // execute query for instanceOne
        Mockito.verify(firstInstanceNativeQuery).executeUpdate();
        // execute query for instanceTwo
        Mockito.verify(secondInstanceNativeQuery).executeUpdate();
        // execute query for instanceTwo
        Mockito.verify(thirdInstanceNativeQuery).executeUpdate();
    }

    @Test
    public void should_InsertAllUniqueValuesWithOneQuery_When_ThereIsNotPersistenceException() throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();
        setupSearch();
        Query nativeQuery = Mockito.mock(Query.class);
        Mockito.when(uniqueValueDao.buildNativeInsertUniqueValuesQuery(SEMANTIC_SEARCH_RESULT_COUNT,
                                                                       TEXT_PROPERTY_UNIQUE_FIELD_ID))
                .thenReturn(nativeQuery);

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI);

        // Verifies  positions and values of passed parameters.
        Mockito.verify(nativeQuery).setParameter(1, INSTANCE_ONE_CONVERTED_VALUE);
        Mockito.verify(nativeQuery).setParameter(2, INSTANCE_ONE_ID);
        Mockito.verify(nativeQuery).setParameter(3, INSTANCE_TWO_CONVERTED_VALUE);
        Mockito.verify(nativeQuery).setParameter(4, INSTANCE_TWO_ID);
        Mockito.verify(nativeQuery).setParameter(5, INSTANCE_THREE_CONVERTED_VALUE);
        Mockito.verify(nativeQuery).setParameter(6, INSTANCE_THREE_ID);

        // Verifies that query is created once
        Mockito.verify(uniqueValueDao).buildNativeInsertUniqueValuesQuery(Matchers.anyInt(), Matchers.anyLong());
        Mockito.verify(nativeQuery).executeUpdate();
    }

    private void setupSearch() {
        ResultItem resultItemOne = createResultItem(INSTANCE_ONE_ID, INSTANCE_ONE_VALUE, INSTANCE_ONE_CONVERTED_VALUE);
        ResultItem resultItemTwo = createResultItem(INSTANCE_TWO_ID, INSTANCE_TWO_VALUE, INSTANCE_TWO_CONVERTED_VALUE);
        ResultItem resultItemThree = createResultItem(INSTANCE_THREE_ID, INSTANCE_THREE_VALUE,
                                                      INSTANCE_THREE_CONVERTED_VALUE);

        Mockito.when(
                searchService.stream(Matchers.any(SearchArguments.class), Matchers.any(ResultItemTransformer.class)))
                .thenReturn(Stream.of(resultItemOne, resultItemTwo, resultItemThree));
    }

    @Test
    public void should_SearchNotDateUniqueValues_When_InPropertyDefinitionIsDefinedTextType() throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();
        Mockito.when(
                searchService.stream(Matchers.any(SearchArguments.class), Matchers.any(ResultItemTransformer.class)))
                .thenReturn(Stream.empty());

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI);

        ArgumentCaptor<SearchArguments<Instance>> searchArgumentsCaptor = ArgumentCaptor.forClass(
                SearchArguments.class);
        Mockito.verify(searchService).stream(searchArgumentsCaptor.capture(), Matchers.any());
        SearchArguments<Instance> value = searchArgumentsCaptor.getValue();
        Assert.assertFalse(value.getStringQuery()
                                   .contains(
                                           "bind(CONCAT(STR(MONTH(?value)), \"/\", STR(DAY(?value)), \"/\", STR(YEAR(?value))) as ?dateValue) ."));
        Map<String, Serializable> bindings = value.getArguments();
        Assert.assertEquals(TEXT_PROPERTY_FIELD_URI, bindings.get("fieldUriVariable"));
        Assert.assertEquals(DEFINITION_ID, bindings.get("definitionIdVariable"));
    }

    @Test
    public void should_SearchDateUniqueValues_When_InPropertyDefinitionIsDefinedDateType() throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();
        Mockito.when(
                searchService.stream(Matchers.any(SearchArguments.class), Matchers.any(ResultItemTransformer.class)))
                .thenReturn(Stream.empty());

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, DATE_PROPERTY_FIELD_URI);

        ArgumentCaptor<SearchArguments<Instance>> searchArgumentsCaptor = ArgumentCaptor.forClass(
                SearchArguments.class);
        Mockito.verify(searchService).stream(searchArgumentsCaptor.capture(), Matchers.any());
        SearchArguments<Instance> value = searchArgumentsCaptor.getValue();
        Assert.assertTrue(value.getStringQuery()
                                  .contains(
                                          "bind(CONCAT(STR(MONTH(?value)), \"/\", STR(DAY(?value)), \"/\", STR(YEAR(?value))) as ?dateValue) ."));
        Map<String, Serializable> bindings = value.getArguments();
        Assert.assertEquals(DATE_PROPERTY_FIELD_URI, bindings.get("fieldUriVariable"));
        Assert.assertEquals(DEFINITION_ID, bindings.get("definitionIdVariable"));
    }

    @Test
    public void should_NotRegisterOldValue_When_UniqueFieldIsNotRegistered() throws Exception {
        setupDefinitionService();
        setupUniqueValueDao();

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI_NOT_REGISTERED);
        Mockito.verify(searchService, Mockito.never()).search(Matchers.any(), Matchers.any());
    }

    @Test
    public void should_NotRegisterOldValue_When_PropertyDefinitionIsNotFound() throws Exception {
        setupDefinitionService();

        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID, "");

        Mockito.verify(searchService, Mockito.never()).search(Matchers.any(), Matchers.any());
    }

    @Test
    public void should_NotRegisterOldValue_When_DefinitionModelNotFound() throws Exception {
        uniqueValueValidationService.registerOldUniqueValues(DEFINITION_ID_OF_MISSING_DEFINITION_MODEL, "");

        Mockito.verify(searchService, Mockito.never()).search(Matchers.any(), Matchers.any());
    }

    private void setupUniqueValueDao() {
        Mockito.when(uniqueValueDao.getUniqueFieldId(DEFINITION_ID, DATE_PROPERTY_FIELD_URI))
                .thenReturn(DATE_PROPERTY_UNIQUE_FIELD_ID);
        Mockito.when(uniqueValueDao.getUniqueFieldId(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI))
                .thenReturn(TEXT_PROPERTY_UNIQUE_FIELD_ID);
        Mockito.when(uniqueValueDao.getUniqueFieldId(DEFINITION_ID, TEXT_PROPERTY_FIELD_URI_NOT_REGISTERED))
                .thenReturn(null);
    }

    private void setupDefinitionService() {
        DefinitionModel definitionModel = Mockito.mock(DefinitionModel.class);
        PropertyDefinition dateProperty = createPropertyDefinition(DATE_PROPERTY_NAME, DATE_PROPERTY_FIELD_URI,
                                                                   DataTypeDefinition.DATE);
        PropertyDefinition textProperty = createPropertyDefinition(TEXT_PROPERTY_NAME, TEXT_PROPERTY_FIELD_URI,
                                                                   DataTypeDefinition.TEXT);
        PropertyDefinition notRegisteredProperty = createPropertyDefinition(TEXT_PROPERTY_NAME_NOT_REGISTERED,
                                                                            TEXT_PROPERTY_FIELD_URI_NOT_REGISTERED,
                                                                            DataTypeDefinition.TEXT);

        Mockito.when(definitionModel.fieldsStream())
                .thenReturn(Stream.of(dateProperty, textProperty, notRegisteredProperty));
        Mockito.when(definitionService.find(DEFINITION_ID_OF_MISSING_DEFINITION_MODEL)).thenReturn(null);
        Mockito.when(definitionService.find(DEFINITION_ID)).thenReturn(definitionModel);
    }

    private PropertyDefinition createPropertyDefinition(String propertyName, String fieldUri, String dataTypeName) {
        PropertyDefinition propertyDefinition = Mockito.mock(PropertyDefinition.class);
        Mockito.when(propertyDefinition.getName()).thenReturn(propertyName);
        Mockito.when(propertyDefinition.getUri()).thenReturn(fieldUri);
        DataTypeDefinition dataTypeDefinition = Mockito.mock(DataTypeDefinition.class);
        Mockito.when(propertyDefinition.getDataType()).thenReturn(dataTypeDefinition);
        Mockito.when(dataTypeDefinition.getName()).thenReturn(dataTypeName);
        return propertyDefinition;
    }

    private ResultItem createResultItem(String instanceId, Serializable value, String convertedValue) {
        ResultItem item = Mockito.mock(ResultItem.class);
        Mockito.when(item.getString("instance")).thenReturn(instanceId);
        Mockito.when(item.getResultValue("uniqueValue")).thenReturn(value);
        Mockito.when(typeConverter.convert(Matchers.any(), Matchers.eq(value))).thenReturn(convertedValue);
        return item;
    }

    private PersistenceException createConstraintViolationException() {
        return new PersistenceException(Mockito.mock(ConstraintViolationException.class));
    }
}
