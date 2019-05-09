import {View, Component, Inject, NgScope} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';

import {ModelPrimitives} from 'administration/model-management/model/model-primitives';
import {ModelPropertyMetaData} from 'administration/model-management/meta/model-property-meta';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelGenericAttribute} from 'administration/model-management/components/attributes/model-generic-attribute';
import _ from 'lodash';

import 'administration/model-management/components/attributes/select/model-select-attribute';

import template from './model-field-type-attribute.html!text';

/**
 * Component rendering a typeOption dropdown.
 * Attribute model is provided through a component property and should be of type {@link ModelSingleAttribute}.
 *
 * @author Stella Djulgerova, Svetlozar Iliev
 */
@Component({
  selector: 'model-field-type-attribute',
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
@Inject(NgScope, TranslateService)
export class ModelFieldTypeAttribute extends ModelGenericAttribute {

  constructor($scope, translateService) {
    super();
    this.$scope = $scope;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.initSelectConfig();
    this.initPropertyWatcher();
  }

  onValue() {
    return this.onChange();
  }

  initSelectConfig() {
    this.selectConfig = _.defaults({}, this.config);
  }

  initSelectData(data) {
    this.selectConfig.data = data;
  }

  initPropertyWatcher() {
    this.$scope.$watch(() => this.context.getProperty(), () => {
      this.resolveTypeOptions();
    });
    this.subscribe(this.getPropertyTypeAttribute(), () => {
      this.clearAttributeValue();
      this.resolveTypeOptions();
    });
    this.subscribe(this.getPropertyRangeAttribute(), () => {
      this.clearAttributeValue();
      this.resolveTypeOptions();
    });
  }

  resolveTypeOptions() {
    let property = this.context.getProperty();
    let type = this.getPropertyTypeValue(property);
    let range = this.getPropertyRangeValue(property);
    let value = this.attribute.getValue().getValue();
    this.initSelectData(this.getOptions(type, range, value));
  }

  getOptions(type, range, value) {
    if (!type || !type.length) {
      return [];
    }
    let source = ModelFieldTypeAttribute.TYPE_GROUPS[type];

    if (!range || !range.length) {
      // collect all types for the given type (object or data)
      let types = _.uniq(_.flatten(_.map(Object.values(source))));

      // build the options based on the collected types
      return this.transformOptions().filter(option => {
        return types.indexOf(option.id) > -1;
      });
    } else {
      // when an object property is present reassign the range to generic xsd object rng
      range = this.isObjectProperty(type) ? ModelFieldTypeAttribute.XSD_ENTITY : range;

      return this.transformOptions().filter(option => {
        // If range is not in groups map (for example http://www.w3.org/2001/XMLSchema#literal) then get the option
        // which match current type
        if (!source || !source[range]) {
          return value === option.id;
        } else {
          // If range is available in groups map get option value from map
          return source[range].indexOf(option.id) > -1;
        }
      });
    }
  }

  transformOptions() {
    return this.attribute.getMetaData().getOptions().map(option => {
      return {id: option.value, text: this.translateService.translateInstant(option.label)};
    });
  }

  getPropertyTypeValue() {
    return this.getAttributeValue(this.getPropertyTypeAttribute());
  }

  getPropertyTypeAttribute() {
    return this.getAttribute(this.context.getProperty(), ModelAttribute.PROPERTY_TYPE);
  }

  getPropertyRangeValue() {
    return this.getAttributeValue(this.getPropertyRangeAttribute());
  }

  getPropertyRangeAttribute() {
    return this.getAttribute(this.context.getProperty(), ModelAttribute.RANGE_ATTRIBUTE);
  }

  getAttributeValue(attribute) {
    return attribute.getValue().getValue();
  }

  getAttribute(target, attribute) {
    return target.getAttribute(attribute);
  }

  isObjectProperty(type) {
    return type === ModelPropertyMetaData.OBJECT_PROPERTY;
  }
}

ModelFieldTypeAttribute.XSD_ENTITY = 'http://www.w3.org/2001/XMLSchema#entity';
ModelFieldTypeAttribute.XSD_LONG = 'http://www.w3.org/2001/XMLSchema#long';
ModelFieldTypeAttribute.XSD_DOUBLE = 'http://www.w3.org/2001/XMLSchema#double';
ModelFieldTypeAttribute.XSD_FLOAT = 'http://www.w3.org/2001/XMLSchema#float';
ModelFieldTypeAttribute.XSD_INT = 'http://www.w3.org/2001/XMLSchema#int';
ModelFieldTypeAttribute.XSD_DATE = 'http://www.w3.org/2001/XMLSchema#date';
ModelFieldTypeAttribute.XSD_DATETIME = 'http://www.w3.org/2001/XMLSchema#dateTime';
ModelFieldTypeAttribute.XSD_STRING = 'http://www.w3.org/2001/XMLSchema#string';
ModelFieldTypeAttribute.XSD_BOOLEAN = 'http://www.w3.org/2001/XMLSchema#boolean';

ModelFieldTypeAttribute.TYPE_GROUPS = {
  [ModelPropertyMetaData.OBJECT_PROPERTY]: {
    [ModelFieldTypeAttribute.XSD_ENTITY]: [
      ModelPrimitives.URI
    ]
  },
  [ModelPropertyMetaData.DATA_PROPERTY]: {
    [ModelFieldTypeAttribute.XSD_LONG]: [
      ModelPrimitives.NUMERIC_TYPE,
      ModelPrimitives.NUMERIC_FIXED_TYPE,
      ModelPrimitives.FLOATING_POINT_TYPE,
      ModelPrimitives.FLOATING_POINT_FIXED_TYPE
    ],
    [ModelFieldTypeAttribute.XSD_DOUBLE]: [
      ModelPrimitives.NUMERIC_TYPE,
      ModelPrimitives.NUMERIC_FIXED_TYPE,
      ModelPrimitives.FLOATING_POINT_TYPE,
      ModelPrimitives.FLOATING_POINT_FIXED_TYPE
    ],
    [ModelFieldTypeAttribute.XSD_FLOAT]: [
      ModelPrimitives.FLOATING_POINT_TYPE,
      ModelPrimitives.FLOATING_POINT_FIXED_TYPE
    ],
    [ModelFieldTypeAttribute.XSD_INT]: [
      ModelPrimitives.NUMERIC_TYPE,
      ModelPrimitives.NUMERIC_FIXED_TYPE
    ],
    [ModelFieldTypeAttribute.XSD_DATE]: [
      ModelPrimitives.DATE_TYPE
    ],
    [ModelFieldTypeAttribute.XSD_DATETIME]: [
      ModelPrimitives.DATE_TYPE,
      ModelPrimitives.DATETIME_TYPE
    ],
    [ModelFieldTypeAttribute.XSD_STRING]: [
      ModelPrimitives.ALPHA_NUMERIC_TYPE,
      ModelPrimitives.ALPHA_NUMERIC_FIXED_TYPE,
      ModelPrimitives.ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE,
      ModelPrimitives.CODELIST
    ],
    [ModelFieldTypeAttribute.XSD_BOOLEAN]: [
      ModelPrimitives.BOOLEAN
    ]
  }
};


