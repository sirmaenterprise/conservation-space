import {RadiobuttonGroup} from 'form-builder/radiobutton-group/radiobutton-group';
import {InstanceModel} from 'models/instance-model';
import {DefinitionModelProperty} from 'models/definition-model';

describe('RadiobuttonGroup', () => {
  let fakeElement = {
    find: (elem)=> {
      return {
        addClass: ()=> {},
        removeClass: ()=> {},
        attr: () => {},
        each:()=>{return {}},}
    },
    each:()=>{return {}},

    addClass: ()=> {
    },
    removeClass: ()=> {
    },
    attr: () => {
    }
  };

  describe('getLabel()', () => {
    RadiobuttonGroup.prototype.fieldViewModel = new DefinitionModelProperty({
      'identifier': 'radioButtonGroupEditable2',
      'control': {
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
    });

    it('should return a label taken from the view model using the field value', () => {
      RadiobuttonGroup.prototype.validationModel = new InstanceModel({
        radioButtonGroupEditable2: {
          value: 'COL1'
        }
      });
      var radiobuttonGroup = new RadiobuttonGroup(fakeElement);
      expect(radiobuttonGroup.getLabel()).to.equal('option 1');
    });

    it('should return empty string as label if there is no selected value', () => {
      RadiobuttonGroup.prototype.validationModel = new InstanceModel({
        radioButtonGroupEditable2: {
          value: null
        }
      });
      var radiobuttonGroup = new RadiobuttonGroup(fakeElement);
      expect(radiobuttonGroup.getLabel()).to.equal('');
    });
  });

  describe('Radiobutton Binding', ()=> {
    it('should be set to disabled if property is changed', ()=> {
      RadiobuttonGroup.prototype.validationModel = new InstanceModel({
        radioButtonGroupEditable2: {
          value: null
        }
      });
      let radiobutton = new RadiobuttonGroup(fakeElement);
      radiobutton.fieldViewModel.control = {
        controlParams:{
          layout : '',
        }
      };
      radiobutton.widgetConfig = {
        formViewMode:'EDIT',
      };
      radiobutton.init();
      let value;
      radiobutton.fieldViewModel.subscribe('propertyChanged',(propertyChanged)=>{
       value = propertyChanged;
      });
      let disableSpy = sinon.spy(radiobutton,'setDisabled');
      radiobutton.fieldViewModel.disabled = false;
      expect(value).to.eql({disabled:false});
      expect(disableSpy.called).to.be.true;
      expect(disableSpy.calledWith(false)).to.be.true;
    });
  });
});