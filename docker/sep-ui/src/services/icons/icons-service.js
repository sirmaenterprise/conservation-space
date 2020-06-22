import {Injectable} from 'app/app';
import {HEADER_DEFAULT,HEADER_BREADCRUMB,HEADER_COMPACT} from 'instance-header/header-constants';
import Paths from 'common/paths';

@Injectable()
export class IconsService {

  getIconForInstance(instanceType = 'documentinstance', iconSize = 16) {
    return `${Paths.getBaseScriptPath()}images/instance-icons/${instanceType}-icon-${iconSize}.png`;
  }
}

IconsService.HEADER_ICON_SIZE = {
  [HEADER_BREADCRUMB]: 16,
  [HEADER_COMPACT]: 16,
  [HEADER_DEFAULT]: 64
};
