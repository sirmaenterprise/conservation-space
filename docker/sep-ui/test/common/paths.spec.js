import Paths from 'common/paths';

describe('Paths', function () {
  it('should get the base script loading path out of systemjs config', function () {
    // Warning: this may break parallel executed tests
    var systemJsConfig = System.paths['*'];

    System.paths['*'] = 'build/*.js';
    expect(Paths.getBaseScriptPath()).to.equal('/build/');

    System.paths['*'] = '*.js';
    expect(Paths.getBaseScriptPath()).to.equal('/');

    //roll back systemjs config not to break other tests
    System.paths['*'] = systemJsConfig;
  });

});