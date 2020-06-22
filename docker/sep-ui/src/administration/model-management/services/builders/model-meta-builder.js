import {Inject, Injectable} from 'app/app';
import {ModelsMetaData} from 'administration/model-management/meta/models-meta';
import {ModelMetaDataLinker} from 'administration/model-management/services/linkers/model-meta-linker';

import {ModelFieldMetaData} from 'administration/model-management/meta/model-field-meta';
import {ModelRegionMetaData} from 'administration/model-management/meta/model-region-meta';
import {ModelControlMetaData} from 'administration/model-management/meta/model-control-meta';
import {ModelControlParamMetaData} from 'administration/model-management/meta/model-control-param-meta';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelPropertyMetaData} from 'administration/model-management/meta/model-property-meta';
import {ModelHeaderMetaData} from 'administration/model-management/meta/model-header-meta';
import {ModelActionGroupMetaData} from 'administration/model-management/meta/model-action-group-meta';
import {ModelActionMetaData} from 'administration/model-management/meta/model-action-meta';
import {ModelActionExecutionMetaData} from 'administration/model-management/meta/model-action-execution-meta';

/**
 * Service which builds a model meta data for fields, properties, regions and attributes from a provided restful
 * response. The constructed meta data is of type {@link ModelsMetaData}
 *
 * @author Svetlozar Iliev
 */
@Injectable()
@Inject(ModelMetaDataLinker)
export class ModelMetaDataBuilder {

  constructor(modelMetaDataLinker) {
    this.modelMetaDataLinker = modelMetaDataLinker;
  }

  /**
   * Builds a model meta data for different type of models, such as - fields, regions, attributes. Meta data
   * provides additional information about a given type of model. Created model meta data built by this
   * method is of type {@link ModelsMetaData}
   *
   * @param meta - data often provided by a restful service
   * @returns {ModelsMetaData} - the constructed models meta data
   */
  buildMetaData(meta) {
    let modelsMetaData = new ModelsMetaData();
    meta.fields.forEach(field => modelsMetaData.addField(this.constructFieldData(field)));
    meta.regions.forEach(region => modelsMetaData.addRegion(this.constructRegionData(region)));
    meta.controls.forEach(control => modelsMetaData.addControl(this.constructControlData(control)));
    meta.controlParams.forEach(controlParam => modelsMetaData.addControlParam(this.constructControlParamData(controlParam)));
    meta.headers.forEach(header => modelsMetaData.addHeader(this.constructHeaderData(header)));
    meta.properties.forEach(prop => modelsMetaData.addProperty(this.constructPropertyData(prop)));
    meta.semantics.forEach(clazz => modelsMetaData.addSemantic(this.constructAttributeData(clazz)));
    meta.definitions.forEach(def => modelsMetaData.addDefinition(this.constructAttributeData(def)));
    meta.actionGroups.forEach(actionGroup => modelsMetaData.addActionGroup(this.constructActionGroupsMetaData(actionGroup)));
    meta.actions.forEach(action => modelsMetaData.addAction(this.constructActionsMetaData(action)));
    meta.actionExecutions && meta.actionExecutions.forEach(actionExecution => modelsMetaData.addActionExecution(this.constructActionExecutionMetaData(actionExecution)));
    modelsMetaData.seal();
    return modelsMetaData;
  }

  constructFieldData(data) {
    let metaData = new ModelFieldMetaData(data.id);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }

  constructControlData(data) {
    let metaData = new ModelControlMetaData(data.id).setControlOptions(data.controlOptions);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }

  constructControlParamData(data) {
    let metaData = new ModelControlParamMetaData(data.id);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }

  constructAttributeData(data) {
    let id = data.uri || data.id || data.name;
    let metaData = new ModelAttributeMetaData(id);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }

  constructPropertyData(data) {
    let metaData = new ModelPropertyMetaData(data.uri);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }

  constructRegionData(data) {
    let metaData = new ModelRegionMetaData(data.id);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }

  constructActionGroupsMetaData(actionGroup) {
    let actionGroupsMetaData = new ModelActionGroupMetaData(actionGroup.id);
    this.modelMetaDataLinker.linkMetaData(actionGroupsMetaData, actionGroup);
    return actionGroupsMetaData;
  }

  constructActionsMetaData(action) {
    let actionMetaData = new ModelActionMetaData(action.id);
    this.modelMetaDataLinker.linkMetaData(actionMetaData, action);
    return actionMetaData;
  }

  constructActionExecutionMetaData(actionExecution) {
    let actionExecutionMetaData = new ModelActionExecutionMetaData(actionExecution.id);
    this.modelMetaDataLinker.linkMetaData(actionExecutionMetaData, actionExecution);
    return actionExecutionMetaData;
  }

  constructHeaderData(data) {
    let metaData = new ModelHeaderMetaData(data.id);
    this.modelMetaDataLinker.linkMetaData(metaData, data);
    return metaData;
  }
}