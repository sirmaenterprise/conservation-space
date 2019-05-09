import {Component, View} from 'app/app';

import template from './model-errors.html!text';

/**
 *  Component representing a list of error messages. Should have property <code>errors</code>, which is an array
 *  of the error messages. It is intended to be used in DialogService.create() method as a first argument; the second
 *  argument in DialogService.create() method should be an object, which contains a key <code>errors</code> with
 *  a value of array.
 *
 *  @author Radoslav Dimitrov
 */
@Component({
  selector: 'model-errors',
  properties: {
    errors: 'errors'
  }
})
@View({
  template
})
export class ModelErrors {

}
