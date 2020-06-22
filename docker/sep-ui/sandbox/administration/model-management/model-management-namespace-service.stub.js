import {Injectable, Inject} from 'app/app';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

import _ from 'lodash';

const LONG_TO_SHORT = 0;
const SHORT_TO_LONG = 1;

@Injectable()
@Inject(PromiseAdapter)
export class NamespaceService {

  constructor(promiseAdapter) {
    this.promiseAdapter = promiseAdapter;
  }

  convertToShortURI(uris) {
    return this.promiseAdapter.resolve({data: this.getConvertedResult(uris, LONG_TO_SHORT)});
  }

  convertToFullURI(uris) {
    return this.promiseAdapter.resolve({data: this.getConvertedResult(uris, SHORT_TO_LONG)});
  }

  getConvertedResult(uris, conversion) {
    return _.transform(uris, (result, val) => result[val] = this.getTransformedUri(val, conversion), {});
  }

  getTransformedUri(uri, conversion) {
    let actualDelimiter = NamespaceService.DELIMITERS[conversion | 0];
    let inversedDelimiter = NamespaceService.DELIMITERS[!conversion | 0];

    let idx = this.getMatchingDelimiterIdx(uri, inversedDelimiter);
    let components = [uri.substring(0, idx), uri.substring(idx + 1)];
    let delimiter = actualDelimiter[inversedDelimiter.indexOf(uri[idx])];

    return NamespaceService.NAMESPACES[conversion][components[0]] + delimiter + components[1];
  }

  getMatchingDelimiterIdx(uri, delimiters) {
    let idx = -1;
    delimiters.some(char => {
      idx = uri.lastIndexOf(char);
      return idx !== -1;
    });
    return idx;
  }
}

NamespaceService.DELIMITERS = {
  [SHORT_TO_LONG]: ['#', '/'],
  [LONG_TO_SHORT]: [':', ':']
};

NamespaceService.NAMESPACES = {
  [SHORT_TO_LONG]: {
    'dc': 'http://purl.org/dc/terms',
    'owl': 'http://www.w3.org/ns/oa',
    'skos': 'http://www.w3.org/2008/05/skos',
    'test': 'http://www.w3.org/2004/02/test',
    'proton': 'http://www.ontotext.com/proton/protontop',
    'emf': 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework',
  },
  [LONG_TO_SHORT]: {
    'http://purl.org/dc/terms': 'dc',
    'http://www.w3.org/ns/oa': 'owl',
    'http://www.w3.org/2008/05/skos': 'skos',
    'http://www.w3.org/2004/02/test': 'test',
    'http://www.ontotext.com/proton/protontop': 'proton',
    'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework': 'emf'
  }
};

