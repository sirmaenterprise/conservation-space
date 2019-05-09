import {CONTENT_ID} from 'instance/instance-properties';
import {InstanceUtils} from 'instance/utils';
import {RelatedObject} from 'models/related-object';

import _ from 'lodash';

/**
 * Response wrapper for instance model.
 *
 * @author svelikov
 */
export class InstanceResponse {

  constructor(response) {
    this.response = response;
  }

  get data() {
    return this.response.data;
  }

  get id() {
    return this.data.id;
  }

  get properties() {
    return this.data.properties;
  }

  get instanceType() {
    return this.data.instanceType;
  }

  get definitionId() {
    return this.data.definitionId;
  }

  get parentId() {
    return this.data.parentId;
  }

  get headers() {
    return new InstanceHeaders(this.data.headers);
  }

  getPropertyValue(name) {
    let property = this.properties[name];
    // should properly handle object property values that are in format { results: [id1, id2, ...] }
    if (_.isObject(property) && property.results) {
      return new RelatedObject(property);
    }
    return property;
  }

  getPurpose() {
    return InstanceUtils.getPurpose(this.getPropertyValue(CONTENT_ID));
  }
}


class InstanceHeaders {

  constructor(headers) {
    this.headers = headers;
  }

  get breadcrumb_header() {
    return this.headers.breadcrumb_header;
  }

  get compact_header() {
    return this.headers.compact_header;
  }

  get default_header() {
    return this.headers.default_header;
  }
}