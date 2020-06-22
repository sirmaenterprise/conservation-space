import {Injectable, Inject} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {Configuration} from 'common/application-config';
import {MomentAdapter} from 'adapters/moment-adapter';
import {InstanceRestService} from 'services/rest/instance-service';
import {ContentRestService} from 'services/rest/content-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {StatusCodes} from 'services/rest/status-codes';
import {ModelUtils} from 'models/model-utils';
import {TOPIC as INFO_AREA_TOPIC} from 'idoc/info-area/info-area';
import base64 from 'common/lib/base64';

const BTN_RESUME = 'RESUME';
const BTN_DISCARD = 'DISCARD';
const DRAFT_INFO_MESSAGE_ID = 'draft-info-message';

/**
 * Service for performing draft operations on an idoc
 */
@Injectable()
@Inject(InstanceRestService, ContentRestService, Eventbus, MomentAdapter, Configuration, PromiseAdapter, DialogService, TranslateService)
export class IdocDraftService {
  constructor(instanceRestService, contentRestService, eventbus, momentAdapter, configuration, promiseAdapter, dialogService, translateService) {
    this.instanceRestService = instanceRestService;
    this.contentRestService = contentRestService;
    this.eventbus = eventbus;
    this.momentAdapter = momentAdapter;
    this.promiseAdapter = promiseAdapter;
    this.dialogService = dialogService;
    this.translateService = translateService;
    this.datePattern = configuration.get(Configuration.UI_DATE_FORMAT) + ' ' + configuration.get(Configuration.UI_TIME_FORMAT);
  }

  saveDraft(idocContext, contentAsString) {
    let content = $(contentAsString);
    let changesets = {};
    idocContext.getAllSharedObjects(true).forEach((object) => {
      if (object.isChanged()) {
        changesets[object.id] = object.getChangeset(true);
      }
    });

    let draftData = {
      changesets
    };

    // jqLite doesn't support data method. Using native dataset api instead
    content[0].dataset['draftData'] = base64.encode(JSON.stringify(draftData));
    return this.instanceRestService.createDraft(idocContext.getCurrentObjectId(), content.prop('outerHTML')).then((result) => {
      let draftMessage = this.translateService.translateInstantWithInterpolation('draft.saved.message', {
        draftCreatedOn: this.momentAdapter.format(new Date(result.data.draftCreatedOn), this.datePattern)
      });
      this.publishInfoAreaMessage(idocContext.getUUID(), draftMessage);
    });
  }

  loadDraft(idocContext) {
    return this.promiseAdapter.promise((resolve, reject) => {
      this.instanceRestService.loadDraft(idocContext.getCurrentObjectId()).then((draft) => {
        if (draft && draft.status === StatusCodes.SUCCESS) {
          let confirmationMessage = this.translateService.translateInstantWithInterpolation('draft.resume.confirmation', {
            draftCreatedOn: this.momentAdapter.format(new Date(draft.data.draftCreatedOn), this.datePattern)
          });
          this.dialogService.confirmation(confirmationMessage, undefined, {
            showClose: false,
            buttons: [
              {id: BTN_RESUME, label: 'draft.btn.resume', cls: 'btn-primary'},
              {id: BTN_DISCARD, label: 'draft.btn.discard'}
            ],
            onButtonClick: (buttonID, componentScope, dialogConfig) => {
              this.onConfirmationButtonClick(buttonID, componentScope, dialogConfig, idocContext, draft.data.draftContentId).then((result) => {
                resolve(result);
              });
            }
          });
        } else {
          resolve({loaded: false});
        }
      }).catch(reject);
    });
  }

  onConfirmationButtonClick(buttonID, componentScope, dialogConfig, idocContext, contentId) {
    return this.promiseAdapter.promise((resolve, reject) => {
      if (buttonID === BTN_RESUME) {
        this.contentRestService.getContent(contentId).then((view) => {
          let content = $(view.data);
          let draftData = JSON.parse(base64.decode(content[0].dataset['draftData']));
          delete content[0].dataset['draftData'];
          idocContext.getSharedObjects(Object.keys(draftData.changesets), null).then((sharedObjects) => {
            sharedObjects.data.forEach((sharedObject) => {
              // Update value directly instead of merging in order to avoid defaultValue update
              let changeset = draftData.changesets[sharedObject.id];
              Object.keys(changeset).forEach((propertyName) => {
                if (sharedObject.models.validationModel.hasOwnProperty(propertyName)) {
                  if (ModelUtils.isRichtext(ModelUtils.flatViewModel(sharedObject.models.viewModel).get(propertyName))) {
                    sharedObject.models.validationModel[propertyName].richtextValue =  changeset[propertyName];
                    sharedObject.models.validationModel[propertyName].value =  ModelUtils.stripHTML(changeset[propertyName]);
                  } else {
                    sharedObject.models.validationModel[propertyName].value = changeset[propertyName];
                  }
                }
              });
            });
            dialogConfig.dismiss();
            resolve({loaded: true, content: content.prop('outerHTML')});
          });
        }).catch(reject);
      } else {
        this.deleteDraft(idocContext).then(() => {
          dialogConfig.dismiss();
          resolve({loaded: false});
        });
      }
    });
  }

  deleteDraft(idocContext) {
    return this.instanceRestService.deleteDraft(idocContext.getCurrentObjectId()).then(() => {
      this.publishInfoAreaMessage(idocContext.getUUID());
    });
  }

  publishInfoAreaMessage(channel, message) {
    this.eventbus.publish({
      channel,
      topic: INFO_AREA_TOPIC,
      data: {
        id: DRAFT_INFO_MESSAGE_ID,
        message
      }
    });
  }
}
