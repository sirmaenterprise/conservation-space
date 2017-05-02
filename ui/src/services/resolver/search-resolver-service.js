import {Inject, Injectable} from 'app/app';
import {PluginsService} from 'services/plugin/plugins-service';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';

/**
 * Service for modifying provided search tree before performing a search. Loads all resolvers registered under the
 * <b>'search-resolvers'</b> name and cycles the search tree through them.
 *
 * The service returns a promise which will resolve once all resolvers are done.
 *
 * @author Mihail Radkov
 */
@Injectable()
@Inject(PluginsService, PromiseAdapter)
export class SearchResolverService {

  constructor(pluginsService, promiseAdapter) {
    this.pluginsService = pluginsService;
    this.promiseAdapter = promiseAdapter;
  }

  resolve(tree, context) {
    if (!this.resolvers) {
      return this.loadResolvers().then((resolvers) => {
        this.resolvers = resolvers;
        return this.resolveInternal(tree, context);
      });
    } else {
      return this.resolveInternal(tree, context);
    }
  }

  resolveInternal(tree, context) {
    var resolverPromises = this.resolvers.map((resolver) => {
      return resolver.resolve(tree, context);
    });

    return this.promiseAdapter.all(resolverPromises);
  }

  loadResolvers() {
    return this.pluginsService.loadPluginServiceModules('search-resolvers', 'component').then((resolvers) => {
      return Object.keys(resolvers).map((key) => {
        return resolvers[key];
      });
    });
  }
}