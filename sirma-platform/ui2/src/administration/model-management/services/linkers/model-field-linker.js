import {Inject, Injectable} from 'app/app';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelControlLinker} from 'administration/model-management/services/linkers/model-control-linker';
import {ModelDescriptionLinker} from 'administration/model-management/services/linkers/model-description-linker';
import {ModelField} from 'administration/model-management/model/model-field';

/**
 * Service which builds and links fields to a given model. Model is required to support fields
 * in order to be properly linked. Created fields linked to the model are of type {@link ModelField}
 * or any of the types which might extend off of it.
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelAttributeLinker, ModelDescriptionLinker, ModelControlLinker)
export class ModelFieldLinker {

  constructor(modelAttributeLinker, modelDescriptionLinker, modelControlLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
    this.modelDescriptionLinker = modelDescriptionLinker;
    this.modelControlLinker = modelControlLinker;
  }

  /**
   * Links a collection of fields provided by a restful response to the given model. The model can be
   * of any type, as long as it supports fields. The Provided meta data is additionally linked with
   * each model field it belongs too.
   *
   * @param model - any model which is supporting fields
   * @param fields - list of fields provided by a restful service
   * @param meta - map of model meta data to be linked with the fields based on an identifier
   */
  linkFields(model, fields, meta) {
    fields.forEach(field => {
      // Create model field and link related meta info
      let fieldModel = this.constructFieldModel(field);
      this.linkAttributes(fieldModel, field.attributes, meta.getFields());
      this.linkDescriptions(fieldModel, field.labels);
      this.linkControls(fieldModel, field.controls, meta);

      if (field.regionId) {
        // link the to the belonging region
        fieldModel.setRegionId(field.regionId);
      }

      // add field to definition
      model.addField(fieldModel);
      fieldModel.setParent(model);
    });
  }

  /**
   * Creates a model field from a provided optional meta model which contains the meta
   * data information needed to construct a basic model field. The field is created and
   * is not assigned to any owner or parent. Both input parameters are optional depending
   * on the caller's requirements for the constructed model field.
   *
   * @param id - the id of the field to be created, can be left empty or null
   * @param meta - the meta data for the field, can be left empty or null
   * @returns {@link ModelField} - created instance of model field
   */
  createModelField(id, meta) {
    let fieldModel = this.constructFieldModel({id});
    this.createAttributes(fieldModel, meta);
    this.linkDescriptions(fieldModel, {});
    return fieldModel;
  }

  createAttributes(model, meta) {
    this.modelAttributeLinker.createAttributes(model, [], meta);
  }

  linkAttributes(model, attributes, meta) {
    this.modelAttributeLinker.linkAttributes(model, attributes, meta);
  }

  linkDescriptions(model, labels) {
    this.modelDescriptionLinker.insertDescriptions(model, labels);
  }

  linkControls(model, controls, meta) {
    this.modelControlLinker.linkControls(model, controls, meta);
  }

  constructFieldModel(data) {
    return new ModelField(data.id);
  }
}