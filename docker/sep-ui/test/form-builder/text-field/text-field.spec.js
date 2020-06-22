import {TextField} from 'form-builder/textfield/text-field';
import {DefinitionModelProperty} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test-utils';
import {mockFormWrapper} from 'test/form-builder/form-wrapper-mock';

describe('TextField', () => {
  let fakeElement = {
    find: () => {
      return {
        on: () => {
          return {};
        },
        val: () => {
          return {};
        },
        attr: () => {
          return {};
        },
        addClass: () => {
          return {};
        },
        removeClass: () => {
          return {};
        }
      };
    },
    addClass: () => {
      return {};
    },
    removeClass: () => {
      return {};
    },
    controller: () => {
      return {$name: 'asd'};
    }
  };

  it('should bind to model on initialization', () => {
    let fieldsMap = {
      testField: new DefinitionModelProperty({
        identifier: 'testField',
        rendered: true
      })
    };

    TextField.prototype.formWrapper = mockFormWrapper()
      .setFieldsMap(fieldsMap)
      .setValidationModel(new InstanceModel({
        testField: {value: 'Test Value'}
      }))
      .setObjectDataForm({
        $addControl: () => {}
      })
      .setConfig({
        formViewMode: 'EDIT'
      })
      .get();

    TextField.prototype.identifier = 'testField';

    let textField = new TextField(fakeElement);
    let controllerSpy = sinon.spy(textField.form, '$addControl');
    textField.ngOnInit();
    expect(controllerSpy.called).to.be.true;
    //the name should have changed to the textfield identifier
    expect(controllerSpy.calledWith({$name: 'testField'}), 'prolly here').to.be.true;
  });

  describe('#ngAfterViewInit', () => {
    it('should not emit event by default to formWrapper', () => {
      let textField = new TextField(fakeElement);
      textField.formEventEmitter = stub(EventEmitter);
      textField.ngAfterViewInit();
      expect(textField.formEventEmitter.publish.calledOnce).to.be.true;
    });
  });
});