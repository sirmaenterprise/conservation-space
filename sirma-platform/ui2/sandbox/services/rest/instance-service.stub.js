import {Injectable, Inject} from 'app/app';
import {createSearchResponse} from 'sandbox/services/rest/instance-history-response';
import {SearchResponse} from 'services/rest/response/search-response';
import {InstanceObject} from 'models/instance-object';
import {InstanceResponse} from 'services/rest/response/instance-response';
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

    // Add predefined idoc
    sessionStorage.setItem('emf:123456', JSON.stringify(this.initIdoc('emf:123456')));

    let idocId_2 = 'emf:234567';
    let idoc_2Content = data.idocContent[idocId_2];
    let idoc_2 = this.initIdoc(idocId_2, null, idoc_2Content);
    sessionStorage.setItem(idocId_2, JSON.stringify(idoc_2));

    let idocId_3 = 'emf:345678';
    let idoc_3Content = data.idocContent[idocId_3];
    let idoc_3 = this.initIdoc(idocId_3, null, idoc_3Content);
    idoc_3.parentId = 'emf:123456';
    sessionStorage.setItem(idocId_3, JSON.stringify(idoc_3));

    // Objects stubbed in search rest service
    sessionStorage.setItem('1', JSON.stringify(this.initIdoc('1')));
    sessionStorage.setItem('2', JSON.stringify(this.initIdoc('2')));
    sessionStorage.setItem('3', JSON.stringify(this.initIdoc('3')));
    sessionStorage.setItem('4', JSON.stringify(this.initIdoc('4')));
    sessionStorage.setItem('5', JSON.stringify(this.initIdoc('5')));
    sessionStorage.setItem('6', JSON.stringify(this.initIdoc('6')));
    sessionStorage.setItem('7', JSON.stringify(this.initIdoc('7')));
    sessionStorage.setItem('8', JSON.stringify(this.initIdoc('8')));
    sessionStorage.setItem('9', JSON.stringify(this.initIdoc('9')));
    sessionStorage.setItem('john@domain', JSON.stringify(this.initIdoc('john@domain')));
    sessionStorage.setItem('ET210001', JSON.stringify(this.initIdoc('ET210001', 'ET210001')));
    sessionStorage.setItem('ET210002', JSON.stringify(this.initIdoc('ET210002', 'ET210002')));
    sessionStorage.setItem('ET210003', JSON.stringify(this.initIdoc('ET210003', 'ET210003')));
    sessionStorage.setItem('ET210004', JSON.stringify(this.initIdoc('ET210004', 'ET210004')));

    sessionStorage.setItem('test_id', JSON.stringify(this.initIdoc('test_id')));
    sessionStorage.setItem('aa873a4d-ccb2-4878-8a68-6be03deb2e7d', JSON.stringify(this.initIdoc('aa873a4d-ccb2-4878-8a68-6be03deb2e7d')));
    sessionStorage.setItem('currentObjectTempId', JSON.stringify(this.initIdoc('currentObjectTempId')));
  }

  initIdoc(id, definitionId, content) {
    let idocContent = content || data.idocContent['default'];
    return {
      id,
      definitionId: definitionId || 'definitionId',
      parentId: 'parentId',
      writeAllowed: true,
      content: idocContent,
      instanceType: 'documentinstance',
      properties: {
        title: 'Title',
        activityId: 'activityId' + id,
        'emf:definitionId': 'definitionId' + id,
        'rdf:type': {
          results: ['emf:Document'],
          total: 1
        }
      },
      headers: {
        breadcrumb_header: `<span class="header-icon"><img src="/build/images/instance-icons/user-icon-16.png" /></span><span><a class="" href="" ><span data-property="title">Header-${id}</span></a></span>`,
        compact_header: `<span class="header-icon"><img src="/build/images/instance-icons/user-icon-16.png" /></span><span><a class="" href="" ><span data-property="title">Header-${id}</span></a></span>`
      }
    };
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
    return this.promiseAdapter.resolve(new InstanceResponse({data: idoc}));
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
