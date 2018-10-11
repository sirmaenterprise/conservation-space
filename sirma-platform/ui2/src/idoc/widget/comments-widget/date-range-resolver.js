import {Injectable, Inject} from 'app/app';
import {DynamicDateRangeResolver} from 'search/resolvers/dynamic-date-range-resolver';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

@Injectable()
@Inject(DynamicDateRangeResolver)
export class DateRangeResolver {

  constructor(dynamicDateRangeResolver) {
    this.dymanicDateRangeResolver = dynamicDateRangeResolver;
  }

  resolveRule(rule) {
    if (this.isInvalidRule(rule)) {
      return ['', ''];
    }
    if (this.isDynamicRangeRule(rule)) {
      this.dymanicDateRangeResolver.resolveRule(rule);
      return rule.value;
    }
    if (this.isBeforeRule(rule)) {
      return ['', rule.value];
    }
    if (this.isAfterRule(rule)) {
      return [rule.value, ''];
    }
    return rule.value;
  }

  isDynamicRangeRule(rule) {
    return rule.operator === AdvancedSearchCriteriaOperators.IS_WITHIN.id;
  }

  isBeforeRule(rule) {
    return rule.operator === AdvancedSearchCriteriaOperators.IS_BEFORE.id;
  }

  isAfterRule(rule) {
    return rule.operator === AdvancedSearchCriteriaOperators.IS_AFTER.id;
  }

  isInvalidRule(rule) {
    return !rule.operator || !rule.value;
  }
}