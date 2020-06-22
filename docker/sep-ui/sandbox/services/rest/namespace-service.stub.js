import {Injectable,Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/namespace-service.data.json!';

@Injectable()
@Inject(PromiseAdapter)
export class NamespaceService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  toFullURI(uris) {
    return this.createResult(uris);
  }

  createResult(uris) {
    var result = {};
    uris.forEach(function (element) {
      result[element] = data[element] || element;
    });

    return this.promiseAdapter.resolve({
      data: result
    });
  }

  convertToFullURI(uris) {
    return this.toFullURI(uris).then((result) => {
      var converted = [];
      uris.forEach((uri) => {
        converted.push(result.data[uri]);
      });
      return converted;
    });
  }

  isUri(uri) {
    return uri.indexOf(':') > 0;
  }

  isFullUri(uri) {
    return  uri.indexOf('http://') === 0 || uri.indexOf('https://') === 0;
  }
}
