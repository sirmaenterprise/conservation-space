import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {TabsService} from 'idoc/idoc-tabs/tabs-service';

@Injectable()
@Inject(TabsService, PromiseAdapter)
export class ConfigureIdocTabs extends ActionHandler {

  constructor(tabsService, promiseAdapter) {
    super();
    this.tabsService = tabsService;
    this.promiseAdapter = promiseAdapter;
  }

  execute(actionDefinition, context) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let tab = $.grep(context.tabs, function (e) {
        return e.id === context.activeTabId;
      });
      this.tabsService.openConfigureTabDialog(tab[0], context, resolve, reject);
    });
  }
}