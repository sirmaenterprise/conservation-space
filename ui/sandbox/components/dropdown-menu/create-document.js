import {Injectable, Inject} from 'app/app';
import {ActionHandler} from 'services/actions/action-handler';

@Injectable()
export class CreateDocument extends ActionHandler {

  execute(actionDefinition, context) {
    let p = document.createElement("p");
    p.classList.add('test');
    let text = document.createTextNode('TEST');
    p.appendChild(text);
    document.body.appendChild(p);
  }
}