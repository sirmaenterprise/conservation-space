import {Component, View, Inject} from 'app/app';
import {UrlUtils} from 'common/url-utils';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {InstanceObject} from 'models/instance-object';
import _ from 'lodash';
import models from 'sandbox/idoc/widget/datatable-widget/data/current-object.data.json!';
import template from 'sandbox/idoc/widget/datatable-widget/template.html!text';

@Component({
  selector: 'datatable-widget-bootstrap'
})
@View({
  template: template
})
@Inject(PromiseAdapter)
class DatatableWidgetBootstrap {

  constructor(promiseAdapter) {
    var hash = '?' + window.location.hash.substring(2);
    var mode = UrlUtils.getParameter(hash, 'mode');
    this.visible = false;
    this.currentObject = DatatableWidgetBootstrap.createObjectModel('currentObjectId');
    this.selectedObjects = {
      data: [ DatatableWidgetBootstrap.createObjectModel('emf:1') ],
      notFound: []
    };
    if (!mode) {
      mode = 'EDIT';
    }

    this.context = {
      mode: mode,
      getMode: () => {
        return mode;
      },
      getCurrentObject: () => {
        return promiseAdapter.resolve({
          getId: ()=> {
            return 'currentObjectId';
          },
          isVersion: () => false
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