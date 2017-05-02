import {Component, View, Inject} from 'app/app';
import {Configurable} from 'components/configurable';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {
  SELECT_OBJECT_MANUALLY,
} from 'idoc/widget/object-selector/object-selector';
import template from './add-relation-panel.html!text';

@Component({
  selector: 'seip-add-relation-panel',
  properties: {
    'config': 'config',
    'context': 'context'
  }
})
@View({
  template: template
})
@Inject()
export class AddRelationPanel extends Configurable {
  constructor() {
    super({
      renderOptions : false,
      selectObjectMode: SELECT_OBJECT_MANUALLY,
      criteriaType: SearchCriteriaUtils.MIXED_MODE
    });

    this.config.onObjectSelectorChanged = (onSelectorChangedPayload) => {
      delete this.config.selectedItemsIds;
      let selectedItems = onSelectorChangedPayload.selectedItems;
      if (selectedItems) {
        this.config.selectedItemsIds = selectedItems.map((value)=> {
          return value.id;
        });
      }
    };
  }
}