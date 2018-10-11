import {ActionHandler} from 'services/actions/action-handler';
import {Injectable, Inject} from 'app/app';
import {Mailbox} from 'idoc/system-tabs/mailbox/mailbox';
import {DialogService} from 'components/dialog/dialog-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(DialogService, PromiseAdapter, InstanceRestService)
export class ComposeEmailAction extends ActionHandler {
  constructor(dialogService, promiseAdapter, instanceRestService) {
    super();
    this.dialogService = dialogService;
    this.promiseAdapter = promiseAdapter;
    this.instanceRestService = instanceRestService;
  }

  execute(actionDefinition, {currentObject, idocContext}) {
    return this.promiseAdapter.promise((resolve, reject) => {
      let dialogConfig = {
        header: 'idoc.compose.email.header',
        showClose: true,
        backdrop: 'static',
        largeModal: true,
        onClose: () => {
          reject();
        }
      };

      let mailboxConfig = {
        context: idocContext,
        mailboxViewType: 'compose',
      };

      if (!idocContext) {
        // if the action is executed not in the current object context email address must be fetched
        this.instanceRestService.load(currentObject.id, {params: {properties: [Mailbox.EMAIL_ADDRESS_CONST, Mailbox.TITLE_CONST]}}).then((response) => {
          let emailAddress = response.data.properties[Mailbox.EMAIL_ADDRESS_CONST];
          let title = response.data.properties[Mailbox.TITLE_CONST];
          mailboxConfig.context = {
            getCurrentObject: () => {
              return this.promiseAdapter.resolve({
                id: currentObject.id,
                getModels: () => {
                  return {
                    validationModel: {
                      [Mailbox.EMAIL_ADDRESS_CONST]: {
                        value: emailAddress
                      },
                      [Mailbox.TITLE_CONST]: {
                        value: title
                      }
                    }
                  };
                }
              });
            }
          };
          this.dialogService.create(Mailbox, mailboxConfig, dialogConfig);
        });
      } else {
        this.dialogService.create(Mailbox, mailboxConfig, dialogConfig);
      }
    });
  }

}