import {Injectable} from 'app/app';

/**
 * Removes any operators that are not specified in the property (if <code>operators</code> array field is defined).
 *
 * @author Mihail Radkov
 */
@Injectable()
export class ExcludedOperatorFilter {

  filter(config, property, operator) {
    if(property.operators) {
      return property.operators.indexOf(operator.id) > -1;
    }
    return true;
  }

}
