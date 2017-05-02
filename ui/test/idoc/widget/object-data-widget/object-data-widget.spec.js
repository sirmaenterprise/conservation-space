import {ObjectDataWidget, NO_LINK} from 'idoc/widget/object-data-widget/object-data-widget';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {SELECT_OBJECT_CURRENT, SELECT_OBJECT_MANUALLY, SELECT_OBJECT_AUTOMATICALLY} from 'idoc/widget/object-selector/object-selector';
import {PromiseAdapterMock} from '../../../adapters/angular/promise-adapter-mock';
import {StatusCodes} from 'services/rest/status-codes';
import {PromiseStub} from 'test/promise-stub';
import {MODE_PREVIEW} from 'idoc/idoc-constants';
import {ViewModelBuilder} from 'test/form-builder/view-model-builder';

describe('ObjectDataWidget', () => {

  let eventbus = {
    subscribe: () => {
    }, publish: () => {
    }
  };
  let isModeling;

  beforeEach(() => {
    isModeling = false;
  });

  describe('on widget create', () => {
    it('should not set form view model watcher in modeling mode', () => {
      isModeling = true;
      let objectDataWidget = createWidget();
      expect(objectDataWidget.config.formViewMode).to.equal(MODE_PREVIEW.toUpperCase())
    });
  });

  it('modelsAreLoaded should return true if models are defined', () => {
    let objectDataWidget = createWidget();
    objectDataWidget.formConfig.models = undefined;
    expect(objectDataWidget.modelsAreLoaded()).to.be.false;
    objectDataWidget.formConfig.models = {};
    expect(objectDataWidget.modelsAreLoaded()).to.be.true;
  });

  it('should update form view mode if context mode is changed', () => {
    ObjectDataWidget.prototype.control = {
      element: $()
    };
    let objectDataWidget = createWidget();
    objectDataWidget.promiseAdapter = PromiseStub;
    objectDataWidget.objectSelectorHelper = {
      getSelectedObject: sinon.spy(() => {
        return PromiseStub.resolve()
      })
    };
    objectDataWidget.context.getMode = function() { return undefined; };
    objectDataWidget.$scope.$digest();
    expect(objectDataWidget.config.formViewMode).to.be.undefined;

    objectDataWidget.context.getMode = function() { return 'test'; };
    objectDataWidget.$scope.$digest();
    expect(objectDataWidget.config.formViewMode).to.equal('TEST');
  });

  it('changing select object mode or selected object should call loadModels', () => {
    let objectDataWidget = createWidget();
    objectDataWidget.promiseAdapter = PromiseStub;
    objectDataWidget.loadModels = sinon.stub();
    objectDataWidget.config.selectObjectMode = 'new value';
    objectDataWidget.context.getMode = function() { return 'edit'; };
    objectDataWidget.$scope.$digest();
    expect(objectDataWidget.loadModels).to.be.calledOnce;

    objectDataWidget.config.selectedObject = 'new value';
    objectDataWidget.context.getMode = function() { return 'preview'; };
    objectDataWidget.$scope.$digest();
    expect(objectDataWidget.loadModels).to.be.calledTwice;
  });

  it('toggleShowAllProperties() should toggle the property value between true and false', () => {
    ObjectDataWidget.prototype.config = {};
    let objectDataWidget = createWidget();
    // default should be false
    expect(ObjectDataWidget.prototype.config.showAllProperties).to.be.false;
    objectDataWidget.toggleShowAllProperties();
    expect(ObjectDataWidget.prototype.config.showAllProperties).to.be.true;
  });

  describe('refresh()', () => {
    it('should set flag to force form builder re-init if object handled by ODW is updated', () => {
      ObjectDataWidget.prototype.config = {
        shouldReload: false
      };
      let objectDataWidget = createWidget();
      objectDataWidget.selectedObjectId = 'emf:123456';
      objectDataWidget.refresh({objectId: 'emf:123456'});
      expect(ObjectDataWidget.prototype.config.shouldReload).to.be.true;
    });

    it('should not set flag to force form builder re-init if the object handled by ODW is not updated', () => {
      ObjectDataWidget.prototype.config = {
        shouldReload: false
      };
      let objectDataWidget = createWidget();
      objectDataWidget.selectedObjectId = 'emf:999999';
      objectDataWidget.refresh({objectId: 'emf:123456'});
      expect(ObjectDataWidget.prototype.config.shouldReload).to.be.false;
    });
  });

  it('loadModels should remove not found objects from widget\'s config', (done) => {
    let objectSelectorHelperMock = {
      getSelectedObject: () => {
        return PromiseStub.resolve('emf:123456');
      }
    };
    let objectDataWidget = createWidget();
    objectDataWidget.objectSelectorHelper = objectSelectorHelperMock;
    objectDataWidget.config.selectObjectMode = SELECT_OBJECT_MANUALLY;
    objectDataWidget.selectedObjectId = 'emf:123456';
    objectDataWidget.context = {
      isModeling: () => {
        return false;
      },
      getSharedObject: () => {
        return PromiseStub.reject({
          status: StatusCodes.NOT_FOUND
        });
      },
      getMode: function() {

      }
    };
    objectDataWidget.objectSelectorHelper.removeSelectedObjects = sinon.spy();
    objectDataWidget.control = {
      saveConfig: sinon.spy(),
      getId: sinon.stub()
    };
    objectDataWidget.loadModels().then(() => {
      expect(objectDataWidget.objectSelectorHelper.removeSelectedObjects.callCount).to.equal(1);
      expect(objectDataWidget.objectSelectorHelper.removeSelectedObjects.args[0][1]).to.eql(['emf:123456']);
      expect(objectDataWidget.control.saveConfig.callCount).to.equal(1);
      done();
    }).catch(done);
  });

  describe('isLinkToInstanceVisible', () => {
    it('should return true if instance link type is defined in config', () => {
      let objectDataWidget = createWidget();
      objectDataWidget.config.instanceLinkType = 'default_header';
      expect(objectDataWidget.isLinkToInstanceVisible()).to.equal.true;
    });

    it('should return false if instance link type is not defined in config', () => {
      let objectDataWidget = createWidget();
      objectDataWidget.config.instanceLinkType = undefined;
      expect(objectDataWidget.isLinkToInstanceVisible()).to.equal.false;
      objectDataWidget.config.instanceLinkType = NO_LINK;
      expect(objectDataWidget.isLinkToInstanceVisible()).to.equal.false;
    });
  });

  describe('ngOnDestroy', () => {
    it('should clear what should be cleared', () => {
      let objectDataWidget = createWidget();
      objectDataWidget.newWidgetScope = {
        $destroy: sinon.stub()
      };
      objectDataWidget.element = {
        empty: sinon.stub(),
        remove: sinon.stub()
      };
      // usually this is set in a watcher but for simplicity of the test it is defined immediately
      // objectDataWidget.formContentReadyWatcher = () => {};
      objectDataWidget.ngOnDestroy();
      // expect(objectDataWidget.formContentReadyWatcher).to.be.undefined;
      expect(objectDataWidget.element.empty.calledOnce).to.be.true;
      expect(objectDataWidget.element.remove.calledOnce).to.be.true;
      expect(objectDataWidget.config === null).to.be.true;
      expect(objectDataWidget.context === null).to.be.true;
      expect(objectDataWidget.formConfig === null).to.be.true;
      expect(objectDataWidget.newWidgetScope.$destroy.calledOnce).to.be.true;
    });
  });

  describe('updatePropertyModel()', () => {
    it('should add control for field', () => {
      let objectDataWidget = createWidget();
      objectDataWidget.config = {
        selectedPropertiesData: {
          'definitionId': {
            'field1': 'controlType',
            'field2': 'controlType'
          }
        }
      };
      objectDataWidget.formConfig = getFormConfig();
      objectDataWidget.updatePropertyModel();
      var fields = objectDataWidget.formConfig.models.viewModel.flatModelMap;
      expect(fields['field1'].control.identifier).to.be.equal('controlType');
      expect(fields['field2'].control.identifier).to.be.equal('controlType');

    });
  });

  describe('getLabel', () => {
    it('should return empty string if  no labels are found', () => {
      let label = ObjectDataWidget.getLabel([], 'property1');
      expect(label).to.equal('');
    });

    it('should return single label if only one is found', () => {
      let label = ObjectDataWidget.getLabel([
        { name: 'property1', labels: [ 'Label' ] }
      ], 'property1');
      expect(label).to.equal('Label');
    });

    it('should return concatenated with comma labels if more than one are found', () => {
      let label = ObjectDataWidget.getLabel([
        { name: 'property1', labels: [ 'Label', 'Another label' ] }
      ], 'property1');
      expect(label).to.equal('Label, Another label');
    });
  });

  function getFormConfig() {
    return {
      models: {
        definitionId: 'definitionId',
        viewModel: {
          flatModelMap: new ViewModelBuilder()
            .addField('field1', 'EDITABLE', 'text', 'field 1', true, false, [], {}, 100)
            .addRegion('region1', 'region 1', 'EDITABLE', false, false)
            .addField('field2', 'EDITABLE', 'text', 'field 2', true, false, [], {}, 100)
            .endRegion()
            .getModel().flatModelMap
        }
      }
    };
  }

  function createWidget() {
    ObjectDataWidget.prototype.context = {
      isModeling: sinon.stub()
    };
    ObjectDataWidget.prototype.context.isModeling.returns(isModeling);
    return new ObjectDataWidget(mock$scope(), PromiseStub, null, eventbus, null, null);
  }

});