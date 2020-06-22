import {Described} from 'administration/model-management/model/described';
import {ModelValidationModel} from 'administration/model-management/model/validation/model-validation-model';

/**
 * Provides a description of a meta data which is used as a reference point.
 * Meta data stores arbitrary properties or attributes which are used to
 * provide the behaviour of a given model. Every model can be associated with
 * some type of meta data.
 *
 * @author Mihail Radkov
 * @author Svetlozar Iliev
 */
export class ModelMetaData extends Described {

  constructor(id) {
    super();
    this.id = id;
    this.validationModel = new ModelValidationModel();
  }

  getId() {
    return this.id;
  }

  setId(id) {
    this.id = id;
    return this;
  }

  getValidationModel() {
    return this.validationModel;
  }

  seal() {
    this.validationModel.seal();
    Object.freeze(this);
  }
}