import {Injectable, Inject} from 'app/app';
import {createSearchResponse} from 'sandbox/services/rest/instance-history-response';
import {SearchResponse} from 'services/rest/response/search-response';
import {InstanceObject} from 'models/instance-object';
import uuid from 'common/uuid';
import _ from 'lodash';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import data from 'sandbox/services/rest/instance-service.data.json!';

const DEFAULT_TEST_OBJECT_ID = 'default';

@Injectable()
@Inject(PromiseAdapter)
export class InstanceRestService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
    this.init();
  }

  init() {
    this.config = {};
    this.config.headers = {
      'Accept': 'application/vnd.seip.v2+json',
      'Content-Type': 'application/vnd.seip.v2+json'
    };

    let idocId = 'emf:123456';
    let idoc = (id, definitionId) => {
      return {
        'definitionId': definitionId || 'definitionId',
        'id': id,
        'parentId': 'parentId',
        'writeAllowed': true,
        'content': '<div><section data-id="2fa960ec-411d-4bab-e582-412a9e85e746" data-title="Tab 0" data-default="true" data-show-navigation="true" data-show-comments="true"><p>Content tab 0</p></section><section data-id="c0afcfc0-f15e-42fe-df3e-b0361764d18f" data-title="Tab 1" data-default="false" data-show-navigation="true" data-show-comments="false"><p>Content tab 1</p></section><section data-id="84fcb1f5-f33c-49d2-ba9f-50dede5823ec" data-title="Tab 2" data-default="false" data-show-navigation="false" data-show-comments="false"><p>Content tab 2</p></section></div>',
        'instanceType': 'documentinstance',
        'properties': {
          'title': 'Title',
          'activityId': 'activityId' + id,
          'emf:definitionId': 'definitionId' + id
        },
        'headers': {
          'breadcrumb_header': `<span class="header-icon"><img src="/build/images/instance-icons/user-icon-16.png" /></span><span><a class="" href="" ><span data-property="title">Header-${id}</span></a></span>`,
          'compact_header': `<span class="header-icon"><img src="/build/images/instance-icons/user-icon-16.png" /></span><span><a class="" href="" ><span data-property="title">Header-${id}</span></a></span>`
        }
      };
    };
    // Add predefined idoc
    let idocId_2 = 'emf:234567';
    let idoc_2 = idoc(idocId_2);
    idoc_2.content = `<div data-tabs-counter="2"> 
 <section data-id="Vb91W9aJ" data-title="Tab1" data-default="true" data-show-navigation="true" data-show-comments="true" data-revision="undefined" data-locked="false" data-user-defined="false"> 
  <p><span widget="object-link" class="widget object-link cke_widget_element initialized" config="eyJzZWxlY3RlZE9iamVjdCI6ImVtZjoxMjM0NTYifQ==" data-widget="object-link" id="rJ7cr45Q7"></span>,&nbsp;<span widget="object-link" class="widget object-link cke_widget_element initialized" config="eyJzZWxlY3RlZE9iamVjdCI6ImVtZjoxMjM0NTYifQ==" data-widget="object-link" id="BJxXqBNcQ7"></span>&nbsp;<br /></p> 
 </section> 
</div>`;
    sessionStorage.setItem(idocId, JSON.stringify(idoc(idocId)));
    sessionStorage.setItem(idocId_2, JSON.stringify(idoc_2));
    // Objects stubbed in search rest service
    sessionStorage.setItem('1', JSON.stringify(idoc('1')));
    sessionStorage.setItem('2', JSON.stringify(idoc('2')));
    sessionStorage.setItem('3', JSON.stringify(idoc('3')));
    sessionStorage.setItem('4', JSON.stringify(idoc('4')));
    sessionStorage.setItem('5', JSON.stringify(idoc('5')));
    sessionStorage.setItem('6', JSON.stringify(idoc('6')));
    sessionStorage.setItem('7', JSON.stringify(idoc('7')));
    sessionStorage.setItem('8', JSON.stringify(idoc('8')));
    sessionStorage.setItem('9', JSON.stringify(idoc('9')));
    sessionStorage.setItem('ET210001', JSON.stringify(idoc('ET210001', 'ET210001')));
    sessionStorage.setItem('ET210002', JSON.stringify(idoc('ET210002', 'ET210002')));
    sessionStorage.setItem('ET210003', JSON.stringify(idoc('ET210003', 'ET210003')));
    sessionStorage.setItem('ET210004', JSON.stringify(idoc('ET210004', 'ET210004')));

    sessionStorage.setItem('test_id', JSON.stringify(idoc('test_id')));
    sessionStorage.setItem('aa873a4d-ccb2-4878-8a68-6be03deb2e7d', JSON.stringify(idoc('aa873a4d-ccb2-4878-8a68-6be03deb2e7d')));
    sessionStorage.setItem('currentObjectTempId', JSON.stringify(idoc('currentObjectTempId')));
  }

  create(object) {
    if (!object.definitionId || !object.properties) {
      throw new Error('Definition or properties not provided');
    }

    return this.promiseAdapter.promise((resolve) => {
      let id = uuid();
      object.id = id;

      if (!object.headers) {
        object.headers = {
          breadcrumb_header: '<div>id</div>'
        };
      }
      sessionStorage.setItem(id, JSON.stringify(object));
      resolve({data: object});
    });
  }

  update(id, object) {
    if (!id) {
      throw new Error('Instance id should be provided');
    }

    return this.promiseAdapter.promise((resolve) => {
      object.id = id;

      if (!object.headers) {
        object.headers = {
          breadcrumb_header: '<div>id</div>'
        };
      }

      sessionStorage.setItem(id, JSON.stringify(object));
      resolve({data: object});
    });
  }

  updateAll(objects) {
    return this.promiseAdapter.promise((resolve) => {
      var id = 'emf:123456';
      objects[0].id = id;
      sessionStorage.setItem(id, JSON.stringify(objects[0]));
      resolve({data: objects});
    });
  }

  load(id) {
    let idoc = this.getStubbedObject(id);
    return this.promiseAdapter.resolve({data: idoc});
  }

  cloneProperties(id) {
    let idoc = this.getStubbedObject(id);
    return this.promiseAdapter.resolve({data: _.cloneDeep(idoc)});
  }

  loadDefaults(definitionId, parentInstanceId) {
    let response = _.cloneDeep(data.defaults[definitionId] || data.defaults.default);
    response.definitionId = definitionId;
    return new Promise((resolve) => {
      resolve({data: response});
    });
  }

  loadView(id) {
    let idoc = this.getStubbedObject(id);
    return this.promiseAdapter.resolve({data: idoc.content});
  }

  loadContextPath() {
    return this.promiseAdapter.resolve({data: []});
  }

  getInstanceProperty(id, property, offset, limit) {
    let instanceModel = data.instanceProperties.objectProperties;
    let propertyIds = instanceModel[property];
    let result;
    if (limit === -1) {
      result = propertyIds;
    } else {
      result = propertyIds.slice(offset, limit);
    }
    return Promise.resolve({data: result});
  }

  loadBatch(ids) {
    let result = ids.map((id) => {
      return this.getStubbedObject(id);
    });
    return this.promiseAdapter.resolve({data: result});
  }

  loadBatchDeleted(ids) {
    return this.loadBatch(ids);
  }

  getStubbedObject(id) {
    var object = JSON.parse(sessionStorage.getItem(id));

    if (!object) {
      object = _.cloneDeep(data.instances[id]);
    }

    return object;
  }

  getAllowedActions(id, type) {

  }

  loadModel(id) {
    let instanceModel = _.cloneDeep(data.models[id]);

    if (!instanceModel) {
      instanceModel = _.cloneDeep(data.models[DEFAULT_TEST_OBJECT_ID]);
    }

    return this.promiseAdapter.resolve({data: instanceModel});
  }

  loadModels(ids) {
    let result = {};
    ids.forEach((id) => {
      let instanceModel = _.cloneDeep(data.models[id]);

      if (!instanceModel) {
        instanceModel = _.cloneDeep(data.models[DEFAULT_TEST_OBJECT_ID]);
      }

      result[id] = instanceModel;
    });
    return this.promiseAdapter.resolve({data: result});
  }

  getContentUploadUrl(instanceId) {
    return `${instanceId}/content/`;
  }

  createDraft(id, content) {
    return this.promiseAdapter.promise((resolve) => {
      sessionStorage.setItem(`draft-${id}`, content);
      resolve({
        data: {
          draftCreatedOn: new Date()
        }
      });
    });
  }

  loadDraft(id) {
    return this.promiseAdapter.promise((resolve) => {
      let draft = sessionStorage.getItem(`draft-${id}`);
      let status = draft ? 200 : 204;
      resolve({
        status,
        data: {
          draftContentId: `draft-${id}`
        }
      });
    });
  }

  deleteDraft(id) {
    return new Promise((resolve) => {
      sessionStorage.removeItem(`draft-${id}`);
      resolve({});
    });
  }

  loadAuditDataForInstances(ids, limit, offset) {
    return this.promiseAdapter.resolve(new SearchResponse(createSearchResponse(ids, limit, offset)));
  }

  getVersions() {
    return this.promiseAdapter.resolve({data: {versions: data.versions}});
  }

  loadInstanceObject(instanceId, operation) {
    let instanceLoader = this.load(instanceId);
    let instanceDefinitionLoader = this.loadModel(instanceId, operation);

    return this.promiseAdapter.all([instanceLoader, instanceDefinitionLoader]).then(([{['data']: instance}, {['data']: definition}]) => {
      definition.headers = instance.headers;
      let instanceObject = new InstanceObject(instanceId, definition, undefined);
      instanceObject.mergePropertiesIntoModel(instance.properties);
      return instanceObject;
    });
  }

}
