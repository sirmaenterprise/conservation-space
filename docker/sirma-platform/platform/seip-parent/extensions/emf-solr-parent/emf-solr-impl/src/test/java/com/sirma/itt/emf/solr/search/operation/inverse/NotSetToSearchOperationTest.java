package com.sirma.itt.emf.solr.search.operation.inverse;

import com.sirma.itt.seip.domain.search.tree.Rule;
import com.sirma.itt.seip.testutil.search.SearchOperationUtils;
import org.junit.Assert;
import org.junit.Test;

import static com.sirma.itt.seip.domain.search.tree.CriteriaWildcards.ANY_OBJECT;

/**
 * Test class for {@link NotSetToSearchOperation}.
 *
 * @author Hristo Lungov
 */
public class NotSetToSearchOperationTest {

    private NotSetToSearchOperation notSetToSearchOperation = new NotSetToSearchOperation();

    @Test
    public void test_IsApplicable() {
        Rule rule = SearchOperationUtils.createRule("testField", "", "not_set_to", "1");
        Assert.assertTrue(notSetToSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("testField", "", "", "1");
        Assert.assertFalse(notSetToSearchOperation.isApplicable(rule));

        rule = SearchOperationUtils.createRule("testField", "", "in", "1");
        Assert.assertFalse(notSetToSearchOperation.isApplicable(rule));
    }

    @Test
    public void test_BuildOperation() {
        Rule rule = SearchOperationUtils.createRule("testField", "", "not_set_to", "emf:uri1");
        StringBuilder builder = new StringBuilder();

        notSetToSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-(testField:(emf\\:uri1))", builder.toString());
    }

    @Test
    public void test_BuildOperationAnyObject() {
        Rule rule = SearchOperationUtils.createRule("testField", "", "not_set_to", ANY_OBJECT);
        StringBuilder builder = new StringBuilder();

        notSetToSearchOperation.buildOperation(builder, rule);

        Assert.assertEquals("-(testField:(*))", builder.toString());
    }
}
