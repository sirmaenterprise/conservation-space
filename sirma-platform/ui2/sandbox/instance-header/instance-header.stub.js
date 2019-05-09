import {Component, View, Inject} from 'app/app';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModel} from 'models/definition-model';
import {FormWrapper} from 'form-builder/form-wrapper';
import {ValidationService} from 'form-builder/validation/validation-service';
import {LABEL_POSITION_LEFT, LABEL_POSITION_HIDE, LABEL_TEXT_LEFT} from 'form-builder/form-wrapper';
import 'instance-header/instance-header';
import instanceHeaderTemplateStub from './instance-header.stub.html!text';
import thumbnailData from 'sandbox/instance-header/static-instance-header/thumbnail.data.json!';

@Component({
  selector: 'instance-header-stub'
})
@View({
  template: instanceHeaderTemplateStub
})
@Inject(ValidationService)
export class InstanceHeaderStub {
  constructor(validationService) {

    this.headerType = 'default_header';
    let date = new Date('2015/12/22');
    let models = {
      validationModel: new InstanceModel({
        type: {
          value: 'Common document',
          messages: []
        },
        title: {
          value: 'Offer',
          messages: []
        },
        country: {
          value: 'BGR',
          valueLabel: 'България',
          messages: []
        },
        owner: {
          value: {
            results: ['3'],
            total: 1
          },
          messages: []
        },
        references: {
          value: {
            results: ['1', '2', '3', '4', '5'],
            total: 5
          },
          defaultValue: {
            results: ['1', '2', '3', '4', '5'],
            total: 5
          },
          messages: []
        },
        dueDate: {
          value: date,
          messages: []
        },
        createdOn: {
          value: date,
          messages: []
        }
      }),
      viewModel: new DefinitionModel({
        fields: [{
          identifier: 'type',
          isDataProperty: true,
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          tooltip: 'Test tooltip',
          validators: [
            {
              id: 'regex',
              context: {
                pattern: '[\\s\\S]{1,20}'
              },
              message: 'This field should be max 20 characters length',
              level: 'error'
            }
          ],
          dataType: 'text',
          label: 'Type',
          maxLength: 20,
          isMandatory: false
        }, {
          identifier: 'title',
          isDataProperty: true,
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          tooltip: 'Test tooltip',
          validators: [
            {
              id: 'regex',
              context: {
                pattern: '[\\s\\S]{1,200}'
              },
              message: 'This field should be max 200 characters length',
              level: 'error'
            }
          ],
          dataType: 'text',
          label: 'Title',
          maxLength: 200,
          isMandatory: false
        }, {
          identifier: 'country',
          isDataProperty: true,
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          codelist: 555,
          dataType: 'ANY',
          tooltip: 'Test tooltip',
          validators: [],
          label: 'Country',
          isMandatory: false
        }, {
          identifier: 'owner',
          isDataProperty: false,
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          validators: [],
          dataType: 'text',
          label: 'Owner',
          isMandatory: false,
          tooltip: 'Test tooltip',
          controlId: 'PICKER',
          control: [
            {
              identifier: 'PICKER'
            }
          ]
        }, {
          identifier: 'references',
          isDataProperty: false,
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          validators: [],
          dataType: 'text',
          label: 'References',
          isMandatory: false,
          multivalue: true,
          tooltip: 'Test tooltip',
          controlId: 'PICKER',
          control: [
            {
              identifier: 'PICKER'
            }
          ]
        }, {
          identifier: 'dueDate',
          isDataProperty: true,
          dataType: 'datetime',
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          tooltip: 'Test tooltip',
          validators: [],
          label: 'Due date',
          isMandatory: false
        }, {
          identifier: 'createdOn',
          isDataProperty: true,
          dataType: 'datetime',
          previewEmpty: true,
          disabled: false,
          displayType: 'EDITABLE',
          tooltip: 'Test tooltip',
          validators: [],
          label: 'Created on',
          isMandatory: false
        }]
      }),
      definitionId: 'ET123121',
      instanceType: 'documentinstance',
      headers: {
        default_header: `<span><a href="" class="instance-link has-tooltip"><b>(<span data-property="type"></span>) <span data-property="title"></span> (<span data-property="country"></span>) <span data-property="owner"></span></b></a></span><span>
          <br> References: <span data-property="references"></span>, Due date: <span data-property="dueDate"><span data-property="dueDate" data-format="MMM/dd/yyyy"></span></span>Created on: <span data-property="createdOn" data-format="MM.dd.yyyy"></span></span></span>`,
        compact_header: '<span data-property="type"></span><span data-property="title"></span><span data-property="country"></span>',
        breadcrumb_header: '<span data-property="type"</span><span data-property="title"></span><span data-property="country"></span>'
      }
    };

    this.context = {
      currentObjectId: 'currentObjectId',
      models,
      getCurrentObject: () => {
        return new Promise((resolve) => {
          resolve(mockInstanceObject('currentObjectId', models));
        });
      }
    };

    this.thumbnailContext = {
      currentObjectId: 'currentObjectId',
      models,
      getCurrentObject: () => {
        return new Promise((resolve) => {
          resolve(mockInstanceObject('currentObjectId', models, thumbnailData.thumbnailImage));
        });
      }
    };

    this.formConfig = {
      models
    };
    this.config = {};
    this.config.labelPosition = LABEL_POSITION_LEFT;
    this.config.showFieldPlaceholderCondition = LABEL_POSITION_HIDE;
    this.config.labelTextAlign = LABEL_TEXT_LEFT;
    this.config.formViewMode = FormWrapper.FORM_VIEW_MODE_EDIT;
    this.validationService = validationService;
  }
}

function mockInstanceObject(id, models, thumbnail) {
  return {
    id,
    isPersisted: () => {
      return true;
    },
    models,
    headers: models.headers,
    getModels: () => {
      return models;
    },
    getInstanceType: () => {
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
  };
}