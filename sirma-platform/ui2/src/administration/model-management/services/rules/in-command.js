import {Injectable} from 'app/app';
import {Command} from 'common/command-chain/command-chain';

/**
 * Command which can handle rules "IN" operation
 *
 * @author Stela Djulgerova
 */
@Injectable()
export class InCommand extends Command {
  canHandle(data) {
    return data.operation === 'in';
  }

  handle(data) {
    return data.values.indexOf(data.value) >= 0;
  }
}