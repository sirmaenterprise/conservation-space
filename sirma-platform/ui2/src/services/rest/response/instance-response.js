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

}
