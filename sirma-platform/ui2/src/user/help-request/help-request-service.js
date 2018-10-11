import {Injectable, Inject} from 'app/app';
import {ActionExecutor} from 'services/actions/action-executor';
import {TranslateService} from 'services/i18n/translate-service';

@Injectable()
@Inject(ActionExecutor, TranslateService)
export class HelpRequestService {

  constructor(actionExecutor, translateService) {
    this.actionExecutor = actionExecutor;
    this.translateService = translateService;
  }

  openDialog() {
    let action = {
      action: 'reportIssue',
      configuration: {
        contextSelectorDisabled: true,
        predefinedTypes: ['emf:Issue'],
        openInNewTab: true
      },
      cssClass: 'seip-action-reportIssue',
      disableButton: false,
      disabled: false,
      extensionPoint: 'settings-menu',
      id: 'reportIssue',
      label: this.translateService.translateInstant('report.issue.action.label'),
      name: 'createInstanceAction'
    };

    let context = {
      currentObject: {
      }
    };
    this.actionExecutor.execute(action, context);
  }
}