import {Injectable, Inject} from 'app/app';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchService} from 'services/rest/search-service';
import {DialogService} from 'components/dialog/dialog-service';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {TranslateService} from 'services/i18n/translate-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchCriteriaService} from 'services/search/search-criteria-service';

export const PICKER_RESTRICTIONS_DIALOG_HEADER = 'dialog.header.warning';
export const PICKER_RESTRICTIONS_DIALOG_MESSAGE = 'picker.restrictions.dialog.message';
export const PICKER_RESTRICTIONS_WARNING_MESSAGE = 'picker.restrictions.warning.message';

export const PICKER_RESTRICTIONS_POPOVER_TITLE = 'picker.restrictions.popover.title';
export const PICKER_RESTRICTIONS_POPOVER_BODY = 'picker.restrictions.popover.body';

/**
 * Service for manipulating given search criteria with specific restriction rules or conditions.
 *
 * @author Mihail Radkov
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(SearchService, DialogService, TranslateService, SearchCriteriaService)
export class PickerRestrictionsService {

  constructor(searchService, dialogService, translateService, searchCriteriaService) {
    this.searchService = searchService;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.searchCriteriaService = searchCriteriaService;

    this.assignSearchMediator();
    this.assignRestrictionLabels();
  }

  assignSearchMediator() {
    this.searchMediator = new SearchMediator(this.searchService);
    this.searchMediator.arguments = {properties: ['id', HEADER_DEFAULT]};
  }

  assignRestrictionLabels() {
    this.dialogHeader = this.translateService.translateInstant(PICKER_RESTRICTIONS_DIALOG_HEADER);
    this.dialogMessage = this.translateService.translateInstant(PICKER_RESTRICTIONS_DIALOG_MESSAGE);
    this.warningMessage = this.translateService.translateInstant(PICKER_RESTRICTIONS_WARNING_MESSAGE);

    this.popoverBody = this.translateService.translateInstant(PICKER_RESTRICTIONS_POPOVER_BODY);
    this.popoverTitle = this.translateService.translateInstant(PICKER_RESTRICTIONS_POPOVER_TITLE);
  }

  /**
   * Assigns the provided restriction rule or condition with rules into the given search criteria.
   * For more detailed documentation and description refer to {@SearchCriteriaUtils#assignRestrictions}
   *
   * @param criteria - the search criteria to populate restrictions with. Reference is preserved.
   * @param restrictions - a search rule or condition with rules which are assigned in the given criteria
   */
  assignRestrictionCriteria(criteria, restrictions) {
    SearchCriteriaUtils.assignRestrictions(criteria, restrictions);
  }

  /**
   * Assigns a restriction message and popover warning configuration to the
   * provided dialog config.
   *
   * @param config - a valid object on which the new message will be assigned
   * @param restrictions - a search rule or condition with restriction rules
   */
  assignRestrictionMessage(config, restrictions) {
    config.warningPopover = {
      style: {
        'max-width': 'none',
        'width': 'auto'
      },
      placement: 'bottom',
      body: this.popoverBody,
      title: this.popoverTitle
    };
    config.warningMessage = this.warningMessage;

    // translate and beautify the provided search restrictions for the popover body
    this.searchCriteriaService.translateSearchCriteria(restrictions).then(translated => {
      config.warningPopover.body = this.searchCriteriaService.stringifySearchCriteria(translated);
    });
  }

  /**
   * Filters the provided instance identifiers by applying
   * the provided restrictions to them. This method executes
   * a search query, thus returning a promise which contains
   * only the instances which are meeting the provided
   * restrictions based on the instance identifiers.
   *
   * An array of valid instance identifiers and restrictions
   * are required when operating with this method.
   *
   * @param - identifiers the list of instance identifiers to filter
   * @param - restrictions the search rule or condition with rules
   *
   * @returns - list of instances meeting the restrictions
   */
  filterByRestrictions(identifiers, restrictions) {
    identifiers = identifiers || [];
    let criteria = this.buildCriteria(identifiers, restrictions);
    this.searchMediator.queryBuilder = new QueryBuilder(criteria);

    return this.searchMediator.search().then(searchResult => {
      // return the fetched & restricted objects
      return searchResult.response.data.values;
    });
  }

  /**
   * Executes the selection handler on the given instance
   * if and only if the  instance is meeting the provided
   * restrictions. Provided handler is passed the instance
   * as it's first argument
   *
   * @param instance - the instance to be validated
   * @param restrictions - restrictions to validate the instance
   * @param handler - selection handler to execute
   */
  handleSelection(instance, restrictions, handler) {
    // check if the selected instance is actually compliant with the provided search restrictions
    this.filterByRestrictions([instance.id], restrictions).then(instances => {
      if (instances.length === 1 && instances[0].id === instance.id) {
        handler(instance);
      } else {
        this.buildDialog();
      }
    });
  }

  buildDialog() {
    this.dialogService.confirmation(this.dialogMessage, this.dialogHeader, {
      buttons: [
        this.dialogService.createButton(DialogService.CONFIRM, 'dialog.button.confirm', true)
      ]
    });
  }

  buildCriteria(identifiers, restrictions) {
    let criteria = SearchCriteriaUtils.buildCondition();
    criteria.rules.push(this.buildInstanceRule(identifiers));
    criteria.rules.push(restrictions);
    return criteria;
  }

  buildInstanceRule(identifiers) {
    return SearchCriteriaUtils.buildRule('instanceId', 'object', 'in', identifiers);
  }
}