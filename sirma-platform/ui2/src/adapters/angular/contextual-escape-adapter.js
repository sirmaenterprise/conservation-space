import {Inject, Injectable} from 'app/app';

@Injectable()
@Inject('$sce')
export class ContextualEscapeAdapter {

  constructor($sce) {
    this.$sce = $sce;
  }

  trustAsHtml(value) {
    return this.$sce.trustAsHtml(value);
  }

  trustAsResourceUrl(url) {
    return this.$sce.trustAsResourceUrl(url);
  }
}