package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link EmptySearchOperation}.
 *
 * @author Hristo Lungov
 */
public class EmptySearchOperationTest {

    private EmptySearchOperation emptySearchOperation = new EmptySearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("property", "string", "empty", "true");
        Assert.assertTrue(emptySearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("property", "string", "contains", "test");
        Assert.assertFalse(emptySearchOperation.isApplicable(rule));
    }

    @Test
    public void test_BuildOperationForIsEmpty() {
        Rule rule = SearchOperationUtils.createRule("property", "string", "empty", "true");
        StringBuilder builder = new StringBuilder();

        emptySearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-property:*", builder.toString());
    }

    @Test
    public void test_BuildOperationForIsNotEmpty() {
        Rule rule = SearchOperationUtils.createRule("property", "string", "not_empty", "false");
        StringBuilder builder = new StringBuilder();

        emptySearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("property:*", builder.toString());
    }
}
