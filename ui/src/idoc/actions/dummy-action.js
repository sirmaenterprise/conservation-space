import {Injectable} from 'app/app';

/**
 * This dummy action handler is assigned to all not implemented actions because it was required to show all off them.
 */
@Injectable()
export class DummyAction {

  execute() {
    alert('Not implemented action!');
    throw new Error('Not implemented action!');
  }
}