import {View, Component, Inject, Injectable} from 'app/app';
import {DropdownMenu} from 'components/dropdownmenu/dropdownmenu';
import {RestClient} from 'services/rest-client';
import 'style/bootstrap.css!';
import template from './dropdown-menu.stub.html!text';
import data from 'sandbox/components/dropdown-menu/dropdown-menu-data.json!';

@Injectable()
@Inject(RestClient)
class ActionsServiceStub {

  constructor(restClient:RestClient) {
    this.restClient = restClient;
  }

  getActions(opts) {
    return new Promise((resolve, reject) => {
      // cloning is required because the dropdown menu mutates the provided config
      resolve(data.slice(0));
    });
  }
}

@Component({
  selector: 'dropdown-menu-stub'
})
@View({
  template: template
})
@Inject(ActionsServiceStub)
export class DropdownMenuStub {

  constructor(actionsServiceStub) {
    this.actionsServiceStub = actionsServiceStub;
    this.config = {
      loadItems: () => this.loadItems(),
      buttonAsTrigger: true,
      triggerLabel: 'New',
      triggerClass: 'btn btn-sm btn-success',
      wrapperClass: 'contextual-actions-menu',
      switchlabel_onchange: true,
      reloadMenu: true
    };

    this.configWithGlyph = {
      loadItems: () => this.loadItems(),
      buttonAsTrigger: true,
      triggerLabel: ' ',
      triggerClass: 'btn-xs button-ellipsis',
      wrapperClass: 'pull-right',
      triggerIcon: '<i class="fa fa-circle-column"></i>',
      switchlabel_onchange: true,
      reloadMenu: true
    };

    this.configAsync = {
      loadItems: () => this.loadItemsAsync(),
      buttonAsTrigger: true,
      triggerLabel: 'New async',
      triggerClass: 'btn btn-sm btn-success',
      wrapperClass: 'contextual-actions-async-menu',
      switchlabel_onchange: true,
      reloadMenu: true
    }
  }

  loadItems() {
    return this.actionsServiceStub.getActions({
      placeholder: 'top.header.new.action',
      type: 'create',
      instanceId: 'instanceid',
      contextInstanceId: 'contextInstanceid'
    });
  }

  loadItemsAsync() {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve(this.loadItems());
      }, 1000);
    });
  }
}
