import {USER_DASHBOARD, IDOC_STATE} from 'idoc/idoc-constants';
import {OPEN_SEARCH_STATE} from 'search/components/quick-search';

const RESET_STATES = [USER_DASHBOARD];
const IGNORE_STATES = [IDOC_STATE, OPEN_SEARCH_STATE];

export class BreadcrumbStateHandler {

  /**
   * Checks if a given state is a reset state. A reset state is such
   * that a breadcrumb & it's contents are required to be reset.
   *
   * @param state the state to be checked
   */
  static isResetState(state) {
    return BreadcrumbStateHandler.hasState(RESET_STATES, state);
  }

  /**
   * Checks if a given stata is an ignore state. An ignore state is
   * such state that wont be registered with the breadcrumb.
   *
   * @param state the state to be checked
   */
  static isIgnoreState(state) {
    return BreadcrumbStateHandler.hasState(IGNORE_STATES, state);
  }

  /**
   * Checks if a state is present inside an array of provided states
   *
   * @param states the array of provided states
   * @param state the state to be checked
   */
  static hasState(states, state) {
    return !!states && states.indexOf(state) >= 0;
  }
}
