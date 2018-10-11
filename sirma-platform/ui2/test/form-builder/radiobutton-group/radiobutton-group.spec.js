import {RadiobuttonGroup} from 'form-builder/radiobutton-group/radiobutton-group';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModelProperty} from 'models/definition-model';
import {EventEmitter} from 'common/event-emitter';
import {stub} from 'test/test-utils';
import {mockFormWrapper} from 'test/form-builder/form-wrapper-mock';

describe('RadiobuttonGroup', () => {
  let fakeElement = {
    find: ()=> {
      return {
        addClass: ()=> {
        },
        removeClass: ()=> {
        },
        attr: () => {
        },
        each: ()=> {
          return {};
        }
      };
    },
    each: () => {
      return {};
    },
    addClass: () => {
    },
    removeClass: () => {
    },
    attr: () => {
    }
  };

  let fieldViewModel = new DefinitionModelProperty({
    'identifier': 'radioButtonGroupEditable2',
    'controlId': 'RADIO_BUTTON_GROUP',
    'control': [
      {
        'identifier': 'RADIO_BUTTON_GROUP',
        'controlFields': [
          {
            'identifier': 'COL1',
            'label': 'option 1'
          },
          {
            'identifier': 'COL2',
            'label': 'option 2'
          }
        ]
      }
    ]
  });

  describe('getLabel()', () => {

    RadiobuttonGroup.prototype.identifier = 'radioButtonGroupEditable2';

    it('should return a label taken from the view model using the field value', () => {
      RadiobuttonGroup.prototype.formWrapper =  mockFormWrapper()
        .setFieldsMap({
          radioButtonGroupEditable2: fieldViewModel
        })
        .setValidationModel({
          radioButtonGroupEditable2: {
            value: 'COL1'
          }
        })
        .get();

      let radiobuttonGroup = new RadiobuttonGroup(fakeElement);
      expect(radiobuttonGroup.getLabel()).to.equal('option 1');
    });

    it('should return empty string as label if there is no selected value', () => {
      RadiobuttonGroup.prototype.formWrapper =  mockFormWrapper()
        .setFieldsMap({
          radioButtonGroupEditable2: fieldViewModel
        })
        .setValidationModel(new InstanceModel({
          radioButtonGroupEditable2: {
            value: null
          }
        }))
        .get();

      let radiobuttonGroup = new RadiobuttonGroup(fakeElement);
      expect(radiobuttonGroup.getLabel()).to.equal('');
    });
  });

  describe('Radiobutton Binding', ()=> {
    it('should be set to disabled if property is changed', ()=> {
      RadiobuttonGroup.prototype.formWrapper =  mockFormWrapper()
        .setFieldsMap({
          radioButtonGroupEditable2: fieldViewModel
        })
        .setValidationModel(new InstanceModel({
          radioButtonGroupEditable2: {
            value: null
          }
        }))
        .get();

      let radiobutton = new RadiobuttonGroup(fakeElement);
      radiobutton.control = {
        controlParams: {
          layout: ''
        }
      };
      radiobutton.widgetConfig = {
        formViewMode: 'EDIT'
      };
      radiobutton.init();
      let value;
      radiobutton.fieldViewModel.subscribe('propertyChanged', (propertyChanged)=> {
        value = propertyChanged;
      });
      let disableSpy = sinon.spy(radiobutton, 'setDisabled');
      radiobutton.fieldViewModel.disabled = false;
      expect(value).to.eql({disabled: false});
      expect(disableSpy.called).to.be.true;
      expect(disableSpy.calledWith(false)).to.be.true;
    });
  });

  describe('#ngAfterViewInit', () => {

    it('should not emit event by default to formWrapper',() => {
      let radiobutton = new RadiobuttonGroup(fakeElement);
      radiobutton.formEventEmitter = stub(EventEmitter);
      radiobutton.ngAfterViewInit();
      expect(radiobutton.formEventEmitter.publish.calledOnce).to.be.true;
    });
  });
});