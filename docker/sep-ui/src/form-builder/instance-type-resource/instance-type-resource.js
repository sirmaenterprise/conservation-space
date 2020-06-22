import {FormControl} from 'form-builder/form-control';
import {EventEmitter} from 'common/event-emitter';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {MODE_PREVIEW} from 'idoc/idoc-constants';

/**
 * Base class for simple instance selector type form controls.
 */
export class InstanceTypeResource extends FormControl {
  constructor($element) {
    super();
    this.$element = $element;
    this.eventEmitter = new EventEmitter();
    this.instanceSelectorConfig = {
      selection: MULTIPLE_SELECTION,
      mode: MODE_PREVIEW,
      objectId: this.objectId,
      propertyName: this.fieldViewModel.identifier,
      eventEmitter: this.eventEmitter
    };

    let loadedSubscription = this.eventEmitter.subscribe('instanceSelectorRendered', () => {
      loadedSubscription.unsubscribe();
      this.formEventEmitter.publish('formControlLoaded', {identifier: this.fieldViewModel.identifier});
    });
  }
}