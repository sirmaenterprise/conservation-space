import {View, Component, Inject, NgElement} from 'app/app';
import {ConceptService} from 'services/rest/concept-service';
import 'components/select/tree-select/tree-select';
import template from './concept-picker.html!text';

/**
 * Allows selection of concepts from a given schema
 *
 */
@Component({
  selector: 'seip-concept-picker',
  properties: {
    'scheme': 'scheme',
    'broader': 'broader',
    'multiple': 'multiple',
    'ngModel': 'ngModel'
  },
  events: ['onChange']
})
@View({
  template: template
})
@Inject(ConceptService)
export class ConceptPicker {

  constructor(conceptService) {
    this.conceptService = conceptService;
  }

  ngOnInit() {
    this.value = this.ngModel;

    this.conceptService.getConceptHierarchy(this.scheme, this.broader).then(result => {
      let multiple = this.multiple;
      let data = this.convertToTreeSelectModel(result);

      this.treeSelectConfig = {
        data,
        multiple
      };
    });
  }

  convertToTreeSelectModel(data) {
    return data.map(element => {
      let result = {
        id: element.id,
        text: element.title
      };

      if (element.ancestors) {
        result.children = this.convertToTreeSelectModel(element.ancestors);
      }

      return result;
    });
  }

  onSelectionChanged() {
    // passing the value to the onchange event is required because for some reason the onchange event is called
    // before the model gets updated. This happens for pass-through ng-model.
    let newValue = this.value;
    this.ngModel = newValue;

    this.onChange({
      event: {
        value: newValue
      }
    });
  }

}