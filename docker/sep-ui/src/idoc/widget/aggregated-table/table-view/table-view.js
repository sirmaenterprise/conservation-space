import {Component, View, Inject, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import 'form-builder/form-wrapper';
import tableViewTemplate from 'idoc/widget/aggregated-table/table-view/table-view.html!text';
import './table-view.css!';

@Component({
  selector: 'seip-table-view',
  properties: {
    config: 'config',
    formConfig: 'form-config',
    headers: 'headers',
    total: 'total'
  }
})
@View({
  template: tableViewTemplate
})
@Inject(NgScope)
export class TableView extends Configurable {
  constructor($scope) {
    super({});
    this.$scope = $scope;
  }

  getColumnHeaderLabel(columnHeader) {
    return columnHeader.labels.join(', ');
  }
}
