package com.sirma.itt.sep.properties.unique;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unit tests for {@link UniqueValueRequestBodyReader ).
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UniqueValueRequestBodyReaderTest {

    @InjectMocks
    private UniqueValueRequestBodyReader reader;

    @Test
    public void should_ValueBeNull_When_JsonKeyValueIsEmpty() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("empty-value.json")) {
            UniqueValueRequest uniqueValueRequest = reader.readFrom(null, null, null, null, null, stream);
            Assert.assertNull(uniqueValueRequest.getValue());
        }
    }

    @Test
    public void should_ValueBeNull_When_JsonKeyValueIsNull() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("null-value.json")) {
            UniqueValueRequest uniqueValueRequest = reader.readFrom(null, null, null, null, null, stream);
            Assert.assertNull(uniqueValueRequest.getValue());
        }
    }

    @Test
    public void should_ValueBeNull_When_JsonKeyValueIsMissing() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("missing-value.json")) {
            UniqueValueRequest uniqueValueRequest = reader.readFrom(null, null, null, null, null, stream);
            Assert.assertNull(uniqueValueRequest.getValue());
        }
    }

    @Test
    public void should_Extract_AllProperties() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("full-request.json")) {
            UniqueValueRequest uniqueValueRequest = reader.readFrom(null, null, null, null, null, stream);
            Assert.assertEquals("definition-id", uniqueValueRequest.getDefinitionId());
            Assert.assertEquals("instance-id", uniqueValueRequest.getInstanceId());
            Assert.assertEquals("title", uniqueValueRequest.getPropertyName());
            Assert.assertEquals("value", uniqueValueRequest.getValue());
        }
    }

    @Test(expected = BadRequestException.class)
    public void should_ThrowException_When_PropertyNameAndDefinitionIdAreMissing() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("missing-property-name-and-definition-id.json")) {
            reader.readFrom(null, null, null, null, null, stream);
        }
    }

    @Test(expected = BadRequestException.class)
    public void should_ThrowException_When_PropertyNameIsMissing() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("missing-property-name.json")) {
            reader.readFrom(null, null, null, null, null, stream);
        }
    }

    @Test(expected = BadRequestException.class)
    public void should_ThrowException_When_DefinitionIdIsMissing() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("missing-definition-id.json")) {
            reader.readFrom(null, null, null, null, null, stream);
        }
    }

    @Test(expected = BadRequestException.class)
    public void should_ThrowException_When_JsonIsEmpty() throws IOException {
        try (InputStream stream = this.getClass().getResourceAsStream("empty.json")) {
            reader.readFrom(null, null, null, null, null, stream);
        }
    }

    @Test
    public void should_NotBeReadable_When_TypeIsNotCorrect() {
        Assert.assertFalse(reader.isReadable(UniqueValueRequestBodyReader.class, null, null, null));
    }

    @Test
    public void should_BeReadable_When_TypeIsCorrect() {
        Assert.assertTrue(reader.isReadable(UniqueValueRequest.class, null, null, null));
    }
}