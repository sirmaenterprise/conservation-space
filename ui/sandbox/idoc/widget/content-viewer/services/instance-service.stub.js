import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter, '$http')
export class InstanceRestService {

  constructor(promiseAdapter, $http) {
    this.promiseAdapter = promiseAdapter;
    this.$http = $http;
  }

  preview(id) {
    if (id === 'pdf') {
      return this.getFile('document.pdf');
    } else if (id === 'image') {
      return this.getFile('image.png');
    }
    return this.promiseAdapter.reject('404');
  }

  getFile(fileName) {
    return this.$http.get('/sandbox/idoc/widget/content-viewer/documents/' + fileName, {
      responseType: 'arraybuffer'
    });
  }

  loadBatch() {
    return this.promiseAdapter.resolve({data: []});
  }
}