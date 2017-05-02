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
 */
export class ViewModelBuilder {

  constructor() {
    this.model = {
      'fields': []
    };
  }

  addField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist) {
    let fieldModel = ModelBuildUtil.createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist);
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

  static createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist) {
    return ModelBuildUtil.createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist);
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

  addField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist) {
    let fieldModel = ModelBuildUtil.createField(id, displayType, dataType, label, mandatory, rendered, validators, control, codelist);
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

class ValidatorModelBuilder {

  constructor(id, args) {

  }
}

class ModelBuildUtil {

  static createField(id, displayType, dataType, label, mandatory, rendered, validators = [], control, codelist) {
    let model = {
      'identifier': id,
      'previewEmpty': true,
      'disabled': false,
      'displayType': displayType,
      'tooltip': 'tooltip',
      'validators': [...validators],
      'dataType': dataType,
      'label': label,
      'isMandatory': mandatory,
      'maxLength': 40,
      'rendered': rendered,
      'codelist': codelist
    };
    if (control) {
      model.control = {
        'identifier': control
      }
    }
    return model;
  }

  static createRegion(id, label, displayType, mandatory, rendered, validators = []) {
    return {
      'identifier': id,
      'label': label,
      'displayType': displayType,
      'mandatory': mandatory,
      'rendered': rendered,
      'fields': [],
      'validators': [...validators]
    }
  }
}
