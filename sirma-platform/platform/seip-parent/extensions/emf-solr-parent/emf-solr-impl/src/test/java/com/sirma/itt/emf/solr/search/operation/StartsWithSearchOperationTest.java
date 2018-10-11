package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Test class for {@link StartsWithSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class StartsWithSearchOperationTest {

    private StartsWithSearchOperation startsWithSearchOperation = new StartsWithSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "starts_with", "123");
        Assert.assertTrue(startsWithSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("title", "string", "", "123");
        Assert.assertFalse(startsWithSearchOperation.isApplicable(rule));

    }

    @Test
    public void test_BuildOperationForSingleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "starts_with", "123");
        StringBuilder builder = new StringBuilder();

        startsWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("(title:(123*))", builder.toString());
    }

    @Test
    public void test_BuildOperationForMultipleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "starts_with", Arrays.asList("123", "abc"));
        StringBuilder builder = new StringBuilder();

        startsWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("(title:(123*) OR title:(abc*))", builder.toString());
    }
}
