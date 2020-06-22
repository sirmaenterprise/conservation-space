import {HtmlUtil} from 'common/html-util';

describe('HtmlUtil', () => {
  it('should strip html from text properly', () => {
    let strippedText = HtmlUtil.stripHtml(`<p></p><ol><li><strong>Lorem ipsum dolor sit amet, consectetur</strong> adipiscing elit. Vivamus sodales luctus <span style="color:#2ecc71;">eros, sit amet</span> <em>malesuada massa euismod id. Nam fermentum</em> sapien et<br></li><li>risus malesuada rhoncus. Phasellus vel quam quam. Aenean nec lectus eget augue lobortis tristique sit amet quis <a data-cke-saved-href="http://www.test.com" target="_blank" href="http://www.test.com">turpis. Praesent id dui nunc. Nunc erat nibh, tincidunt ultricies sapien vel, facilis</a>is posuere augue. Fusce eget sagittis ante, ut luctus ligula. Maecenas laoreet ullamcorper nulla a fermentum. Maecena<span style="background-color:#c0392b;">s neque ex, semper mollis volutpat at, faucibus vitae enim. Praesent commodo vehicula lacus, nec pellentesque arcu lobortis ac</span></li></ol></p>`);
    let expectedText = 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus sodales luctus eros, sit amet malesuada massa euismod id. Nam fermentum sapien etrisus malesuada rhoncus. Phasellus vel quam quam. Aenean nec lectus eget augue lobortis tristique sit amet quis turpis. Praesent id dui nunc. Nunc erat nibh, tincidunt ultricies sapien vel, facilisis posuere augue. Fusce eget sagittis ante, ut luctus ligula. Maecenas laoreet ullamcorper nulla a fermentum. Maecenas neque ex, semper mollis volutpat at, faucibus vitae enim. Praesent commodo vehicula lacus, nec pellentesque arcu lobortis ac';
    expect(strippedText).to.equal(expectedText);
  });

  it('should remove given elements from HTML string', () => {
    expect(HtmlUtil.removeElements('<span>Hello<script>alert("world")</script><a href="#">Invalid link</a></span>there', ['script', 'a'])).to.equals('<span>Hello</span>there');
  });

  it('should escape html tags', () => {
    expect(HtmlUtil.escapeHtml('<video><source onerror="alert(1)">')).to.equal('&lt;video&gt;&lt;source onerror="alert(1)"&gt;');
  });
});
