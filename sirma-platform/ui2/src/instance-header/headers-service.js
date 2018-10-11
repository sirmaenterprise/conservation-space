import {Injectable, Inject} from 'app/app';
import {InstanceRestService} from 'services/rest/instance-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {Logger} from 'services/logging/logger';
import {ModelUtils} from 'models/model-utils';

/**
 * Utility methods for instance headers loading.
 */
@Injectable()
@Inject(InstanceRestService, Logger, PromiseAdapter)
export class HeadersService {

  constructor(instanceRestService, logger, promiseAdapter) {
    this.instanceRestService = instanceRestService;
    this.promiseAdapter = promiseAdapter;
    this.logger = logger;
  }

  /**
   * Loads headers for instances and maps them by instance id.
   *
   * @param ids The instance ids for which to load headers.
   * @param headerType One of HeaderConstants INSTANCE_HEADERS constants
   * @param headers An optional object containing already loaded headers of given type. If provided, then loaded
   *  headers would be mapped to appropriate id in that object. Otherwise a new empty object will be created.
   *
   * @returns An object with loaded headers in format:
   *  { instanceId: { id: 'instanceId', compact_header: 'loaded-header', default_header: 'loaded-default-header' }, ... }
   */
  loadHeaders(ids = [], headerType, headers) {
    if (ids.length === 0) {
      return this.promiseAdapter.resolve(headers || {});
    }
    return this.instanceRestService.loadBatch(ids, {params: {properties: [headerType]}})
      .then((instances) => {
        return this.headersLoaded(instances, headerType, headers);
      }).catch((error) => {
        this.logger.error(error);
      });
  }

  headersLoaded(instances, headerType, headers) {
    let loadedHeaders = headers || {};
    instances.data.forEach((instance) => {
      let requiredHeader = instance.headers[headerType];

      ModelUtils.updateObjectPropertyHeaders({ headers: loadedHeaders }, instance.id, headerType, requiredHeader);
    });
    return loadedHeaders;
  }
}