import {stub} from 'test/test-utils';
import {DatatableWidget} from 'idoc/widget/datatable-widget/datatable-widget';
import {TooltipAdapter} from 'adapters/tooltip-adapter';
import {ObjectSelectorHelper} from 'idoc/widget/object-selector/object-selector-helper';
import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {Eventbus} from 'services/eventbus/eventbus';
import {DefinitionService} from 'services/rest/definition-service';
import {PropertiesSelectorHelper} from 'idoc/widget/properties-selector/properties-selector-helper';
import {TranslateService} from 'services/i18n/translate-service';
import {PropertiesRestService} from 'services/rest/properties-service';
import {ResizeDetectorAdapterMock} from 'test/adapters/resize-detector-adapter-mock';
import {DatatableResizableIntegration} from 'idoc/widget/datatable-widget/datatable-resizable-integration';
import {DatatableSortableIntegration} from 'idoc/widget/datatable-widget/datatable-sortable-integration';
import {InstanceObject} from 'models/instance-object';
import {IdocContext} from 'idoc/idoc-context';
import {WidgetControl} from 'idoc/widget/widget';

export function instantiateDataTableWidget(config = {}, isModeling = false) {
  let tooltipAdapterStub = stub(TooltipAdapter);
  let objectSelectorHelperStub = stub(ObjectSelectorHelper);

  let widget = new DatatableWidget(mock$scope(), stubDefinitionService(), instantiatePropertiesSelectorHelper(),
    PromiseStub, objectSelectorHelperStub, stubTranslateService(), tooltipAdapterStub,
    stub(Eventbus), IdocMocks.mockLocationAdapter('/#/idoc/id?mode=edit'), {}, IdocMocks.mockElement(),
    IdocMocks.mockInterval(), IdocMocks.mockLogger(), mockPropertiesService(), mockResizeDetectorAdapter(), stub(DatatableResizableIntegration), stub(DatatableSortableIntegration));

  widget.config = config;
  widget.config.selectedProperties = widget.config.selectedProperties || {};
  widget.config.selectedSubPropertiesData = widget.config.selectedSubPropertiesData || {};
  widget.context = stubIdocContext(isModeling);
  widget.control = stubWidgetControl(widget);

  widget.ngOnInit();

  return widget;
}

export function stubElementFind() {
  return () => {
    return {
      width: sinon.spy(),
      add: () => {
        return {
          width: () => {
          }
        };
      }
    };
  };
}

function stubDefinitionService() {
  let definitionServiceStub = stub(DefinitionService);
  definitionServiceStub.getFields.returns(PromiseStub.resolve({data: mockMultipleDefinitions()}));
  return definitionServiceStub;
}

function instantiatePropertiesSelectorHelper() {
  return new PropertiesSelectorHelper(PromiseStub, stubDefinitionService());
}

function stubTranslateService() {
  let translateService = stub(TranslateService);
  translateService.translateInstant.returnsArg(0);
  return translateService;
}

function mockPropertiesService() {
  let propertiesServiceStub = stub(PropertiesRestService);
  propertiesServiceStub.getSearchableProperties.returns(PromiseStub.resolve({}));
  return propertiesServiceStub;
}

function mockResizeDetectorAdapter() {
  return ResizeDetectorAdapterMock.mockAdapter();
}

function stubIdocContext(isModeling) {
  let idocContext = stub(IdocContext);
  idocContext.isPreviewMode.returns(true);
  idocContext.isEditMode.returns(false);
  idocContext.isPrintMode.returns(false);
  idocContext.isModeling.returns(isModeling);
  idocContext.getSharedObjects = (objectIds) => {
    let instanceObjects = objectIds.map((objectId) => {
      return new InstanceObject(objectId);
    });
    return PromiseStub.resolve({ data: instanceObjects, notFound: [] });
  };
  idocContext.getCurrentObject = () => {
    let currentObject = stub(InstanceObject);
    currentObject.isVersion = () => false;
    return PromiseStub.resolve(currentObject);
  };
  return idocContext;
}

function stubWidgetControl(widget) {
  let widgetControl = new WidgetControl(IdocMocks.mockElement(), widget);
  sinon.stub(widgetControl, 'getId').returns('widget123456');
  sinon.stub(widgetControl, 'getBaseWidget').returns({
    saveConfigWithoutReload: sinon.stub(),
    ngOnDestroy: sinon.stub()
  });
  sinon.stub(widgetControl, 'saveConfig');
  sinon.stub(widgetControl, 'publish');
  sinon.stub(widgetControl, 'getDataFromAttribute');
  sinon.stub(widgetControl, 'storeDataInAttribute');
  return widgetControl;
}

function mockMultipleDefinitions() {
  return [
    {
      identifier: 'GEP111111',
      label: 'Project for testing',
      fields: [
        {
          name: 'property1',
          label: 'Property 1',
          fields: [
            {
              name: 'property5',
              label: 'Property 5'
            },
            {
              name: 'property6',
              label: 'Property 6'
            }
          ]
        },
        {
          name: 'property2',
          label: 'Property 2'
        },
        {
          name: 'property3',
          label: 'GEP111111 Property 3'
        },
        {
          name: 'emf:createdBy',
          label: 'Created by'
        }
      ]
    },
    {
      identifier: 'GEP100002',
      label: 'Test Project',
      fields: [
        {
          name: 'property1',
          label: 'Property 1'
        },
        {
          name: 'property3',
          label: 'GEP100002 Property 3'
        },
        {
          name: 'property5',
          label: 'Property 5'
        },
        {
          name: 'emf:createdBy',
          label: 'Created by'
        }
      ]
    }
  ];
}