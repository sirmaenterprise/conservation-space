import {Component, View, Inject} from 'app/app';
import formattedSelectTemplateStub from 'formatted-select-template!text';
import {Select} from 'components/select/select';

@Component({
  selector: 'seip-formatted-select-stub'
})
@View({
  template: formattedSelectTemplateStub
})

export class FormattedSelectStub {
  constructor() {
    this.selectConfig = {
      data: [{id: 'key1', text: 'VaLuE'}],
      defaultValue: 'key1',
      escapeMarkup: function (val) {
        return val;
      },
      formatResult: function (item) {
        return "<i>" + item.text + "</i>";
      },
      formatSelection: function (item) {
        return "<b>" + item.text + "</b>";
      }
    }
  }
}