import {Component, View,Inject} from 'app/app';
import template from './permissions-bootstrap-template.html!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'permissions-bootstrap'
})
@View({
  template: template
})
@Inject()
class Permissions {
  constructor() {
    this.context = {
      currentObjectId: 'emf:5e15053a-563c-4cab-aa8f-4edbd4db70a1',
      getCurrentObjectId: function () {
        return this.currentObjectId;
      },
      isEditMode: ()=> {
        return false;
      },
      getCurrentObject: ()=> {
        return Promise.resolve({libraryType: 'classinstance'});
      }
    };
  }
}
