import {View, Component, Inject, NgScope} from 'app/app';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {InstanceObject} from 'idoc/idoc-context';
import {ActionsMenu} from 'idoc/actions-menu/actions-menu';
import {Configurable} from 'components/configurable';
import 'instance-header/static-instance-header/static-instance-header';
import 'header-container/header-container.css!css';
import headerContainerTpl from 'header-container/header-container.html!text';

const TRIGGER_ICON_GLYPH = '<i class="fa fa-circle-column"></i>';

@Component({
  selector: 'seip-header-container',
  properties: {
    'instance': 'instance',
    'instanceId': 'instance-id',
    'instanceType': 'instance-type',
    'header': 'header',
    'headerType': 'header-type',
    'thumbnail': 'thumbnail',
    'definitionId': 'definition-id',
    'config': 'config'
  }
})
@View({
  template: headerContainerTpl
})
@Inject(NgScope)
export class HeaderContainer extends Configurable {
  constructor($scope) {
    super({renderMenu: false});
    this.$scope = $scope;
    this.actionContext = this.getActionConfig();
  }

  getInstanceObject() {
    let instance = new InstanceObject(this.instanceId, {
      instanceType: this.instanceType,
      definitionId: this.definitionId,
      instance: this.instance
    }, null);
    // no context inside search results
    instance.setContextPath([]);
    instance.setThumbnail(this.thumbnail);
    return instance;
  }

  isMenuAllowed() {
    return !this.isBreadcrumb() && this.config.renderMenu;
  }

  isBreadcrumb() {
    return this.headerType === HEADER_BREADCRUMB;
  }

  getActionConfig() {
    return {
      placeholder: this.config.placeholder,
      scope: this.$scope,
      buttonAsTrigger: true,
      triggerLabel: ' ',
      triggerClass: 'btn-xs button-ellipsis',
      wrapperClass: 'pull-right',
      triggerIcon: TRIGGER_ICON_GLYPH,
      currentObject: this.getInstanceObject(),
      reloadMenu: this.config.reloadMenu || false,
      renderMenu: () => {
        return this.isMenuAllowed();
      }
    };
  }
}