import {Inject, Injectable} from 'app/app';

const BASE_PATH = '/remote';

@Injectable()
@Inject('$http')
export class ServletClient {

  constructor($http) {
    this.$http = $http;
    this.basePath = BASE_PATH;
  }

  get(url, config) {
    return this.$http.get(this.basePath + url, config);
  }
}