import {ThemeBootstrapService} from 'services/theme/ui-theme-service';
import {ThemeRestClient} from 'services/rest/ui-theme-rest-service';
import {stub} from 'test/test-utils';

describe('ThemeBootstrapService', () => {
  let themeRestClient = stub(ThemeRestClient);
  let themeBootstrapService;

  beforeEach(() => {
    themeBootstrapService = new ThemeBootstrapService(themeRestClient);
  });

  describe('#processStyles', () => {
    it('should process all styles that need processing', () => {
      let styles = {
        brand_primary_color : '#008e8e',
        brand_secondary_color:'#003f3f',
        button_color:'#8e0071',
        button_disabled_color:'#f2ff00',
        button_hover_color:'#8e0000',
        link_color:'#8e0071',
        link_disabled_color:'#f2ff00',
        link_hover_color:'#8e0000',
        tooltip_color:'#008e8e'
      };
      let actualStyles = themeBootstrapService.processStyles(styles);
      expect(actualStyles).to.eql({
        brand_primary_color : '#008e8e',
        brand_secondary_color:'#003f3f',
        button_color:'#8e0071',
        button_color_dark: '#75005d',
        button_disabled_color:'#f2ff00',
        button_disabled_color_dark:'#dae600',
        button_hover_color:'#8e0000',
        button_hover_color_dark: "#750000",
        link_color:'#8e0071',
        link_color_darker:'#420034',
        link_disabled_color:'#f2ff00',
        link_hover_color:'#8e0000',
        tooltip_color:'#008e8e'
      });
    });
  });
});