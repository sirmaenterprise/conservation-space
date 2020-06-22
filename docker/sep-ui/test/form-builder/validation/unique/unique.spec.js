import {Unique} from 'form-builder/validation/unique/unique';
import {ValidationService} from 'form-builder/validation/validation-service';
import {PromiseStub} from 'promise-stub';

describe('Unique', () => {
  let propertiesService;
  let idocContextFactory = {
    getCurrentContext: function () {
      return {
        getCurrentObjectId: function () {
          return 'currentObject';
        }
      }
    }
  };
  let timeout = function (callback) {
    callback();
  };

  describe('validate()', () => {
    it('should call the properties rest service', () => {
      ValidationService.processAsyncValidation = sinon.spy();
      let unique = getUniqueValidator(true);
      let validationModel = getValidationModel('definitionId');
      let flatModel = getFlatModel('fieldUri');
      let formControl = getFormControl('create');
      unique.processResponse = sinon.spy();
      unique.getViewValue = sinon.spy();
      unique.validate('field', '', validationModel, flatModel, formControl, 'definitionId');
      expect(unique.getViewValue.called).to.be.true;
      expect(propertiesService.checkFieldUniqueness.called).to.be.true;
    });

    it('should return true if the unique is invoked when creating new object and no context is provided', (done) => {
      let unique = getUniqueValidator(true);
      unique.idocContextFactory = {
        getCurrentContext: () => {
          return undefined;
        }
      };
      unique.getViewValueAsString = sinon.spy();
      unique.validate('field').then((unique) => {
        expect(unique).to.be.true;
        done();
      });
    });
  });

  describe('processResponse()', () => {
    it('should call processAsyncValidation with correct data when value has changed', () => {
      let unique = getUniqueValidator(true);

      unique.getViewValue = sinon.stub().returns('newValue');
      ValidationService.processAsyncValidation = sinon.spy();
      unique.processResponse('oldValue', {data: {unique: true}});
      expect(ValidationService.processAsyncValidation.args[0][0]).to.equal(true);
    });

    it('should call processAsyncValidation with correct data when value is the same', () => {
      let unique = getUniqueValidator(true);

      unique.getViewValue = sinon.stub().returns('value');
      ValidationService.processAsyncValidation = sinon.spy();
      unique.processResponse('value', {data: {unique: false}});
      expect(ValidationService.processAsyncValidation.args[0][0]).to.equal(false);
    });
  });

  function getUniqueValidator(unique) {
    propertiesService = {
      checkFieldUniqueness: sinon.stub().returns(PromiseStub.resolve({data: {unique: unique}}))
    };

    return new Unique(propertiesService, idocContextFactory, {}, timeout);
  }

  function getValidationModel(definitionId) {
    return {
      'emf:definitionId': {
        value: definitionId
      }
    };
  }

  function getFlatModel(fieldUri) {
    return {
      field: {
        modelProperty: {
          uri: fieldUri
        }
      }
    };
  }

  function getFormControl(operation) {
    return {
      widgetConfig: {
        operation: operation,
        formConfig: {
          models: {
            definitionId: 'definitionId'
          }
        }
      }
    };
  }

});

