import {Injectable, Inject} from 'app/app';
import {FieldValidator} from 'form-builder/validation/field-validator';
import {PropertiesRestService} from 'services/rest/properties-service';
import {ValidationService} from 'form-builder/validation/validation-service';
import {TranslateService} from 'services/i18n/translate-service';
import {Logger} from 'services/logging/logger';
import {VERSION_PART_ID, REVISION_PART_ID} from 'idoc/idoc-constants';

/**
 * This validator is added for the fields which unique property is set to true.
 * It checks if the current value is not already in use.
 *
 * @author NikolayCh
 */
@Injectable()
@Inject(PropertiesRestService, TranslateService, Logger)
export class Unique extends FieldValidator {

  constructor(propertiesService, translateService, logger) {
    super();
    this.propertiesService = propertiesService;
    this.translateService = translateService;
    this.logger = logger;
  }

  validate(fieldName, validatorDef, validationModel, flatModel, formControl, defId, objectId) {

    let instanceId = objectId ? objectId : '';
    // Revisions and versions shouldn't have unique properties with their main object.
    instanceId = instanceId.replace(VERSION_PART_ID, '');
    instanceId = instanceId.replace(REVISION_PART_ID, '');

    let definitionId = defId;
    let value = this.getViewValueAsString(fieldName, validationModel);

    return this.propertiesService.checkFieldUniqueness(definitionId, instanceId, fieldName, value)
      .then((response) => {
        return this.processResponse(value, response, fieldName, validationModel, validatorDef);
      })
      .catch((error) => {
        this.logger.log(error);
      });
  }

  /**
   * By the time the response is received the user could have altered the value.
   * The method compares the sent and the current values and only if they are the same
   * checks the response and eventually set's the error label.
   *
   * @param sentValue the sent value
   * @param response the response
   * @param fieldName the name of the unique field
   * @param validationModel the model
   * @param validatorDef the validator definition
   */
  processResponse(sentValue, response, fieldName, validationModel, validatorDef) {
    let unique = response.data.unique;
    if (sentValue === this.getViewValueAsString(fieldName, validationModel) && !unique) {
      ValidationService.processAsyncValidation(unique, fieldName, validationModel, validatorDef, this.translateService);
      return unique;
    } else {
      ValidationService.processAsyncValidation(true, fieldName, validationModel, validatorDef, this.translateService);
      return true;
    }
  }

}