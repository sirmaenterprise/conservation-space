import 'jquery';
import 'select2';

$.fn.select2.amd.define('select2/data/CustomAdapter', ['select2/data/ajax', 'select2/utils'], function (AjaxAdapter, Utils) {

  function CustomDataAdapter($element, options) {
    CustomDataAdapter.__super__.constructor.call(this, $element, options);
    this.init();
  }

  Utils.Extend(CustomDataAdapter, AjaxAdapter);

  CustomDataAdapter.prototype.init = function () {
    let opts = this.options.options;
    if (opts.defaultValue) {
      this.current(() => {
      }, Array.isArray(opts.defaultValue) ? opts.defaultValue : [opts.defaultValue]);
      return;
    }

    if (opts.defaultToSingleValue) {
      this.query({}, (data) => {
        if (data && data.results && data.results.length === 1) {
          this.current(() => {
          }, [data.results[0].id]);
        }
      });
      return;
    }

    if (opts.defaultToFirstValue) {
      this.query({}, (data) => {
        if (data && data.results && data.results.length) {
          this.current(() => {
          }, [data.results[0].id]);
        }
      });
      return;
    }
  };

  CustomDataAdapter.prototype._populateCache = function () {
    let ajax = this.options.get('ajax');
    let transport = ajax.transport;
    let processResults = ajax.processResults;

    let populate = (response) => {
      let results = response;
      if (processResults) {
        results = processResults(response).results;
      }

      for (let result of results) {
        this._cache[result.id] = result;
      }
    };

    return new Promise((resolve) => {
      if (this._cache) {
        resolve();
        return;
      }
      this._cache = {};
      transport(null, (response) => {
        populate(response);
        resolve();
      });
    });
  }

  CustomDataAdapter.prototype._map = function (ids) {
    let mapper = this.options.get('mapper');
    if (mapper) {
      return mapper(ids);
    }

    return new Promise((resolve) => {
      this._populateCache().then(() => {
        let results = [];
        let idsArray = Array.isArray(ids) ? ids : [ids];
        for (let id of idsArray) {
          let item = this._cache[id];
          if (item) {
            results.push(item);
          }
        }
        resolve(results);
      });
    });
  }

  CustomDataAdapter.prototype.current = function (callback, defaultValue) {
    let value = this.options.get('value') || defaultValue;
    if (value) {
      this._map(value).then((mapped) => {
        for (let item of mapped) {
          this.select(item);
        }
        callback(mapped);
      });
      this.options.set('value', null);
    } else {
      AjaxAdapter.prototype.current.call(this, callback);
    }
  };

  CustomDataAdapter.prototype.selectValue = function (value) {
    if (value) {
      return this._map(value).then((mapped) => {
        for (let item of mapped) {
          this.select(item);
        }
      });
    } else {
      return Promise.resolve();
    }
  };

  return CustomDataAdapter;
});

export const CustomDataAdapter = $.fn.select2.amd.require('select2/data/CustomAdapter');
