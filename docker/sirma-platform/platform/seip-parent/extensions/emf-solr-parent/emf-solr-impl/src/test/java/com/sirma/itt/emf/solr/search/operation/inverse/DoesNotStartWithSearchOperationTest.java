package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Test class for {@link DoesNotStartWithSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DoesNotStartWithSearchOperationTest {

    private DoesNotStartWithSearchOperation doesNotStartWithSearchOperation = new DoesNotStartWithSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "does_not_start_with", "123");
        Assert.assertTrue(doesNotStartWithSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("title", "string", "", "123");
        Assert.assertFalse(doesNotStartWithSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_BuildOperationForSingleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "does_not_start_with", "123");
        StringBuilder builder = new StringBuilder();

        doesNotStartWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-(title:(123*))", builder.toString());
    }

    @Test
    public void test_BuildOperationForMultipleValue() {
        Rule rule = SearchOperationUtils.createRule("title", "string", "does_not_start_with", Arrays.asList("123", "abc"));
        StringBuilder builder = new StringBuilder();

        doesNotStartWithSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-(title:(123*) OR title:(abc*))", builder.toString());
    }
}
