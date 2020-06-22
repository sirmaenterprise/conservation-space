package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link DoesNotContainSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DoesNotContainSearchOperationTest {

    private DoesNotContainSearchOperation doesNotContainSearchOperation = new DoesNotContainSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("testField", "string", "does_not_contain", "test");
        Assert.assertTrue(doesNotContainSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("testField", "string", "equals", "test");
        Assert.assertFalse(doesNotContainSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_buildOperation() {
        Rule rule = SearchOperationUtils.createRule("testField", "string", "does_not_contain","test");
        StringBuilder builder = new StringBuilder();

        doesNotContainSearchOperation.buildOperation(builder, rule);
        Assert.assertEquals("-(testField:(*test*))", builder.toString());
    }
}
