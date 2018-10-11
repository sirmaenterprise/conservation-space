package com.sirma.itt.sep.instance.unique.patch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.Assert;

/**
 * Unit tests for {@link UniquePropertyValuesDbSchemaPatch}
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniquePropertyValuesDbSchemaPatchTest {

    @InjectMocks
    private UniquePropertyValuesDbSchemaPatch uniquePropertyValuesDbSchemaPatch;

    @Test
    public void should_ReturnCorrectPath_When_MethodIsCalled() {
        Assert.assertTrue(uniquePropertyValuesDbSchemaPatch.getPath().endsWith("unique-fields-changelog.xml"));
    }
}