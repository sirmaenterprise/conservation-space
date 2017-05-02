import {View, Component, Inject} from 'app/app';
import _ from 'lodash';
import {Configurable} from 'components/configurable';
import {PickerService} from 'services/picker/picker-service';
import {InstanceUtils} from 'instance/utils';
import {HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {IdocContextFactory} from  'services/idoc/idoc-context-factory';

import 'font-awesome/css/font-awesome.css!';
import './context-selector.css!';
import contextSelectorTemplate from './context-selector.html!text';

@Component({
  selector: 'seip-context-selector',
  properties: {
    'config': 'config'
  }
})
@View({
  template: contextSelectorTemplate
})
@Inject(PickerService, TranslateService, InstanceRestService, IdocContextFactory)
export class ContextSelector extends Configurable {

  constructor(pickerService, translateService, instanceRestService, idocContextFactory) {
    super({
      contextSelectorDisabled: false,
      onContextSelected: _.noop
    });
    this.pickerService = pickerService;
    this.translateService = translateService;
    this.instanceRestService = instanceRestService;
    this.idocContextFactory = idocContextFactory;

    this.pickerConfig = {};
    this.pickerService.assignDefaultConfigurations(this.pickerConfig);

    this.preserveContextSelection(this.config.parentId);
    this.getContext(this.config.parentId);
  }

  /**
   * If the provided parent instance ID is for existing instance, it preserves the picker selection.
   * @param id - the parent instance ID
   */
  preserveContextSelection(id) {
    if (this.parentExists(id)) {
      this.pickerService.setSelectedItems(this.pickerConfig, [{id: id}]);
    }
  }

  selectContext() {
    this.pickerService.open(this.pickerConfig, this.idocContextFactory.getCurrentContext()).then((selectedItems) => {
      if (selectedItems.length > 0) {
        this.config.onContextSelected(selectedItems[0].id);
        this.getContext(selectedItems[0].id);
      }
    });
  }

  clearContext() {
    this.config.parentId = null;
    this.pickerService.clearSelectedItems(this.pickerConfig);
    this.breadcrumbHeader = this.translateService.translateInstant(ContextSelector.NO_CONTEXT);
    this.config.onContextSelected();
  }

  getContext(id) {
    if (!this.parentExists(id)) {
      this.clearContext();
      return;
    }

    this.instanceRestService.load(id).then(idoc => {
      this.breadcrumbHeader = idoc.data.headers[HEADER_BREADCRUMB];
      this.config.parentId = id;
    }).catch(() => {
      this.clearContext();
    });
  }

  /**
   * Determines if the provided instance ID is for existing parent instance or for one that is not yet persisted.
   * @param id - the parent instance ID
   * @returns {boolean} true if the instance ID is for persisted one or false otherwise
   */
  parentExists(id) {
    return id && !InstanceUtils.isTempId(id);
  }

}

ContextSelector.NO_CONTEXT = 'context.selector.no.context';
