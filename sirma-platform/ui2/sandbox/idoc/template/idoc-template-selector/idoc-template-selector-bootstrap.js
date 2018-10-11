import {Component, View, Inject} from 'app/app';
import 'idoc/template/idoc-template-selector';
import {InstanceObject} from 'models/instance-object';
import template from './idoc-template-selector-bootstrap.html!text';

@Component({
  selector: 'idoc-template-selector-bootstrap'
})
@View({
  template: template
})
@Inject()
export class IdocTemplateSelectorBootstrap {

  constructor() {
    this.purpose = 'creatable';

    this.models = {
      validationModel: {
        active: {
          dataType: 'boolean',
          value: true
        }
      },
      viewModel: {
        fields: [{
          dataType: 'boolean',
          identifier: 'active',
          displayType: 'EDITABLE',
          isMandatory: true
        }]
      },
      definitionId: 'document'
    };
    this.instanceObject = this.getCurrentObjectMock();
  }

  getCurrentObjectMock() {
    var object = new InstanceObject('instanceId',this.models );
    return object;
  }
}