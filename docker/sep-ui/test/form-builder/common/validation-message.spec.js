import {ValidationMessage} from 'form-builder/common/validation-message';
import {InstanceModelProperty} from 'models/instance-model';
import {DefinitionModelProperty} from 'models/definition-model';

describe('ValidationMessage', () => {

  let baseElement = {
    html: () => {
    },
    empty: () => {
    },
    remove: () => {
    }
  };

  sinon.stub(baseElement, 'remove').returns(baseElement);
  sinon.stub(baseElement, 'empty').returns(baseElement);

  let htmlSpy = sinon.spy(baseElement, 'html');

  let fakeElement = {
    find: (elem)=> {
      return baseElement;
    }
  };
  var validationMessage;

  beforeEach(()=> {
    htmlSpy.reset();

    validationMessage = new ValidationMessage(fakeElement);
    validationMessage.validationModel = new InstanceModelProperty({
      messages: []
    });
    validationMessage.viewModel = new DefinitionModelProperty({
      preview: false
    });
  });

  describe('ngOnInit', ()=> {
    it('should append validation message when in edit mode and there are available messages', () => {
      validationMessage.validationModel.messages = [{
        'id': 'mandatory', 'level': 'error', 'message': 'The field is mandatory'
      }];

      validationMessage.ngOnInit();
      expect(htmlSpy.calledOnce).to.be.true;
      expect(htmlSpy.calledWith('<span id="mandatory" class="mandatory">The field is mandatory</span>'));
    });

    it('should return not append messages when in edit mode and there are no messages object', () => {
      validationMessage.ngOnInit();
      expect(htmlSpy.calledWith('')).to.be.true;
    });
  });

  describe('ngOnDestroy', ()=> {
    it('should unsubscribe to event emitter when component is destroyed', ()=> {
      validationMessage.ngOnInit();
      let viewModelSpy = sinon.spy(validationMessage.viewModelSubscription, 'unsubscribe');
      let validationModel = sinon.spy(validationMessage.validationModelSubscription, 'unsubscribe');

      validationMessage.ngOnDestroy();
      expect(viewModelSpy.calledOnce).to.be.true;
      expect(validationModel.calledOnce).to.be.true;
    });
  });

  it('should add a new message when a validation message is published', () => {
    validationMessage.ngOnInit();
    validationMessage.validationModel.messages = [{
      'id': 'mandatory',
      'level': 'error',
      'message': 'The field is mandatory'
    }];
    expect(htmlSpy.called).to.be.true;
    expect(htmlSpy.calledWith('<span id="mandatory" class="error">The field is mandatory</span>')).to.be.true;
  });

  it('should remove a message when a an empty validation message array is published', ()=> {
    validationMessage.validationModel.messages = [{
      'id': 'mandatory',
      'level': 'error',
      'message': 'The field is mandatory'
    }];
    validationMessage.ngOnInit();
    validationMessage.validationModel.messages = [];
    expect(htmlSpy.called).to.be.true;
    expect(htmlSpy.calledWith('')).to.be.true;
  });

  it('should remove validation messages when mode is switched from edit to preview', ()=> {
    validationMessage.validationModel.messages = [{
      'id': 'mandatory',
      'level': 'error',
      'message': 'The field is mandatory'
    }];
    validationMessage.ngOnInit();
    validationMessage.viewModel.preview = true;
    expect(htmlSpy.calledTwice, 'once on init, once to remove').to.be.true;
  });
});