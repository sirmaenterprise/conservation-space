import {View, Component, Inject, NgElement} from 'app/app';
import {FormControl} from 'form-builder/form-control';
import {ModelUtils} from 'models/model-utils';
import {MODE_EDIT, MODE_PREVIEW} from 'idoc/idoc-constants';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {MULTIPLE_SELECTION, SINGLE_SELECTION} from 'search/search-selection-modes';
import {InstanceRestService} from 'services/rest/instance-service';
import {HeadersService} from 'instance-header/headers-service';

import 'components/concept-picker/concept-picker';
import 'components/instance-selector/instance-selector';
import template from './concept-control.html!text';

@Component({
  selector: 'seip-concept-control',
  properties: {
    'formWrapper': 'form-wrapper',
    'identifier': 'identifier'
  }
})
@View({
  template: template
})
@Inject(InstanceRestService, NgElement, HeadersService)
export class ConceptControl extends FormControl {

  constructor(instanceRestService, $element, headersService) {
    super();
    this.$element = $element;
    this.headersService = headersService;
    this.instanceRestService = instanceRestService;
  }

  ngOnInit() {
    let controlParams = ModelUtils.getControl(this.fieldViewModel.control, this.fieldViewModel.controlId).controlParams;
    this.scheme = controlParams.scheme;
    this.broader = controlParams.broader;

    this.validationProperty = this.validationModel[this.fieldViewModel.identifier];

    if (!this.validationProperty.value) {
      this.validationProperty.value = ModelUtils.getEmptyObjectPropertyValue();
    }

    this.value = this.validationProperty.value.results;

    this.instanceSelectorConfig = {
      mode: this.fieldViewModel.preview ? MODE_PREVIEW : MODE_EDIT,
      instanceHeaderType: this.widgetConfig.instanceLinkType,
      propertyName: this.fieldViewModel.identifier,
      objectId: this.objectId,
      selection: this.fieldViewModel.multivalue ? MULTIPLE_SELECTION : SINGLE_SELECTION
    };
  }

  onChange(value) {
    let selection = value;
    let isSingleValue = !this.fieldViewModel.multivalue;
    if (!selection) {
      selection = [];
    } else if (isSingleValue) {
      selection = [selection];
    }

    ModelUtils.updateObjectPropertyValue(this.validationProperty, isSingleValue, selection);

    this.loadHeaders(selection);
  }

  loadHeaders(selection) {
    if (!this.validationProperty.value.headers) {
      this.validationProperty.value.headers = {};
    }

    // find out concepts without loaded headers
    let conceptsWithoutHeaders = selection.filter((id) => {
      return !this.validationProperty.value.headers[id];
    });

    return this.headersService.loadHeaders(conceptsWithoutHeaders, this.widgetConfig.instanceLinkType, this.validationProperty.value.headers);
  }

}