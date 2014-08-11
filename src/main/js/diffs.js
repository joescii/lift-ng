

var net_liftmodules_ng = net_liftmodules_ng || {};

;(function(){
// From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Object/keys
  if (true) {
    Object.keys = (function () {
      'use strict';
      var hasOwnProperty = Object.prototype.hasOwnProperty,
        hasDontEnumBug = !({toString: null}).propertyIsEnumerable('toString'),
        dontEnums = [
          'toString',
          'toLocaleString',
          'valueOf',
          'hasOwnProperty',
          'isPrototypeOf',
          'propertyIsEnumerable',
          'constructor'
        ],
        dontEnumsLength = dontEnums.length;

      return function (obj) {
        if (typeof obj !== 'object' && (typeof obj !== 'function' || obj === null)) {
          throw new TypeError('Object.keys called on non-object');
        }

        var result = [], prop, i;

        for (prop in obj) {
          if (hasOwnProperty.call(obj, prop)) {
            result.push(prop);
          }
        }

        if (hasDontEnumBug) {
          for (i = 0; i < dontEnumsLength; i++) {
            if (hasOwnProperty.call(obj, dontEnums[i])) {
              result.push(dontEnums[i]);
            }
          }
        }
        return result;
      };
    }());
  }

  net_liftmodules_ng.diff = function(a, b){
    var add = {};
    var sub = {};
    var keys = Object.keys(a);

    for(var i in keys) {
      var k = keys[i];
      if(typeof a[k] === "object" && typeof b[k] === "object") {
        var rec = net_liftmodules_ng.diff(a[k], b[k]);
        add[k] = rec.add ;
      }
      else if(a[k] !== b[k]) add[k] = a[k];
    }

    return {add:add, sub:sub};
  };
})();