import {Component, View} from 'app/app';
import {ObjectTypeSelect} from 'components/select/object/object-type-select';
import {UrlUtils} from 'common/url-utils';
import selectTemplateStub from 'select-template!text';

@Component({
  selector: 'seip-object-type-select-stub'
})
@View({
  template: selectTemplateStub
})
export class ObjectTypeSelectStub {

  constructor() {
    this.buildConfig();
  }

  buildConfig() {
    var hash = '?' + window.location.hash.substring(2);
    var disableTypesWithoutDefinition = !!UrlUtils.getParameter(hash, 'disableTypesWithoutDefinition');

    this.config = {
      predefinedData: [{
        id: 'anyObject',
        text: 'Any Object'
      }],
      multiple: false,
      defaultValue: 'anyObject',
      disableTypesWithoutDefinition: disableTypesWithoutDefinition
    }
  }
}