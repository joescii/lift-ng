describe('lift-ng', function(){

  describe('Plumbing', function(){
    var rootScope;
    var plumbing;

    beforeEach(module('lift-ng'));

    beforeEach(inject(['$rootScope', 'plumbing', function(r, p){
      rootScope = r;
      plumbing = p;
    }]));

    it('should handle unrecognized data in fulfill()', function() {
      plumbing.fulfill({garbage:"in"})
    });

    it('should resolve data with success==true', function(){
      var pair = plumbing.createDefer();
      var defer = pair[0];
      var id = pair[1];
      var data = {
        success: true,
        data: "string"
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBe("string") })
        .catch(function(err) { expect(err).toBeNull() });

      rootScope.$apply();
    });

    it('should reject data with success==false', function(){
      var pair = plumbing.createDefer();
      var defer = pair[0];
      var id = pair[1];
      var data = {
        success: false,
        data: "string",
        msg: "failed"
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBeNull() })
        .catch(function(msg) { expect(msg).toBe("failed") });

      rootScope.$apply();
    });

    it('should resolve with empty data', function(){
      var pair = plumbing.createDefer();
      var defer = pair[0];
      var id = pair[1];
      var data = {
        success: true
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBeUndefined() })
        .catch(function(msg) { expect(msg).toBeNull() });

      rootScope.$apply();
    });

    it('should resolve with data == false', function(){
      var pair = plumbing.createDefer();
      var defer = pair[0];
      var id = pair[1];
      var data = {
        success: true,
        data: false
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBe(false) })
        .catch(function(msg) { expect(msg).toBeNull() });

      rootScope.$apply();
    });

    it('should allow providing an id for the defer', function(){
      var id = "myDefer";
      var defer = plumbing.createDefer(id);
      var data = {
        success: true,
        data: "string"
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBe("string") })
        .catch(function(err) { expect(err).toBeNull() });

      rootScope.$apply();
    });

    it('should inject promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.future": true,
          "id": "NG1234"
        }
      };
      var data = {
        success: true,
        data: "from future"
      };

      plumbing.inject(model);

      plumbing.fulfill(data, "NG1234");

      model.f.then(function(val){ expect(val).toBe("from future") });
      expect(model.str).toBe("a string");

      rootScope.$apply();
    });

    it('should inject promises in contained objects', function() {
      var model = {
        str: "a string",
        obj: {
          num: 42,
          f: {
            "net.liftmodules.ng.Angular.future": true,
            "id": "NG1234"
          }
        }
      };
      var data = {
        success: true,
        data: "from future"
      };

      plumbing.inject(model);

      plumbing.fulfill(data, "NG1234");

      model.obj.f.then(function(val){ expect(val).toBe("from future") });
      expect(model.str).toBe("a string");
      expect(model.obj.num).toBe(42);

      rootScope.$apply();
    });

    it('should inject promises in arrays', function() {
      var arr = [
        {"net.liftmodules.ng.Angular.future": true, id: "NG1234"},
        {"net.liftmodules.ng.Angular.future": true, id: "NG1235"}
      ];
      var data1 = {
        success: true,
        data: "from future"
      };
      var data2 = {
        success: true,
        data: "other future"
      };

      plumbing.inject(arr);

      plumbing.fulfill(data1, "NG1234");
      plumbing.fulfill(data2, "NG1235");

      arr[0].then(function(val){ expect(val).toBe("from future") });
      arr[1].then(function(val){ expect(val).toBe("other future") });

      rootScope.$apply();
    });

    it('should inject promises in contained arrays', function() {
      var model = {
        str: "a string",
        arr: [
          {"net.liftmodules.ng.Angular.future": true, id: "NG1234"},
          {"net.liftmodules.ng.Angular.future": true, id: "NG1236"}
        ]
      };
      var data1 = {
        success: true,
        data: "whatever"
      };
      var data2 = {
        success: true,
        data: "blah"
      };

      plumbing.inject(model);
      plumbing.fulfill(data1, "NG1234");
      plumbing.fulfill(data2, "NG1236");

      model.arr[0].then(function(val){ expect(val).toBe("whatever") });
      model.arr[1].then(function(val){ expect(val).toBe("blah") });
      expect(model.str).toBe("a string");

      rootScope.$apply();
    });

    it('should inject fulfilled promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.future": true,
          data: "y'all"
        }
      };

      plumbing.inject(model);

      model.f.then(function(val){ expect(val).toBe("y'all") });
      expect(model.str).toBe("a string");

      rootScope.$apply();
    });

    it('should inject failed promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.future": true,
          msg: "shit, y'all!"
        }
      };

      plumbing.inject(model);

      model.f.catch(function(val){ expect(val).toBe("shit, y'all!") });
      expect(model.str).toBe("a string");

      rootScope.$apply();
    });

    it('should inject fulfilled promises in the base level for the empty case', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.future": true
        }
      };

      plumbing.inject(model);

      model.f.then(function(val){ expect(val).toBeUndefined });
      expect(model.str).toBe("a string");

      rootScope.$apply();
    });

  })
})
;