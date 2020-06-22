import {ObjectControl} from 'form-builder/object-control/object-control';
import {FormWrapper} from 'form-builder/form-wrapper';
import {EventEmitter} from 'common/event-emitter';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {stub} from 'test/test-utils';

describe('ObjectControl', () => {
  let objectControl, objectControlConstructorStub;

  before(() => {
    objectControlConstructorStub = sinon.stub(ObjectControl.prototype, 'constructor');
  });

  after(() => {
    objectControlConstructorStub.restore();
  });

  beforeEach(() => {
    objectControl = new ObjectControlMock();
    objectControl.fieldViewModel = {
      label: 'Field Label'
    };
    objectControl.widgetConfig = {};
    objectControl.eventEmitter = stub(EventEmitter);
    objectControl.formWrapper = {
      formViewMode: FormWrapper.FORM_VIEW_MODE_PRINT
    };
    objectControl.isNewInstance = true;
  });

  describe('#constructor', () => {
    it('should emit event when instance selector is rendered', () => {
      objectControl.ngAfterViewInit();
      objectControl.formEventEmitter = stub(EventEmitter);
      objectControl.eventEmitter.publish('instanceSelectorRendered');
      expect(objectControl.formEventEmitter.publish.called).to.be.false;
    });
  });

  describe('getInstanceSelectorConfig(predefinedTypes)', () => {
    it('should construct configuration for the instance selector component', () => {
      let predefinedTypes = ['emf:User'];
      let config = objectControl.getInstanceSelectorConfig(predefinedTypes);

      expect(config).to.exist;
      expect(config.predefinedTypes).to.deep.equal(predefinedTypes);
      expect(config.label).to.equal('Field Label');
    });

    it('should configure to exclude the current object if the control is used for existing object', () => {
      objectControl.objectId = 'emf:123';
      let config = objectControl.getInstanceSelectorConfig([]);
      expect(config.excludedObjects).to.deep.equal(['emf:123']);
    });

    it('should configure to not exclude the current object if the control is used for not existing object', () => {
      objectControl.objectId = undefined;
      let config = objectControl.getInstanceSelectorConfig([]);
      expect(config.excludedObjects).to.deep.equal([]);
    });

    it('should pass through the isNewInstance flag', () => {
      let config = objectControl.getInstanceSelectorConfig([]);
      expect(config.isNewInstance).to.be.true;
    });

    it('should use provided picker restrictions', () => {
      let restrictions = SearchCriteriaUtils.getDefaultRule();
      let config = objectControl.getInstanceSelectorConfig([], restrictions);
      expect(config.pickerRestrictions).to.deep.equal(restrictions);
    });
  });

  describe('getPredefinedTypes()', () => {
    it('should return any predefined types declared in the view model\'s control range', () => {
      let controlParams = {
        range: 'emf:User, emf:Group'
      };
      let predefinedTypes = ObjectControl.getPredefinedTypes(controlParams);
      expect(predefinedTypes).to.deep.equal(['emf:User', 'emf:Group']);
    });

    it('should not return any predefined types if the view model\'s control is not configured', () => {
      let predefinedTypes = ObjectControl.getPredefinedTypes({});
      expect(predefinedTypes).to.not.exist;
    });
  });

  describe('getPickerRestrictions()', () => {
    it('should extract the restrictions and parse them to JSON', () => {
      let restrictions = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      let controlParams = {
        restrictions: JSON.stringify(restrictions)
      };
      let extractedRestrictions = ObjectControl.getPickerRestrictions(controlParams);
      expect(extractedRestrictions).to.deep.equal(restrictions);
    });
  });

  describe('#ngAfterViewInit', () => {
    it('should not emit event by default to formWrapper', () => {
      objectControl.formEventEmitter = stub(EventEmitter);
      objectControl.ngAfterViewInit();
      expect(objectControl.formEventEmitter.publish.callCount).to.equal(0);
    });
  });

});

class ObjectControlMock extends ObjectControl {

}