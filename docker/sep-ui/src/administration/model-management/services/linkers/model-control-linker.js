import {Inject, Injectable} from 'app/app';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelControlParamLinker} from 'administration/model-management/services/linkers/model-control-param-linker';
import {ModelControl} from 'administration/model-management/model/model-control';
import {ModelControlMetaData} from 'administration/model-management/meta/model-control-meta';

import _ from 'lodash';

/**
 * Service which builds and links controls to a given field model.
 * Created controls linked to the field are of type {@link ModelControl}.
 *
 * @author svelikov
 */
@Injectable()
@Inject(ModelControlParamLinker, ModelAttributeLinker)
export class ModelControlLinker {

  constructor(modelControlParamLinker, modelAttributeLinker) {
    this.modelControlParamLinker = modelControlParamLinker;
    this.modelAttributeLinker = modelAttributeLinker;
  }

  /**
   * Links a collection of controls provided by a restful response to the given model field. The provided meta data is
   * additionally linked with each model control it belongs to.
   *
   * @param model - a reference to ModelField
   * @param controls - list of controls provided by a restful service
   * @param meta - map of model meta data to be linked with the control based on an identifier
   */
  linkControls(model, controls, meta) {
    controls.forEach(control => {
      let modelControl = this.constructModelControl(control);
      this.modelAttributeLinker.linkAttributes(modelControl, control.attributes, meta.getControls());
      this.fillMissingControlParams(control, meta.getControls().getModel(ModelControlMetaData.ID).getControlOptions());
      this.modelControlParamLinker.linkControlParams(modelControl, control.controlParams, meta);

      modelControl.setParent(model);
      model.addControl(modelControl);
    });
  }

  createModelControl(id, meta) {
    let modelControl = this.constructModelControl({id});
    let attributes = this.buildControlAttributes(id, meta.getControls());

    this.modelAttributeLinker.createAttributes(modelControl, attributes, meta.getControls());
    this.modelControlParamLinker.linkControlParams(modelControl, [], meta.getControls());
    return modelControl;
  }

  buildControlAttributes(id, meta) {
    return meta.getModels().map(param => this.buildControlAttribute(param, id));
  }

  buildControlAttribute(param, id) {
    return {
      name: param.id,
      value: id,
      type: param.type
    };
  }

  constructModelControl(data) {
    return new ModelControl(data.id);
  }

  fillMissingControlParams(control, options) {
    _.result(_.find(options, (option) => {
      return option.id === control.id;
    }), ModelControlMetaData.PARAMS, []).forEach(attribute => {
      let existingAttrinbute = _.find(control.controlParams, (param) => {
        return attribute.id === param.id;
      });
      if (!existingAttrinbute) {
        control.controlParams.push({id: attribute.id, attributes: []});
      }
    });
  }
}
