package com.sirma.itt.emf.semantic.queries;

import com.sirma.itt.seip.search.NamedQueries;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

/**
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectInstancesDataPropertiesOnlyQueryCallbackTest {

    @Spy
    private SelectInstancesDataPropertiesOnlyQueryCallback callback;

    @Test
    public void should_ReturnCorrectQuery() {
        String expected = "SELECT DISTINCT ?uri ?propertyName ?propertyValue WHERE {  ?uri ?propertyName ?propertyValue . %s FILTER EXISTS {  ?propertyName a emf:DefinitionDataProperty.  } FILTER(";
        Assert.assertEquals(callback.getMultipleStart().replaceAll("\\s+", " "), expected.replaceAll("\\s+", " "));
    }

    @Test
    public void should_ReturnCorectName() {
        Assert.assertEquals(callback.getName(), NamedQueries.SELECT_DATA_PROPERTIES_BY_IDS);
    }
}