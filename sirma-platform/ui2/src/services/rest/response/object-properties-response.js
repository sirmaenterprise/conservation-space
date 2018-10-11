/**
 * Response wrapper for object properties model. The model is a simple array.
 *
 * @author svelikov
 */
export class ObjectPropertiesResponse {

  constructor(response) {
    this.response = response;
  }

  get data() {
    return this.response.data;
  }

}