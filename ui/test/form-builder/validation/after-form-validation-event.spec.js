import {AfterFormValidationEvent} from 'form-builder/validation/after-form-validation-event';

describe('AfterFormValidationEvent', function () {
  it('should return the event payload when getData is called', function () {
    var event = new AfterFormValidationEvent({
      data: {
        property1: 123
      }
    });
    expect(event.getData()[0]).to.deep.equal({
      data: {
        property1: 123
      }
    });
  });
});