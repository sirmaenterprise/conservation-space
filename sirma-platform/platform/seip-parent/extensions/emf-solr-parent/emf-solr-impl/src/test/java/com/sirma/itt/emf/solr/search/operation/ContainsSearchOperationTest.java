package com.sirma.itt.emf.solr.search.operation;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;

/**
 * Test class for {@link ContainsSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class ContainsSearchOperationTest {

    private ContainsSearchOperation operation = new ContainsSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("a", "string", "contains", "test");
        Assert.assertTrue(operation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("a", "string", "equals", "test");
        Assert.assertFalse(operation.isApplicable(rule));
    }

    @Test
    public void test_buildOperation() {
        Rule rule = SearchOperationUtils.createRule("a", "string", "contains","test");
        StringBuilder builder = new StringBuilder();

        operation.buildOperation(builder, rule);
        Assert.assertEquals("(a:(*test*))", builder.toString());
    }
}
