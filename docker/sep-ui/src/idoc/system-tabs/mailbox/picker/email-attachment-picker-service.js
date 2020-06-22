import {Inject, Injectable} from 'app/app';
import {Configurable} from 'components/configurable';
import {HEADER_DEFAULT, HEADER_BREADCRUMB} from 'instance-header/header-constants';
import {EMF_VERSION} from 'instance/instance-properties';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {PickerService, SEARCH_EXTENSION, RECENT_EXTENSION} from 'services/picker/picker-service';
import _ from 'lodash';

/**
 * Uses the picker to select and extract specific to email attachments data.
 */
@Injectable()
@Inject(PickerService)
export class EmailAttachmentPickerService extends Configurable {
  constructor(pickerService) {
    let defaultConfiguration = {
      extensions: {}
    };
    defaultConfiguration.extensions[SEARCH_EXTENSION] = {
      arguments: {
        properties: ['id', HEADER_DEFAULT, HEADER_BREADCRUMB, EMF_VERSION, 'title']
      },
      results: {config: {selection: MULTIPLE_SELECTION, selectedItems: []}}
    };
    defaultConfiguration.extensions[RECENT_EXTENSION] = {
      propertiesToLoad: ['id', HEADER_DEFAULT, HEADER_BREADCRUMB, EMF_VERSION, 'title']
    };
    super(defaultConfiguration);
    this.pickerService = pickerService;
  }

  selectAttachments() {
    return this.pickerService.configureAndOpen(_.cloneDeep(this.config));
  }
}