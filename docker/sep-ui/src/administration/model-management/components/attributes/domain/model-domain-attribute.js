import {View, Component, Inject, NgScope} from 'app/app';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import template from './model-domain-attribute.html!text';

/**
 * Component responsible for resolving and rendering a domain attribute. Domain attribute is a drop down
 * attribute which takes care of visualizing all models which belong to the domain of the owner of the
 * provided attribute as component property to this view.
 *
 * @author Svetlozar Iliev
 */
@Component({
  selector: 'model-domain-attribute',
  properties: {
    'config': 'config',
    'context': 'context',
    'editable': 'editable',
    'attribute': 'attribute'
  },
  events: ['onChange']
})
@View({
  template
})
@Inject(NgScope)
export class ModelDomainAttribute extends ModelGenericAttribute {

  constructor($scope) {
    super();
    this.$scope = $scope;
  }

  ngOnInit() {
    this.initSelectConfig();
    this.initSelectOptions();
    this.initDomainValueWatcher();
  }

  onValue() {
    return this.onChange();
  }

  initDomainValueWatcher() {
    this.$scope.$watch(() => this.getDomainValue(), (newValue, oldValue) => {
      newValue !== oldValue && this.initSelectOptions();
    });
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({
      appendMissing: true
    }, this.config);
  }

  initSelectOptions() {
    this.selectConfig.data = this.getDomainData();
  }

  getDomainData() {
    return [this.getModel(), ...this.getModel().getParents()].map(this.getItem.bind(this));
  }

  getDomainValue() {
    return this.attribute.getValue().getValue();
  }

  getItem(item) {
    return {
      id: item.getId(),
      text: item.getDescription().getValue()
    };
  }

  getModel() {
    return this.context.getParent();
  }
}