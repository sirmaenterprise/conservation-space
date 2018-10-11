import {Filter} from 'app/app';

/**
 * Filter for paging a collection of items.
 *
 * @author Mihail Radkov
 */
@Filter
export class Paginate {

  /**
   * Performs the pagination upon the provided collection with the given pagination config.
   * Example config:
   * {
   *   page: 1,
   *   pageSize: 5
   * }
   * The configuration fields are mandatory & should be correctly calculated.
   *
   * @param collection - the collection to paginate
   * @param config - configuration for the pagination
   * @return subset of items or empty collection if the provided one is undefined
   */
  filter(collection, config) {
    if (!collection) {
      return [];
    }
    let start = (config.page - 1) * config.pageSize;
    let end = start + config.pageSize;
    return collection.slice(start, end);
  }

}
