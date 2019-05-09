PluginRegistry.add('model-management-field-controls', [
  {
    'order': 10,
    'type': 'DEFAULT_VALUE_PATTERN',
    'supportedBy': ['ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE', 'FLOATING_POINT_TYPE', 'FLOATING_POINT_FIXED_TYPE', 'NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE', 'DATE_TYPE', 'DATETIME_TYPE', 'BOOLEAN', 'CODELIST', 'URI'],
    'name': 'model-default-value-pattern-control',
    'component': 'model-default-value-pattern-control',
    'module': 'administration/model-management/components/field/control/default-value-pattern/model-default-value-pattern-control',
    'label': 'administration.models.management.field.controls.default_value_pattern.label',
    'link': 'administration.models.management.field.controls.default_value_pattern.link',
    'tooltip': 'administration.models.management.field.controls.default_value_pattern.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.default_value_pattern.link.tooltip'
  },
  {
    'order': 20,
    'type': 'RICHTEXT',
    'supportedBy': ['ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE'],
    'name': 'model-richtext-control',
    'component': 'model-richtext-control',
    'module': 'administration/model-management/components/field/control/richtext/model-richtext-control',
    'label': 'administration.models.management.field.controls.richtext.label',
    'link': 'administration.models.management.field.controls.richtext.link',
    'tooltip': 'administration.models.management.field.controls.richtext.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.richtext.link.tooltip'
  },
  {
    'order': 30,
    'type': 'PICKER',
    'supportedBy': ['URI'],
    'name': 'model-picker-control',
    'component': 'model-picker-control',
    'module': 'administration/model-management/components/field/control/picker/model-picker-control',
    'label': 'administration.models.management.field.controls.picker.label',
    'link': 'administration.models.management.field.controls.picker.link',
    'tooltip': 'administration.models.management.field.controls.picker.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.picker.link.tooltip'
  },
  {
    'order': 40,
    'type': 'RELATED_FIELDS',
    'supportedBy': ['CODELIST'],
    'name': 'model-related-fields-control',
    'component': 'model-related-fields-control',
    'module': 'administration/model-management/components/field/control/related-fields/model-related-fields-control',
    'label': 'administration.models.management.field.controls.related_fields.label',
    'link': 'administration.models.management.field.controls.related_fields.link',
    'tooltip': 'administration.models.management.field.controls.related_fields.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.related_fields.link.tooltip'
  },
  {
    'order': 50,
    'type': 'CONCEPT_PICKER',
    'supportedBy': ['URI'],
    'name': 'model-concept-picker-control',
    'component': 'model-concept-picker-control',
    'module': 'administration/model-management/components/field/control/concept-picker/model-concept-picker-control',
    'label': 'administration.models.management.field.controls.concept_picker.label',
    'link': 'administration.models.management.field.controls.concept_picker.link',
    'tooltip': 'administration.models.management.field.controls.concept_picker.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.concept_picker.link.tooltip'
  },
  {
    'order': 60,
    'type': 'BYTE_FORMAT',
    'supportedBy': ['NUMERIC_TYPE', 'NUMERIC_FIXED_TYPE'],
    'name': 'model-byte-format-control',
    'component': 'model-byte-format-control',
    'module': 'administration/model-management/components/field/control/byte-format/model-byte-format-control',
    'label': 'administration.models.management.field.controls.byte_format.label',
    'link': 'administration.models.management.field.controls.byte_format.link',
    'tooltip': 'administration.models.management.field.controls.byte_format.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.byte_format.link.tooltip'
  },
  {
    'order': 70,
    'type': 'OBJECT_TYPE_SELECT',
    'supportedBy': ['ALPHA_NUMERIC_TYPE', 'ALPHA_NUMERIC_WITH_CONSTRAINTS_TYPE', 'ALPHA_NUMERIC_FIXED_TYPE'],
    'immutable': true,
    'name': 'model-object-type-select-control',
    'component': 'model-object-type-select-control',
    'module': 'administration/model-management/components/field/control/object-type-select/model-object-type-select-control',
    'label': 'administration.models.management.field.controls.object_type_select.label',
    'link': 'administration.models.management.field.controls.object_type_select.link',
    'tooltip': 'administration.models.management.field.controls.object_type_select.tooltip',
    'linkTooltip': 'administration.models.management.field.controls.object_type_select.link.tooltip'
  }
]);