import {Component, View, Inject, NgScope} from 'app/app';
import {CommentsRestService} from 'services/rest/comments-service';
import {InstanceObject} from 'models/instance-object';
import template from 'image-widget-bootstrap-template!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'image-widget-bootstrap'
})
@View({
  template: template
})
@Inject(CommentsRestService, NgScope)
class ImageWidgetBootstrap {
  constructor(commentsRestService, $scope) {
    this.$scope = $scope;
    this.$scope.editor = {
      tab: {},
      editor: {
        fire: () => {

        }
      }
    };
    // cleanup mirador state
    sessionStorage.clear();
    localStorage.clear();
    this.visible = true;
    this.isModeling = false;
    this.commentsRestService = commentsRestService;

    this.context = {
      getMode: () => {
        return 'edit';
      },
      isModeling: () => {
        return this.isModeling;
      },
      isEditMode(){
        return true;
      },
      isPreviewMode(){
        return false
      },
      isPrintMode() {
        return false;
      },
      getCurrentObject(){
        return Promise.resolve({
          getId: () => {
            return 'emf:123';
          },
          data: [new InstanceObject('emf:123')]
        });
      },
      getSharedObjects() {
        return Promise.resolve({
          notFound: 0,
          data: [new InstanceObject('emf:123')]
        })
      }
    };

    this.control = {
      getDataValue(){
      },
      setDataValue(){
      },
      getDataFromAttribute(){
      },
      getId(){
        return '1'
      }
    };
  }

  toggleWidget() {
    this.visible = !this.visible;
  }
}