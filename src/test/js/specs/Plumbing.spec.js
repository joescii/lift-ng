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

    it('should resolve data with state == "resolved"', function(){
      var id = "myDefer";
      var defer = plumbing.createDefer(id);
      var data = {
        state: "resolved",
        data: "string"
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBe("string") })
        .catch(function(err) { expect(err).toBeNull() });

      rootScope.$apply();
    });

    it('should reject data with state == "rejected"', function(){
      var id = "myDefer";
      var defer = plumbing.createDefer(id);
      var data = {
        state: "rejected",
        data: "string"
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBeNull() })
        .catch(function(data) { expect(data).toBe("string") });

      rootScope.$apply();
    });

    it('should resolve with empty data', function(){
      var id = "myDefer";
      var defer = plumbing.createDefer(id);
      var data = {
        state: "resolved"
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBeUndefined() })
        .catch(function(msg) { expect(msg).toBeNull() });

      rootScope.$apply();
    });

    it('should resolve with data == false', function(){
      var id = "myDefer";
      var defer = plumbing.createDefer(id);
      var data = {
        state: "resolved",
        data: false
      };

      plumbing.fulfill(data, id);

      defer.promise
        .then(function(data) { expect(data).toBe(false) })
        .catch(function(msg) { expect(msg).toBeNull() });

      rootScope.$apply();
    });

    it('should inject promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.future": "NG1234",
          state: "pending"
        }
      };
      var data = {
        state: "resolved",
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
            "net.liftmodules.ng.Angular.future": "NG1234",
            state: "pending"
          }
        }
      };
      var data = {
        state: "resolved",
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
        {"net.liftmodules.ng.Angular.future": "NG1234", state: "pending"},
        {"net.liftmodules.ng.Angular.future": "NG1235", state: "pending"}
      ];
      var data1 = {
        state: "resolved",
        data: "from future"
      };
      var data2 = {
        state: "resolved",
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
          {"net.liftmodules.ng.Angular.future": "NG1234", state: "pending"},
          {"net.liftmodules.ng.Angular.future": "NG1236", state: "pending"}
        ]
      };
      var data1 = {
        state: "resolved",
        data: "whatever"
      };
      var data2 = {
        state: "resolved",
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
          "net.liftmodules.ng.Angular.future": "won't matter",
          state: "resolved",
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
          "net.liftmodules.ng.Angular.future": "blah",
          state: "rejected",
          data: "shit, y'all!"
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
          "net.liftmodules.ng.Angular.future": "whateve",
          state: "resolved"
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