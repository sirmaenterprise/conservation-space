import {Component, View} from 'app/app';
import {ResourceManagement} from 'administration/resources-management/resource-management';
import template from './resource-management-template.html!text';

@Component({
  selector: 'resource-management-bootstrap'
})
@View({
  template
})
class ResourceManagementBootstrap {

  constructor() {
    ResourceManagement.PAGE_SIZE = 3;
  }

}