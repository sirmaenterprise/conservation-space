package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for {@link DoesNotEqualSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class DoesNotEqualSearchOperationTest {

    private DoesNotEqualSearchOperation doesNotEqualSearchOperation = new DoesNotEqualSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("testField", "string", "does_not_equal", "test");
        Assert.assertTrue(doesNotEqualSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("testField", "string", "not_in", "test");
        Assert.assertTrue(doesNotEqualSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("testField", "string", "equals", "test");
        Assert.assertFalse(doesNotEqualSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_buildOperation() {
        Rule rule = SearchOperationUtils.createRule("testField", "string", "does_not_equal","test");
        StringBuilder builder = new StringBuilder();

        doesNotEqualSearchOperation.buildOperation(builder, rule);
        Assert.assertEquals("-(testField:(test))", builder.toString());
    }

}
