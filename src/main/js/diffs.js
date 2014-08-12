

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
    var keysA = Object.keys(a);
    var keysB = Object.keys(b);

    for(var i in keysA) {
      var k = keysA[i];
      if(typeof a[k] === "object" && typeof b[k] === "object") {
        var rec = net_liftmodules_ng.diff(a[k], b[k]);
        if(Object.keys(rec.add).length > 0) add[k] = rec.add;
        if(Object.keys(rec.sub).length > 0) sub[k] = rec.sub;
      }
      else if(a[k] !== b[k]) add[k] = a[k];
    }

    for(var i in keysB) {
      var k = keysB[i];
      if(typeof a[k] === "undefined") sub[k] = "";
    }

    return {add:add, sub:sub};
  };
})();