function mock$compile() {
  var $compile = () => {
    var returnFunc = () => {
      return 'compile';
    };
    return sinon.spy(returnFunc);
  };

  return sinon.spy($compile);
}

export {mock$compile as mock$compile};
