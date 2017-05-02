import {Injectable,Inject} from 'app/app';
import data from 'sandbox/services/rest/instance-service.data.json!';
import {createSearchResponse} from 'sandbox/services/rest/instance-history-response';
import {SearchResponse} from 'services/rest/response/search-response';
import uuid from 'common/uuid';
import _ from 'lodash';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

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
    let idoc = (id) => {
      return {
        definitionId: 'definitionId',
        id: id,
        parentId: 'parentId',
        content: '<div><section data-id="2fa960ec-411d-4bab-e582-412a9e85e746" data-title="Tab 0" data-default="true" data-show-navigation="true" data-show-comments="true"><p>Content tab 0</p></section><section data-id="c0afcfc0-f15e-42fe-df3e-b0361764d18f" data-title="Tab 1" data-default="false" data-show-navigation="true" data-show-comments="false"><p>Content tab 1</p></section><section data-id="84fcb1f5-f33c-49d2-ba9f-50dede5823ec" data-title="Tab 2" data-default="false" data-show-navigation="false" data-show-comments="false"><p>Content tab 2</p></section></div>',
        properties: {
          title: 'Title',
          activityId: 'activityId' + id,
          'emf:definitionId': 'definitionId' + id
        },
        headers: {
          breadcrumb_header: '<span><a class="" href="" ><span data-property="title">Header</span></a></span>',
          compact_header: '<span><a class="" href="" ><span data-property="title">Header</span></a></span>'
        }
      }
    };
    // Adds one predefined idoc
    sessionStorage.setItem(idocId, JSON.stringify(idoc(idocId)));
    // Objects stubbed in search rest service
    sessionStorage.setItem('1', JSON.stringify(idoc('1')));
    sessionStorage.setItem('2', JSON.stringify(idoc('2')));
    sessionStorage.setItem('3', JSON.stringify(idoc('3')));
    sessionStorage.setItem('4', JSON.stringify(idoc('4')));
    sessionStorage.setItem('5', JSON.stringify(idoc('5')));
    sessionStorage.setItem('6', JSON.stringify(idoc('6')));

    sessionStorage.setItem('test_id', JSON.stringify(idoc('test_id')));
    sessionStorage.setItem('aa873a4d-ccb2-4878-8a68-6be03deb2e7d', JSON.stringify(idoc('aa873a4d-ccb2-4878-8a68-6be03deb2e7d')));
    sessionStorage.setItem('currentObjectTempId', JSON.stringify(idoc('currentObjectTempId')));
  }

  create(object) {
    if (!object.definitionId || !object.properties) {
      throw new Error("Definition or properties not provided");
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
      throw new Error("Instance id should be provided");
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
    return new Promise((resolve) => {
      var id = 'emf:123456';
      objects[0].id = id;
      sessionStorage.setItem(id, JSON.stringify(objects[0]));
      resolve({data: objects});
    });
  }

  load(id) {
    let idoc = this.getStubbedObject(id);

    return new Promise((resolve) => {
      resolve({data: idoc});
    });
  }

  loadDefaults(definitionId, parentInstanceId) {
    let response = _.cloneDeep(data.defaults);
    response.definitionId = definitionId;
    return new Promise((resolve) => {
      resolve({data: response});
    });
  }

  loadView(id) {
    let idoc = this.getStubbedObject(id);

    return new Promise((resolve) => {
      resolve({data: idoc.content});
    });
  }

  loadContextPath() {
    return Promise.resolve({data: []});
  }

  loadBatch(ids) {
    let result = ids.map((id) => {
      return this.getStubbedObject(id);
    });
    return Promise.resolve({data: result});
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
      instanceModel = _.cloneDeep(data.models[DEFAULT_TEST_OBJECT_ID])
    }

    return Promise.resolve({data: instanceModel});
  }

  loadModels(ids) {
    let result = {};
    ids.forEach((id) => {
      let instanceModel = _.cloneDeep(data.models[id]);

      if (!instanceModel) {
        instanceModel = _.cloneDeep(data.models[DEFAULT_TEST_OBJECT_ID])
      }

      result[id] = instanceModel;
    });
    return Promise.resolve({data: result});
  }

  getContentUploadUrl(instanceId) {
    return `${instanceId}/content/`;
  }

  createDraft(id, content) {
    return new Promise((resolve) => {
      sessionStorage.setItem(`draft-${id}`, content);
      resolve({
        data: {
          draftCreatedOn: new Date()
        }
      });
    });
  }

  loadDraft(id) {
    return new Promise((resolve) => {
      let draft = sessionStorage.getItem(`draft-${id}`);
      let status = draft ? 200 : 204;
      resolve({
        status: status, data: {
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
}
