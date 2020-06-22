import {Injectable, Inject} from 'app/app';
import {SearchResolver} from 'search/resolvers/search-resolver';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';
import {DynamicDateRange} from 'search/components/advanced/dynamic-date-range/dynamic-date-range';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

/**
 * Resolver for {@link AdvancedSearchCriteriaOperators.IS_WITHIN} tree rules. If such a rule is present in the
 * search tree, then the dynamic date range is calculated based on the date offset.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(PromiseAdapter)
export class DynamicDateRangeResolver extends SearchResolver {

  constructor(promiseAdapter) {
    super();
    this.promiseAdapter = promiseAdapter;
  }

  resolve(tree) {
    return this.promiseAdapter.promise((resolve) => {
      var treeWalker = new DynamicTreeWalkListener().addOnRule((rule) => {
        if (this.isRuleValueEmbeddedTree(rule)) {
          QueryBuilder.walk(rule.value, treeWalker);
        } else {
          this.resolveRule(rule);
        }
      });
      QueryBuilder.walk(tree, treeWalker);
      resolve(tree);
    });
  }

  resolveRule(rule) {
    if (this.isDynamicRangeRule(rule)) {
      if (rule.value.length === 3) {
        var dateOffset = {
          dateStep: rule.value[0],
          offset: rule.value[1],
          offsetType: rule.value[2]
        };
        rule.value = DynamicDateRange.buildDateRange(dateOffset);
      } else {
        // Should not send incorrect data to the back-end
        rule.value = [];
      }
    }
  }

  isDynamicRangeRule(rule) {
    return rule.operator === AdvancedSearchCriteriaOperators.IS_WITHIN.id;
  }

}