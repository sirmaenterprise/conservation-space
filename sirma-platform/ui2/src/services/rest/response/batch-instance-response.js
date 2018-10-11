/**
 * Response wrapper for batch instance request response.
 *
 * @author svelikov
 */
export class BatchInstanceResponse {

  constructor(response) {
    this.response = response;
  }

  get data() {
    return this.response.data;
  }
}
