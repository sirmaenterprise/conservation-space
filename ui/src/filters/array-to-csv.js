import {Filter} from 'app/app';

@Filter
export class ArrayToCSV {
  filter(value) {
    let _value = value || '';
    if(_value.constructor === Array) {
      _value = _value.filter((val) => { return val; }).join(', ');
    }
    return _value;
  }
}