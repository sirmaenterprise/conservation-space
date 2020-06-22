import {Inject, Injectable} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {StatusCodes} from 'services/rest/status-codes';
import _ from 'lodash';
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

  loadTemplates(objectType, purpose, filterCriteria) {
    let templates = _.cloneDeep(data);

    if (purpose || filterCriteria) {
      if (!_.isUndefined(filterCriteria)  && !_.isObject(filterCriteria)) {
        throw new Error("An object should be provided for filter criteria")
      }

      if (filterCriteria && filterCriteria.active) {
        templates.data.splice(1,1);
      } else {
        templates.data.splice(2,1);
      }

      return this.promiseAdapter.resolve(templates);
    } else {
      return this.promiseAdapter.promise((resolve) => {
        setTimeout(() => {
          resolve(templates);
        }, config.template.timeout);
      });
    }
  }

  loadContent(id) {
    return this.promiseAdapter.resolve({data: blankTemplateContent});
  }

  editTemplateRules(templateInstanceId, rules) {
    window.savedRules = {
      id: templateInstanceId,
      rule: rules
    };

    return this.promiseAdapter.resolve({
      data: {},
      status: StatusCodes.SUCCESS
    });
  }
}