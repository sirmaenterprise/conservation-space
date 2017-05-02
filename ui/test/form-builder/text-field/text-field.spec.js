import {TextField} from 'form-builder/textfield/text-field';
import {DefinitionModelProperty} from 'models/definition-model';
import {InstanceModel} from 'models/instance-model';

describe('TextField',()=>{
  it('should bind to model on initialization',()=>{
    TextField.prototype.fieldViewModel = new DefinitionModelProperty({
      identifier:'testField',
      rendered:true,
    });
    TextField.prototype.form = {
      $addControl: ()=>{},
    };
    TextField.prototype.validationModel = new InstanceModel({
      testField:{value:'Test Value'},
    });
    TextField.prototype.widgetConfig ={
      formViewMode:'EDIT',
    };
    let fakeElement = {
      find:()=>{return {
        on:()=>{return {}},
        val:()=>{return {}},
        attr:()=>{return {}},
        addClass:()=>{return {}},
        removeClass:()=>{return {}},}},
      addClass:()=>{return {}},
      removeClass:()=>{return {}},
      controller: ()=>{return {$name:'asd'}},
    };
    let textField = new TextField(fakeElement);
    let controllerSpy = sinon.spy(textField.form,'$addControl');
    textField.ngOnInit();
    expect(controllerSpy.called).to.be.true;
    //the name should have changed to the textfield identifier
    expect(controllerSpy.calledWith({$name:'testField'}),'prolly here').to.be.true;
  })
})