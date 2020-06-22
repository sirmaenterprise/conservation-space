import { Inject, Injectable } from 'app/app';
import { RestClient } from 'services/rest-client';
import { MODE_PRINT } from 'idoc/idoc-constants';
import { UrlUtils } from 'common/url-utils';
import _ from 'lodash';

export const SERVICE_URL = '/export';

@Injectable()
@Inject(RestClient)
export class ExportService {

  constructor(restClient) {
    this.restClient = restClient;
    this.config = {};
  }

  exportPDF(objectId, tabId) {
    let idocPrintUrl = UrlUtils.buildIdocUrl(objectId, tabId, {
      'mode': MODE_PRINT
    });
    return this.restClient.post(SERVICE_URL, idocPrintUrl, this.config);
  }

  getExportedFile(fileName) {
    let config = _.cloneDeep(this.config);
    config.headers = {
      'Accept': 'application/pdf'
    };
    config.responseType = 'arraybuffer';
    let url = `${SERVICE_URL}/${fileName}`;
    return this.restClient.get(url, config);
  }

  exportXlsx(data) {
    let config = _.cloneDeep(this.config);
    config.headers = {
      'content-type': 'application/vnd.seip.v2+json'
    };
    return this.restClient.post('/instances/'+ data.instanceId + '/actions/export-xlsx', data, config);
  }
}