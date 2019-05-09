import {Inject, Injectable} from 'app/app';
import {ModelAttributeLinker} from 'administration/model-management/services/linkers/model-attribute-linker';
import {ModelHeader} from 'administration/model-management/model/model-header';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';

import _ from 'lodash';

const HEADER_TYPE = ModelAttribute.HEADER_TYPE_ATTRIBUTE;

/**
 * Service which builds and links headers to a given model.
 *
 * @author svelikov
 */
@Injectable()
@Inject(ModelAttributeLinker)
export class ModelHeaderLinker {

  constructor(modelAttributeLinker) {
    this.modelAttributeLinker = modelAttributeLinker;
  }

  /**
   * Links headers provided by a restful response to the given model. The Provided meta data is additionally linked with
   * each header model it belongs too. Also models are created for missing headers too, because we want to see every
   * header type panel.
   *
   * @param modelDefinition - Instance of {@link ModelDefinition}
   * @param headers - map with headers provided by a restful service
   * @param meta - map of model meta data to be linked with the headers
   */
  linkHeaders(modelDefinition, headers, meta) {
    let modelHeaders = headers || [];
    let modelHeadersMap = this.getHeadersMap(modelHeaders);

    this.getHeaderTypes(meta).forEach((type) => {
      let header = this.constructHeaderModel(type);
      let attributes = this.getHeaderAttributes(type, modelHeadersMap);
      this.modelAttributeLinker.linkAttributes(header, attributes, meta);
      header.getAttribute(HEADER_TYPE).getValue().setValue(type).setDirty(false);

      header.setParent(modelDefinition);
      modelDefinition.addHeader(header);
    });
  }

  constructHeaderModel(headerId) {
    return new ModelHeader(headerId);
  }

  getHeadersMap(headers) {
    return _.transform(headers, (map, header) => map[header.id] = header, {});
  }

  getHeaderAttributes(headerId, modelHeadersMap) {
    let header = modelHeadersMap[headerId];
    return header ? header.attributes : [];
  }

  getHeaderTypes(headersMetaModel) {
    let headerType = headersMetaModel.getModel(HEADER_TYPE);
    return headerType.getOptions().map(option => option.value);
  }
}