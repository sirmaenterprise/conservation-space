import {IconsService} from 'services/icons/icons-service';
import Paths from 'common/paths';

describe('IconsService', () => {

  var service;
  beforeEach(() => {
    service = new IconsService();
  });

  describe('getIconForInstance()', () => {
    it('should return url for document icon by default', () => {
      expect(service.getIconForInstance()).to.contains('images/instance-icons/documentinstance-icon-16.png');
    });

    it('should construct url based on the provided parameters', () => {
      expect(service.getIconForInstance('taskinstance', 8)).to.contains('images/instance-icons/taskinstance-icon-8.png');
    });
  });
});