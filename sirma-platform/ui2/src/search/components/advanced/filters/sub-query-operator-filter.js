import {Injectable} from 'app/app';
import {AdvancedSearchCriteriaOperators} from '../criteria/advanced-search-criteria-operators';
import _ from 'lodash';

const QUERY_OPERATORS = [
  AdvancedSearchCriteriaOperators.SET_TO_QUERY.id,
  AdvancedSearchCriteriaOperators.NOT_SET_TO_QUERY.id
];

/**
 * Limits the sub queries levels in the advanced search form to be at maximum 1.
 */
@Injectable()
export class SubQueryOperatorFilter {

  filter(config, property, operator) {
    return !(config.level > 2 && _.includes(QUERY_OPERATORS, operator.id));
  }
}