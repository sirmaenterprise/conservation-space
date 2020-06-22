import {Component, View, Inject} from 'app/app';
import template from './template.html!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'comments-widget-bootstrap'
})
@View({
  template: template
})
@Inject()
class CommentsWidgetBootstrap {
  constructor() {
    this.visible = false;
    this.modelingMode = false;

    this.context = {
      currentObjectId: 'sandbox-page',
      getCurrentObjectId: () => 'sandbox-page',
      isModeling: () => {
        return this.modelingMode;
      },
      isEditMode(){
        return true;
      },
      getMode: () => {
        return 'edit';
      },
      isPreviewMode(){
        return true;
      },
      isPrintMode(){
        return false;
      },
      getCurrentObject(){
        if (this.modelingMode) {
          return Promise.resolve({
            "annotations": [],
            "instanceHeaders": {}
          });
        }
        return Promise.resolve({
          getId(){
            return 'emf:1';
          },
          isPersisted(){
            return false;
          },
          isVersion() {
            return false;
          }
        });
      }
    };
    this.config = {
      selectedObjects: [1],
      selectObjectMode: 'manually',
      getEditor: () => {
        tab: {
          locked: false
        }
      },
      filterCriteria: {
        operator: 'is',
        value: [moment(), moment().add(1, 'day')]
      }
    };
    this.control = {
      getId(){
        return '1'
      }
    };
  }

  display() {
    this.visible = !this.visible;
  }

  setModellingMode() {
    this.modelingMode = !this.modelingMode;
  }
}
