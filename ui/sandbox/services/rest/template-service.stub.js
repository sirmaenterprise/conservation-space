import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {StatusCodes} from 'services/rest/status-codes';
import data from 'sandbox/services/rest/template-service.data.json!';
import config from 'sandbox/services/rest/services.config.json!';
import blankTemplateContent from 'services/rest/blank-template.html!text';

@Injectable()
@Inject(PromiseAdapter)
export class TemplateService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  create(template) {
    window.savedTemplate = template;

    return this.promiseAdapter.resolve({
      data: 'templateInstanceId',
      status: StatusCodes.SUCCESS
    });
  }

  loadTemplates(definitions) {
    return this.promiseAdapter.promise((resolve) => {
      setTimeout(() => {
        resolve(data);
      }, config.template.timeout);
    });
  }

  loadContent(id) {
    return this.promiseAdapter.resolve({ data: blankTemplateContent });
  }
}