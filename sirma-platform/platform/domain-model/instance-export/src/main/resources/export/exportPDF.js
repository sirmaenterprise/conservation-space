/**
 * PhantomJS script to export UI2 URL to PDF.
 */

// README:
// phantomjs default dpi https://github.com/ariya/phantomjs/issues/10659
// phantomjs pdf rendering is zoomed https://github.com/ariya/phantomjs/issues/12685, CMF-20730
// phantomjs configurable dpi which seems to be ignored now https://github.com/ariya/phantomjs/issues/13553

'use strict';

var page = require('webpage').create(),
    system = require('system');

if (system.args.length < 3 || system.args[1] === '--help') {
  printUsage();
  phantom.exit(1);
}

var errorsArray = [];
var url = system.args[1];
var outputFile = system.args[2];

// this seems to be phantoms default dpi for linux systems
var dpi = 72.0,
    dpcm = dpi/2.54;

// A4
var widthCm = 21.0,
    heightCm = 29.7,
    marginCm = 1.5;

page.viewportSize = {
  width: Math.round(widthCm * dpcm),
  height: Math.round(heightCm * dpcm)
};

page.paperSize = {
  width: page.viewportSize.width + 'px',
  height: page.viewportSize.height + 'px',
  margin: Math.round(marginCm * dpcm)
};

page.onError = function(msg, trace) {
  errorsArray.push('Message: ' + msg);
  if (!trace || !trace.length) {
    return;
  }

  errorsArray.push('Trace:');
  trace.forEach(function(t) {
    errorsArray.push('  -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function +')' : ''));
  });
};

addCookies(system.args);

page.open(url, function start(status) {
  if (status !== 'success') {
    writeErrors();
    phantom.exit(1);
  }

  waitForExportReadyStatus(function() {
    page.evaluate(function() {
      // this zoom seems to be the optimal for 72 dpi
      document.querySelector('body').style.zoom = 0.78;
    });

    renderPDF();
    phantom.exit();
  })
});

function waitForExportReadyStatus(callback) {
  var exportInterval = setInterval(function() {
    var status = page.evaluate(function() {
      return window.status;
    });

    if (status === 'export-ready') {
      clearInterval(exportInterval);
      callback();
    }
  }, 500);
}

function renderPDF() {
  page.render(outputFile);
  writeErrors();
}

function writeErrors() {
  if (errorsArray.length > 0) {
    system.stderr.writeLine('PHANTOMJS JAVASCRIPT ERRORS:');
    errorsArray.forEach(function(err) {
      system.stderr.writeLine(err);
    });
  }
}

function addCookies(args) {
  var domain = getArg(args, '--domain');
  args.forEach(function(arg, i) {
    if (arg === '--cookie' && args.length > i + 2) {
      phantom.addCookie({
        'name' : args[i + 1],
        'value' : args[i + 2],
        'domain' : domain
      });
    }
  });
}

/**
 * Obtains command argument's value.
 * For example for command line of type phantom.js exportPDF.js --domain localhost then getArg('--domain') will return 'localhost'.
 * @param args
 * @param argName
 * @returns
 */
function getArg(args, argName) {
  var argIndex = args.indexOf(argName);
  if (argIndex && args.length > argIndex + 1) {
    return args[argIndex + 1];
  }
}

function printUsage() {
  system.stderr.writeLine('Usage: phantom.js exportPDF.js <UI2_URL> <output file> --domain <domain> --cookie <cookie name> <cookie value>');
}