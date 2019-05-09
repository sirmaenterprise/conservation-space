import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';

/**
 * Represents a specific property meta data extending from {@link ModelAttributeMetaData}
 *
 * @author Svetlozar Iliev
 */
export class ModelPropertyMetaData extends ModelAttributeMetaData {

  constructor(id) {
    super(id);
  }
}

ModelPropertyMetaData.DATA_PROPERTY = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DefinitionDataProperty';
ModelPropertyMetaData.OBJECT_PROPERTY = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#DefinitionObjectProperty';
