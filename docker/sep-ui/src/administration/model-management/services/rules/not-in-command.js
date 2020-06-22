import {Injectable} from 'app/app';
import {Command} from 'common/command-chain/command-chain';

/**
 * Command which can handle rules of type "NOT_IN". The rule guarantees that given value is not found in provided data.
 *
 * @author svelikov
 */
@Injectable()
export class NotInCommand extends Command {
  canHandle(data) {
    return data.operation === 'not_in';
  }

  handle(data) {
    return data.values.indexOf(data.value) === -1;
  }
}