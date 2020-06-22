import {LINK} from 'services/rest/http-headers';

const LINKS_PATTERN = /\<(.*?)\>.*?rel\=\"(.*?)\"/gm;
const OFFSET_PARAM_PATTERN = /offset=(\d+)/;
const LIMIT_PARAM_PATTERN = /limit=(\d+|all)/;

export class SearchResponse {

  constructor(response) {
    this.response = response;
    this.data = response.data;
  }

  get lastPageNumber() {
    if (this._lastPage) {
      return this._lastPage;
    }

    this._lastPage = Math.floor(this.offset / this.limit);
    return this._lastPage;
  }

  get limit() {
    if (this._limit) {
      return this._limit;
    }

    if (!this.links.last) {
      this._limit = 0;
      return this._limit;
    }

    var limitResult = LIMIT_PARAM_PATTERN.exec(this.links.last);
    this._limit = limitResult[1];
    if (this._limit !== 'all') {
      this._limit = parseInt(this._limit);
    }
    return this._limit;
  }

  get offset() {
    if (this._offset) {
      return this._offset;
    }

    if (!this.links.last) {
      this._offset = 0;
      return this._offset;
    }

    var offsetResult = OFFSET_PARAM_PATTERN.exec(this.links.last);
    this._offset = parseInt(offsetResult[1]);
    return this._offset;
  }

  get links() {
    if (this._links) {
      return this._links;
    }

    var links = {};
    var header = this.response.headers(LINK);

    var result;
    while ((result = LINKS_PATTERN.exec(header)) !== null) {
      links[result[2]] = result[1];
    }

    this._links = links;
    return this._links;
  }
}