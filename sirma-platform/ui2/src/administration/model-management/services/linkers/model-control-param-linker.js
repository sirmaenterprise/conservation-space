import {Injectable, Inject} from 'app/app';
import {ModelControlParam} from 'administration/model-management/model/model-control-param';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';

/**
 * Service which builds and links controls params to a given control model.
 * Created controls params linked to the control are of type {@link ModelControlParam}.
 *
 * @author svelikov
 */
@Injectable()
@Inject(ModelAttributeLinker)
export class ModelControlParamLinker {

  constructor(modelAttributeLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
  }

  /**
   * Links a collection of control params provided by a restful response to the given model field. The provided
   * meta data is additionally linked with each model control param it belongs to.
   *
   * @param model - reference to ModelControl
   * @param controlParams - list of control params provided by a restful service
   * @param meta - map of model meta data to be linked with the control params based on an identifier
   */
  linkControlParams(model, controlParams, meta) {
    controlParams.forEach(param => {
      let modelControlParam = this.constructModelControlParam(param);
      this.modelAttributeLinker.linkAttributes(modelControlParam, param.attributes, meta.getControlParams());

      modelControlParam.setParent(model);
      model.addControlParam(modelControlParam);
    });
  }

  createModelControlParam(attributes, meta) {
    let modelControlParam = this.constructModelControlParam(attributes);
    let attributesArray = this.buildControlParamAttributes(meta.getControlParams(), attributes);

    this.modelAttributeLinker.createAttributes(modelControlParam, attributesArray, meta.getControlParams());
    return modelControlParam;
  }

  buildControlParamAttributes(meta, attributes) {
    return meta.getModels().map(param => this.buildControlParamAttribute(param, attributes));
  }

  buildControlParamAttribute(param, attributes) {
    return {
      name: param.id,
      value: attributes[param.id] || attributes.defaultValue || '',
      type: param.type
    };
  }

  constructModelControlParam(data) {
    return new ModelControlParam(data.id);
  }
}