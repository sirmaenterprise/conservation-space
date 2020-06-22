import {Injectable} from 'app/app';
import {SearchCriteriaUtils, CRITERIA_FTS_RULE_FIELD} from 'search/utils/search-criteria-utils';

/**
 * Filters out free text property if already present inside the search criteria tree
 *
 * @author Svetlozar Iliev
 */
@Injectable()
export class FreeTextPropertyFilter {

  /**
   * Specific filter for properties that filters out the keyword property from
   * all properties only if there is present fts criteria inside the tree or
   * there is at least one present search criteria inside the tree
   * @param config the config from which to extract the tree
   * @param property current property that is being processed
   * @returns {boolean} true if property should not be filtered, false otherwise
   */
  filter(config, criteria, property) {
    if(config.searchMediator) {
      let tree = config.searchMediator.queryBuilder.tree;
      return !(property.id === CRITERIA_FTS_RULE_FIELD && !SearchCriteriaUtils.isCriteriaTopmost(tree, criteria));
    }
    return true;
  }
}
