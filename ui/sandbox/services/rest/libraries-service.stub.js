import { Injectable, Inject} from 'app/app';
import { PromiseAdapter } from 'adapters/angular/promise-adapter';

@Injectable()
@Inject(PromiseAdapter)
export class LibrariesService {
  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  loadLibraries() {
    return this.promiseAdapter.resolve(
      {
        data: {
          values: [{
            default_header: '↵<span><span class="banner label label-warning"></span><br /><span class="truncate-element"><a href="/#/idoc/emf:firstLibrary" class="instance-link has-tooltip" uid=""><b><span data-property="type">(classDefinition) </span><span data-property="title">Audio</span></b></a></span><br />Last modified by: <a href="javascript:void(0)"></a></span>',
            label: "Audio",
            name: "emf:Audio"
          }, {
            default_header: '↵<span><span class="banner label label-warning"></span><br /><span class="truncate-element"><a href="/#/idoc/emf:secondLibrary" class="instance-link has-tooltip" uid=""><b><span data-property="type">(classDefinition) </span><span data-property="title">Audio</span></b></a></span><br />Last modified by: <a href="javascript:void(0)"></a></span>',
            label: "Video",
            name: "emf:Video"
          }]
        }
      }
    );
  }
}