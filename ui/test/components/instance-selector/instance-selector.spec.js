import {InstanceSelector} from 'components/instance-selector/instance-selector';
import {PickerService, SEARCH_EXTENSION} from 'services/picker/picker-service';
import {DialogService} from 'components/dialog/dialog-service';
import {HEADER_DEFAULT} from 'instance-header/header-constants';
import {InstanceObject} from 'idoc/idoc-context';

import {PromiseStub} from 'test/promise-stub';

describe('InstanceSelector', () => {
  let instanceSelector;

  beforeEach(() => {
    let initialValue = [{
      id: 'emf:123456',
      instanceType: 'documentInstance',
      headers: {
        [HEADER_DEFAULT]: 'defaultHeader'
      }
    }];
    InstanceSelector.prototype.instanceModelProperty = {value: initialValue};
    instanceSelector = getComponentInstance();
  });

  function getComponentInstance() {
    let extensionsDialogService = {
      openDialog: sinon.spy(() => {
        var pickerResult = {};
        pickerResult[SEARCH_EXTENSION] = {
          results: {
            config: {
              selectedItems: [{id: 'emf:123456'}]
            }
          }
        };
        return PromiseStub.resolve(pickerResult);
      })
    };
    let pickerService = new PickerService(extensionsDialogService);
    let instanceRestService = {
      loadBatch: (selectedItemIds) => {
        let result = selectedItemIds.map((selectedItemId) => {
          return {
            id: selectedItemId
          }
        });

        return PromiseStub.resolve({data: result});
      }
    };
    return new InstanceSelector(pickerService, instanceRestService, mockContextFactoryService());
  }

  it('removeSelectedItem should remove value by index', () => {
    instanceSelector.instanceModelProperty = {value: ['value0', 'value1', 'value2', 'value3']};
    instanceSelector.removeSelectedItem(2);
    expect(instanceSelector.instanceModelProperty.value).to.eql(['value0', 'value1', 'value3']);
  });

  it('select should configure and open picker dialog with context', () => {
    instanceSelector.select();

    var openSpy = instanceSelector.pickerService.extensionsDialogService.openDialog;
    expect(openSpy.callCount).to.equal(1);

    var searchConfig = openSpy.getCall(0).args[0].extensions[SEARCH_EXTENSION];
    expect(searchConfig).to.exist;
    expect(searchConfig.properties).to.exist;
    expect(searchConfig.triggerSearch).to.be.true;
    expect(searchConfig.results.config.exclusions).to.deep.equal([getContextPath().id]);
  });

  it('select should configure and open picker dialog without context', () => {
    instanceSelector.idocContextFactory.getCurrentContext = () => undefined;
    instanceSelector.select();

    var openSpy = instanceSelector.pickerService.extensionsDialogService.openDialog;
    expect(openSpy.callCount).to.equal(1);

    var searchConfig = openSpy.getCall(0).args[0].extensions[SEARCH_EXTENSION];
    expect(searchConfig).to.exist;
    expect(searchConfig.properties).to.exist;
    expect(searchConfig.triggerSearch).to.be.true;
    expect(searchConfig.results.config.exclusions).to.deep.equal([]);
  });

  it('select should set the selected items', () => {
    instanceSelector.select();
    expect(instanceSelector.instanceModelProperty.value).to.deep.equal([{id: 'emf:123456'}]);
  });

  it('isEditMode should return true if mode is edit', () => {
    instanceSelector.config.mode = 'edit';
    expect(instanceSelector.isEditMode()).to.be.true;
    instanceSelector.config.mode = 'preview';
    expect(instanceSelector.isEditMode()).to.be.false;
  });

  it('showPreviewDelimiter should return proper result', () => {
    instanceSelector.config.mode = 'preview';
    instanceSelector.instanceModelProperty = {value: ['value0', 'value1']};
    expect(instanceSelector.showPreviewDelimiter(0)).to.be.true;
    expect(instanceSelector.showPreviewDelimiter(1)).to.be.false;
  });

  it('should set header type according to the configuration', () => {
    InstanceSelector.prototype.config = {instanceHeaderType: HEADER_DEFAULT};
    instanceSelector = getComponentInstance();
    expect(instanceSelector.headerType).to.equal(HEADER_DEFAULT);
  });

  function getContextPath() {
    return {
      id: "test_id",
      type: "caseinstance"
    };
  }

  function mockContextFactoryService() {
    return {
      getCurrentContext: () => {
        return {
          getCurrentObject: () => {
            let path = getContextPath();
            let instance = new InstanceObject(path.id);
            instance.setContextPath(path);
            return PromiseStub.resolve(instance);
          }
        };
      }
    };
  }

});
