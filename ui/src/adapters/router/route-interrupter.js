import {Component} from 'app/app';

@Component({
  selector: 'route-interrupter'
})
export class RouteInterrupter {
  constructor() {
    if (typeof this.shouldInterrupt !== 'function') {
      throw new TypeError('The class has no \'shouldInterrupt\' function!');
    }
  }
}
