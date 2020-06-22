import {Injectable} from 'app/app';
import {Command} from 'common/command-chain/command-chain';

/**
 * Command which can handle rules "NOT_MATCH" operation
 *
 * @author Stela Djulgerova
 */
@Injectable()
export class NotMatchCommand extends Command {
  canHandle(data) {
    return data.operation === 'not_match';
  }

  handle(data) {
    return !new RegExp(data.values[0]).test(data.value);
  }
}