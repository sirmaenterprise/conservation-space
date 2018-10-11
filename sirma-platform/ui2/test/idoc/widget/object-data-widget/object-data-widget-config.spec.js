import {ObjectDataWidgetConfig} from 'idoc/widget/object-data-widget/object-data-widget-config';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {CommonMocks} from '../properties-selector/common-mocks';
import {LABEL_POSITION_LEFT, LABEL_POSITION_HIDE, LABEL_TEXT_LEFT, LABEL_TEXT_RIGHT} from 'form-builder/form-wrapper';
import {SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {NO_LINK} from 'idoc/widget/object-data-widget/object-data-widget';
import {HEADER_COMPACT} from 'instance-header/header-constants';
import {SINGLE_SELECTION, MULTIPLE_SELECTION} from 'search/search-selection-modes';

describe('ObjectDataWidgetConfig', () => {
  describe('onObjectSelectorChanged', () => {
    it('should call getDefinitionsArray if select object mode is changed', () => {
      let objectDataWidgetConfig = mockObjectDataWidgetConfig();
      let getDefinitionsArrayStub = sinon.stub(objectDataWidgetConfig.propertiesSelectorHelper, 'getDefinitionsArray');
      objectDataWidgetConfig.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
      var payload = {
        selectObjectMode: SELECT_OBJECT_CURRENT
      };
      objectDataWidgetConfig.onObjectSelectorChanged(payload);
      expect(getDefinitionsArrayStub).to.be.calledOnce;
      getDefinitionsArrayStub.restore();
    });

    it('should call getDefinitionsArray if select object mode is "manually" and selected object is changed', () => {
      let objectDataWidgetConfig = mockObjectDataWidgetConfig();
      let getDefinitionsArrayStub = sinon.stub(objectDataWidgetConfig.propertiesSelectorHelper, 'getDefinitionsArray');
      objectDataWidgetConfig.config.selectedObject = 'old selected object';
      var payload = {
        selectObjectMode: SELECT_OBJECT_MANUALLY,
        selectedItems: [{id: 'new selected object'}]
      };
      objectDataWidgetConfig.onObjectSelectorChanged(payload);
      expect(getDefinitionsArrayStub).to.be.calledOnce;
      getDefinitionsArrayStub.restore();
    });

    it('should call getDefinitionsArray if select object mode is "automatically" and selected types in the criteria were changed', () => {
      let objectDataWidgetConfig = mockObjectDataWidgetConfig();
      let getDefinitionsArrayStub = sinon.stub(objectDataWidgetConfig.propertiesSelectorHelper, 'getDefinitionsArray');
      var payload = {
        selectObjectMode: SELECT_OBJECT_AUTOMATICALLY,
        searchCriteria: CommonMocks.mockCriteria()
      };
      objectDataWidgetConfig.onObjectSelectorChanged(payload);
      expect(getDefinitionsArrayStub.calledOnce).to.be.true;
      getDefinitionsArrayStub.restore();
    });
  });

  it('setSelectedObject should set selected object depending on the arguments', () => {
    let objectDataWidgetConfig = mockObjectDataWidgetConfig();
    objectDataWidgetConfig.setSelectedObject();
    expect(objectDataWidgetConfig.config.selectedObject).to.be.undefined;
    objectDataWidgetConfig.setSelectedObject([{id: 'selectedObjectId'}, {id: 'anotherSelectedObjectId'}]);
    expect(objectDataWidgetConfig.config.selectedObject).to.be.undefined;
    objectDataWidgetConfig.setSelectedObject([{id: 'selectedObjectId'}]);
    expect(objectDataWidgetConfig.config.selectedObject).to.equal('selectedObjectId');
  });

  describe('creation', () => {
    it('should populate config object with default values', () => {
      let objectDataWidgetConfig = mockObjectDataWidgetConfig({}, {});
      expect(objectDataWidgetConfig.config).to.eql({
        selectObjectMode: SELECT_OBJECT_MANUALLY,
        selection: SINGLE_SELECTION,
        labelPosition: LABEL_POSITION_LEFT,
        showFieldPlaceholderCondition: LABEL_POSITION_HIDE,
        labelTextAlign: LABEL_TEXT_LEFT,
        showMore: true,
        showRegionsNames: true,
        selectedProperties: {},
        selectedPropertiesData : {},
        showCodelistPropertyOptions: true,
        instanceLinkType: NO_LINK
      });
    });

    it('should override default values if external configurations are provided', () => {
      let config = {
        selectObjectMode: SELECT_OBJECT_CURRENT,
        labelPosition: LABEL_POSITION_HIDE,
        showFieldPlaceholderCondition: LABEL_POSITION_LEFT,
        labelTextAlign: LABEL_TEXT_RIGHT,
        showMore: false,
        showRegionsNames: false,
        showWidgetBorders: false,
        showWidgetHeaderBorders: false,
        selectedProperties: {
          'prop1': 'selected property'
        },
        selectedPropertiesData : {},
        instanceLinkType: HEADER_COMPACT
      };
      let objectDataWidgetConfig = mockObjectDataWidgetConfig(undefined, config);
      expect(objectDataWidgetConfig.config).to.eql({
        selectObjectMode: SELECT_OBJECT_CURRENT,
        selection: SINGLE_SELECTION,
        labelPosition: LABEL_POSITION_HIDE,
        showFieldPlaceholderCondition: LABEL_POSITION_LEFT,
        labelTextAlign: LABEL_TEXT_RIGHT,
        showMore: false,
        showRegionsNames: false,
        showWidgetBorders: false,
        showWidgetHeaderBorders: false,
        selectedProperties: {
          'prop1': 'selected property'
        },
        selectedPropertiesData : {},
        showCodelistPropertyOptions: true,
        instanceLinkType: HEADER_COMPACT
      })
    });
  });
});

function mockObjectDataWidgetConfig(scope, config, context) {
  let $scope = scope || mock$scope();
  ObjectDataWidgetConfig.prototype.config = config || {};
  ObjectDataWidgetConfig.prototype.context = context || CommonMocks.mockContext();
  let objectSelectorHelperMock = {
    getSelectedItems: () => {
      return [];
    }
  };

  let objectDataWidgetConfig = new ObjectDataWidgetConfig($scope, objectSelectorHelperMock, CommonMocks.mockPropertiesSelectorHelper());
  return objectDataWidgetConfig;
}
