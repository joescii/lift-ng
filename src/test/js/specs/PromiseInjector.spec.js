describe('lift-ng', function(){

  describe('Promise Injector', function(){
    var rootScope;
    var inj;
    var plumbing;

    beforeEach(module('lift-ng'));

    beforeEach(inject(['$rootScope', 'promiseInjector', 'plumbing', function(r, i, p){
      rootScope = r;
      inj = i;
      plumbing = p;
    }]));

    it('should inject promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.futureId": "NG1234"
        }
      };
      var data = {
        success: true,
        data: "from future"
      };

      inj.inject(model);

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
            "net.liftmodules.ng.Angular.futureId": "NG1234"
          }
        }
      };
      var data = {
        success: true,
        data: "from future"
      };

      inj.inject(model);

      plumbing.fulfill(data, "NG1234");

      model.obj.f.then(function(val){ expect(val).toBe("from future") });
      expect(model.str).toBe("a string");
      expect(model.obj.num).toBe(42);

      rootScope.$apply();
    });

    it('should inject promises in arrays', function() {
      var arr = [
        {"net.liftmodules.ng.Angular.futureId": "NG1234"},
        {"net.liftmodules.ng.Angular.futureId": "NG1235"}
      ];
      var data1 = {
        success: true,
        data: "from future"
      };
      var data2 = {
        success: true,
        data: "other future"
      };

      inj.inject(arr);

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
          { "net.liftmodules.ng.Angular.futureId": "NG1234" },
          { "net.liftmodules.ng.Angular.futureId": "NG1236" }
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

      inj.inject(model);

      plumbing.fulfill(data1, "NG1234");
      plumbing.fulfill(data2, "NG1236");

      model.arr[0].then(function(val){ expect(val).toBe("whatever") });
      model.arr[1].then(function(val){ expect(val).toBe("blah") });
      expect(model.str).toBe("a string");

      rootScope.$apply();
    });

  })
})
;