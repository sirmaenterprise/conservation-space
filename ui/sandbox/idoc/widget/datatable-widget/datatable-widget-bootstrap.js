import {Component, View, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'idoc/idoc-context';
import template from 'sandbox/idoc/widget/datatable-widget/template.html!text';
import models from 'sandbox/idoc/widget/datatable-widget/data/current-object.data.json!';
import _ from 'lodash';

@Component({
  selector: 'datatable-widget-bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter)
class DatatableWidgetBootstrap {

  constructor(promiseAdapter) {
    this.visible = false;
    this.currentObject = DatatableWidgetBootstrap.createObjectModel('currentObjectId');
    this.selectedObjects = {
      data: [ DatatableWidgetBootstrap.createObjectModel('emf:1') ],
      notFound: []
    };

    this.context = {
      mode: 'EDIT',
      getMode: () => {
        return 'edit';
      },
      getCurrentObject: () => {
        return promiseAdapter.resolve({
          getId: ()=> {
            return 'currentObjectId';
          }
        });
      },
      getSharedObject: () => {
        return promiseAdapter.resolve(this.currentObject);
      },
      getSharedObjects: () => {
        return promiseAdapter.resolve(this.selectedObjects);
      },
      getSharedObjectsRegistry: () => {
        return {
          onWidgetDelete: () => {
          }
        }
      },
      isModeling: () => {
        return true;
      },
      isEditMode: () => {
        return true;
      },
      isPrintMode: () => {
        return false;
      },
      isPreviewMode: () => {
        return false;
      }
    }
  }

  toggleWidget() {
    this.visible = !this.visible;
  }

  static createObjectModel(id) {
    return new InstanceObject(id, _.cloneDeep(models), 'content', false);
  }
}