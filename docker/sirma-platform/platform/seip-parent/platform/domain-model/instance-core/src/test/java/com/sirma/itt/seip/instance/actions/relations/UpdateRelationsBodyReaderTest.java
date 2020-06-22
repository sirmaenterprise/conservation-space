package com.sirma.itt.seip.instance.actions.relations;

import com.sirma.itt.seip.rest.exceptions.BadRequestException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Tests for {@link UpdateRelationsBodyReader}.
 *
 * @author Boyan Tonchev.
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateRelationsBodyReaderTest {

    @Spy
    private UpdateRelationsBodyReader updateRelationsBodyReader;

    @Test
    public void should_ProcessAddRelation_When_AddKeyIsMissing() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations-only-remove-key.json")) {
            executeAndAssertRemoveRelations(stream);
        }
    }

    private void executeAndAssertRemoveRelations(InputStream stream ) throws IOException {
        UpdateRelationsRequest updateRelationsRequest = updateRelationsBodyReader.readFrom(null, null, null, null,
                                                                                           null, stream);
        Collection<UpdateRelationData> linksToBeAdded = updateRelationsRequest.getLinksToBeRemoved();
        Assert.assertTrue(linksToBeAdded.size() == 2);
        assertInstanceIds(linksToBeAdded, "emf:hasAttachment", Arrays.asList("emf:0004", "emf:0005"));
        assertInstanceIds(linksToBeAdded, "emf:hasWatchers", Arrays.asList("emf:0004", "emf:0005"));
    }


    @Test
    public void should_BeSetCorrectLinksToBeRemoved_When_JsonIsCorrect() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations.json")) {
            executeAndAssertRemoveRelations(stream);
        }
    }

    @Test
    public void should_BeSetCorrectLinksToBeAdded_When_JsonIsCorrect() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations.json")) {
            UpdateRelationsRequest updateRelationsRequest = updateRelationsBodyReader.readFrom(null, null, null, null,
                                                                                               null, stream);
            Collection<UpdateRelationData> linksToBeAdded = updateRelationsRequest.getLinksToBeAdded();
            Assert.assertTrue(linksToBeAdded.size() == 2);
            assertInstanceIds(linksToBeAdded, "emf:hasAttachment", Arrays.asList("emf:0002", "emf:0003"));
            assertInstanceIds(linksToBeAdded, "emf:hasWatchers", Arrays.asList("emf:0002", "emf:0003"));
        }
    }

    @Test
    public void should_BeSetCorrectUserOperation_When_JsonIsCorrect() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations.json")) {
            UpdateRelationsRequest updateRelationsRequest = updateRelationsBodyReader.readFrom(null, null, null, null,
                                                                                               null, stream);
            Assert.assertEquals("userOperation", updateRelationsRequest.getUserOperation());
        }
    }

    @Test
    public void should_BeSetCorrectOperation_When_JsonIsCorrect() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations.json")) {
            UpdateRelationsRequest updateRelationsRequest = updateRelationsBodyReader.readFrom(null, null, null, null,
                                                                                               null, stream);
            Assert.assertEquals(UpdateRelationsRequest.OPERATION_NAME, updateRelationsRequest.getOperation());
        }
    }

    @Test
    public void should_BeSetCorrectTargetId_When_JsonIsCorrect() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations.json")) {
            UpdateRelationsRequest updateRelationsRequest = updateRelationsBodyReader.readFrom(null, null, null, null,
                                                                                               null, stream);
            Assert.assertEquals("emf:0001", updateRelationsRequest.getTargetId());
        }
    }

    @Test(expected = BadRequestException.class)
    public void should_ThrowBadRequestException_When_JsonNotContainsCurrentInstanceId() throws IOException {
        try (InputStream stream = getJsonAsStream("update-relations-without-current-instance-id.json")) {
            updateRelationsBodyReader.readFrom(null, null, null, null, null, stream);
        }
    }

    @Test(expected = BadRequestException.class)
    public void should_ThrowBadRequestException_When_JsonIsEmpty() throws IOException {
        try (InputStream stream = getJsonAsStream("empty.json")) {
            updateRelationsBodyReader.readFrom(null, null, null, null, null, stream);
        }
    }

    @Test
    public void should_NotBeReadable_When_ClassIsNotCorrect() {
        Assert.assertFalse(updateRelationsBodyReader.isReadable(UpdateRelationsBodyReaderTest.class, null, null, null));
    }

    @Test
    public void should_BeReadable_When_ClassIsCorrect() {
        Assert.assertTrue(updateRelationsBodyReader.isReadable(UpdateRelationsRequest.class, null, null, null));
    }

    private InputStream getJsonAsStream(String jsonFileName) {
        return this.getClass().getResourceAsStream(jsonFileName);
    }

    private void assertInstanceIds(Collection<UpdateRelationData> linksData, String linkId, List<String> expectedIds) {
        linksData.forEach(updateRelationData -> {
            Set<String> instances = updateRelationData.getInstances();
            Assert.assertEquals(instances.size(), expectedIds.size());
            Assert.assertTrue(expectedIds.containsAll(instances));
        });
    }
}