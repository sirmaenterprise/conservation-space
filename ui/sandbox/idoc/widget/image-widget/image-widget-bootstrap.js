import {Component, View, Inject} from 'app/app';
import {CommentsRestService} from 'services/rest/comments-service';
import {InstanceObject} from 'idoc/idoc-context';
import template from 'image-widget-bootstrap-template!text';
import 'style/bootstrap.css!';

@Component({
  selector: 'image-widget-bootstrap'
})
@View({
  template: template
})
@Inject(CommentsRestService)
class ImageWidgetBootstrap {
  constructor(commentsRestService) {
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