import {Component, View} from 'app/app';
import {InstanceHeader} from 'instance-header/instance-header';
import instanceHeaderTemplateStub from 'instance-header-stub-template!text';
import thumbnailData from 'sandbox/instance-header/static-instance-header/thumbnail.data.json!';

@Component({
  selector: 'instance-header-stub'
})
@View({
  template: instanceHeaderTemplateStub
})
export class InstanceHeaderStub {
  constructor() {

    this.headerType = 'default_header';
    var date = new Date('2015/12/22');
    var models = {
      validationModel: {
        type: {
          dataType: 'test',
          valueLabel: 'Type'
        },
        title: {
          dataType: 'test',
          value: 'Title'
        },
        status: {
          valueLabel: 'Draft'
        },
        owner: {
          dataType: 'test',
          value: {
            label: 'Owner'
          }
        },
        plannedEndDate: {
          value: date
        },
        createdOn: {
          value: date
        }
      },
      viewModel: {
        fields: [{
          identifier: 'type',
          isDataProperty: true
        }, {
          identifier: 'title',
          isDataProperty: true
        }, {
          identifier: 'status',
          isDataProperty: true
        }, {
          identifier: 'owner',
          isDataProperty: true
        }, {
          identifier: 'plannedEndDate',
          isDataProperty: true,
          dataType: 'datetime'
        }, {
          identifier: 'createdOn',
          isDataProperty: true,
          dataType: 'datetime'
        }]
      },
      instanceType: 'documentinstance',
      headers: {
        'default_header': '<span><a href="" class="instance-link has-tooltip"><b>(<span data-property="type"></span>) ' +
        '<span data-property="title"></span> (<span data-property="status"></span>) <span data-property="owner"></span></b></a></span>' +
        '<span><br>Due date: <span data-property="plannedEndDate"><span data-property="plannedEndDate" data-format="MMM/dd/yyyy"></span></span>' +
        'Created on: <span data-property="createdOn" data-format="MM.dd.yyyy"></span></span></span>',
        'compact_header': '<span data-property="type"></span><span data-property="title"></span><span data-property="status"></span>',
        'breadcrumb_header': '<span data-property="type"</span><span data-property="title"></span><span data-property="status"></span>'
      }
    };

    this.context = {
      currentObjectId: 'currentObjectId',
      models: models,
      getCurrentObject: () => {
        return new Promise((resolve) => {
          resolve(mockInstanceObject('currentObjectId', models));
        });
      }
    };

    this.thumbnailContext = {
      currentObjectId: 'currentObjectId',
      models: models,
      getCurrentObject: () => {
        return new Promise((resolve) => {
          resolve(mockInstanceObject('currentObjectId', models, thumbnailData.thumbnailImage));
        });
      }
    };
  }
}

function mockInstanceObject(id, models, thumbnail) {
  return {
    id: id,
    isPersisted: () => {
      return true;
    },
    models: models,
    headers: models.headers,
    getModels: () => {
      return models;
    },
    getInstanceType: function () {
      return models.instanceType;
    },
    getHeader: (type) => {
      return models.headers[type || 'compact_header'];
    },
    getThumbnail: () => {
      return thumbnail;
    },
    constructor: {
      isObjectProperty: () => {
        return false;
      }
    }
  }
}