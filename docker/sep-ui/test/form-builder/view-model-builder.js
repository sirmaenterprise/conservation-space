import {DefinitionModel} from 'models/definition-model';

/**
 * Example use:
 * let model = new ViewModelBuilder()
 * .addField('field1', 'EDITABLE', 'text', 'field 1', true, true, [], codelist)
 * .addField('field2', 'EDITABLE', 'text', 'field 2', true, true, [], codelist)
 * .addField('field3', 'EDITABLE', 'text', 'field 3', true, true, [], codelist)
 * .addRegion('region1', 'region 1', 'EDITABLE', false, true)
 * .addField('field4', 'EDITABLE', 'text', 'field 4', true, true, [], codelist)
 * .addField('field5', 'EDITABLE', 'text', 'field 5', true, true, [], codelist)
 * .endRegion()
 * .addField('field6', 'EDITABLE', 'text', 'field 6', true, true, [], codelist)
 * .getModel();
 *
 * let model = new ViewModelBuilder()
 * .appendField(ModelBuildUtil.createField(propertyName, 'EDITABLE', 'text', 'Title', false, false, [], undefined, undefined, false, propertyUri))
 * .getModel();
 */
export class ViewModelBuilder {

  constructor() {
    this.model = {
      'fields': []
    };
  }

  appendField(fieldModel) {
    this.model.fields.push(fieldModel);
    return this;
  }

  addField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist, multivalue, uri) {
    let fieldModel = ModelBuildUtil.createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist, multivalue, uri);
    this.model.fields.push(fieldModel);
    return this;
  }

  addRegion(id, label, displayType, mandatory, rendered, validators = []) {
    let regionModel = new RegionModelBuilder(id, label, displayType, mandatory, rendered, validators, this);
    this.model.fields.push(regionModel.getModel());
    return regionModel;
  }

  getModel() {
    return new DefinitionModel(this.model);
  }

  static createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist, multivalue) {
    return ModelBuildUtil.createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist, multivalue);
  }

  static createRegion(id, label, displayType, mandatory, rendered) {
    return ModelBuildUtil.createRegion(id, label, displayType, mandatory, rendered);
  }
}

class RegionModelBuilder {

  constructor(id, label, displayType, mandatory, rendered, validators, parent) {
    this.parent = parent;
    this.model = ModelBuildUtil.createRegion(id, label, displayType, mandatory, rendered, validators);
  }

  addField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist, multivalue) {
    let fieldModel = ModelBuildUtil.createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist, multivalue);
    this.model.fields.push(fieldModel);
    return this;
  }

  getModel() {
    return this.model;
  }

  endRegion() {
    return this.parent;
  }
}

export class ModelBuildUtil {

  static createField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist, multivalue, uri, isDataProperty = true) {
    let model = {
      identifier: id,
      previewEmpty: true,
      disabled: false,
      displayType,
      tooltip: 'tooltip',
      validators: [...validators],
      dataType,
      label,
      isMandatory: mandatory,
      maxLength: 40,
      rendered,
      codelist,
      multivalue,
      controlId: control,
      uri,
      isDataProperty
    };
    if (control) {
      model.control = [{
        identifier: control
      }];
    }
    return model;
  }

  static createRegion(id, label, displayType, mandatory, rendered, validators = []) {
    return {
      identifier: id,
      label,
      displayType,
      mandatory,
      rendered,
      fields: [],
      validators: [...validators]
    };
  }
}
