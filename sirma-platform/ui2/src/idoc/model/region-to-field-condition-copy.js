import {Injectable} from 'app/app';
import {Converter} from 'common/convert/converter';
import _ from 'lodash';

/**
 * Copies condition validators from region level to region fields level. This is needed because objects might be
 * represented in datatable widget where the model is flat and regions are not present. Applying the region conditions
 * allows they to be executed for every separated field.
 */
@Injectable()
export class RegionToFieldConditionCopy extends Converter {

  convert(data) {
    let viewModel = data.viewModel;
    viewModel.fields.forEach((field) => {
      // if is region
      if(field.fields) {
        field.validators.forEach((validator) => {
          if(validator.id === 'condition') {
            field.fields.forEach((regionField) => {
              // should not duplicate validators for consecutive calls on same model
              let exists = _.includes(regionField.validators, validator);
              if(!exists) {
                regionField.validators.push(validator);
              }
            });
          }
        });
      }
    });
  }
}