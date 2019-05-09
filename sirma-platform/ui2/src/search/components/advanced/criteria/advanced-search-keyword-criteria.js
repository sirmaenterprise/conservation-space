import {Component, View} from 'app/app';
import {Configurable} from 'components/configurable';
import {FTS_CHANGE_EVENT} from 'search/components/search';
import template from './advanced-search-keyword-criteria.html!text';

@Component({
  selector: 'seip-advanced-search-keyword-criteria',
  properties: {
    'config': 'config',
    'criteria': 'criteria',
    'property': 'property'
  }
})
@View({template})
export class AdvancedSearchKeywordCriteria extends Configurable {

  constructor() {
    super({
      disabled: false
    });
  }

  onChange() {
    this.config.searchMediator.trigger(FTS_CHANGE_EVENT, this.criteria.value);
  }
}
