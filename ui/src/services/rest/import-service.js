import {Inject, Injectable} from 'app/app';
import {RestClient} from 'services/rest-client';

@Injectable()
@Inject(RestClient)
export class ImportService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
    this.config.headers = {
      'content-type': 'application/vnd.seip.v2+json'
    };
  }

  readFile(currentObject, context) {
    let readData = {};
    readData.context = context;
    return this.restClient.post('/instance/' + currentObject + '/integration/read', readData, this.config);
  }

  importFile(currentObject, context, data) {
    let dataForImport = {};
    dataForImport.data = data.data;
    dataForImport.report = data.report.id;
    dataForImport.context = context;
    return this.restClient.post('/instance/' + currentObject + '/integration/import', dataForImport, this.config);
  }
}
