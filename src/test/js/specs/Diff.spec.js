describe('lift-ng', function(){

  describe('json diff', function(){
    it('should produce empty add/sub for the same string', function() {
      var val1 = {str: "a string"};
      var val2 = {str: "a string"};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce empty add/sub for the same object', function() {
      var val1 = {str: {a:1, b:2}};
      var val2 = {str: {a:1, b:2}};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce add for adding a string field', function() {
      var val1 = {str: "a string"};
      var val2 = {};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{str: "a string"}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce add for adding an object', function() {
      var val1 = {obj: {a: "stuff", b: "thangs"}};
      var val2 = {};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{obj: {a: "stuff", b: "thangs"}}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce add for only changes in a sub-object', function() {
      var val1 = {obj: {a: "stuff", b: "thangs"}};
      var val2 = {obj: {a: "stuff"}};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{obj: {b: "thangs"}}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce add for array changes', function() {
      var val1 = {obj: ["stuff", "thangs", 42]};
      var val2 = {obj: ["stuff", "thangs"]};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{obj: {2: 42}}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce add for changing types', function() {
      var val1 = {obj: {a: [1,2,3], b: "thangs"}};
      var val2 = {obj: {a: "stuff", b: "thangs"}};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{obj: {a: [1,2,3]}}, sub:{}};

      expect(diff).toEqual(expected);
    });

    it('should produce sub for array changes', function() {
      var val1 = {obj: ["stuff"]};
      var val2 = {obj: ["stuff", "thangs", 42]};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{}, sub:{obj: {1:"", 2:""}}};

      expect(diff).toEqual(expected);
    });

    it('should produce sub for sub object changes', function() {
      var val1 = {obj: {arr: [1,2], o: {a:1, B:3}}};
      var val2 = {obj: {o: {a:1, B:3, c:2, d:{e:"F"}}, arr: [1,2,7,8,9], r:{}}};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{}, sub:{obj: {arr:{2:"", 3:"", 4:""}, o:{c:"", d:""}, r:""}}};

      expect(diff).toEqual(expected);
    });

    it('should produce add and sub for changes', function() {
      var val1 = {obj: {arr: [1,2], o: {a:1, B:3}}, q:"string"};
      var val2 = {obj: {o: {a:1, B:3, c:2, d:{e:"F"}}, arr: [1,2,7,8,9], r:{}}, s:42};
      var diff = net_liftmodules_ng.diff(val1, val2);
      var expected = {add:{q:"string"}, sub:{obj: {arr:{2:"", 3:"", 4:""}, o:{c:"", d:""}, r:""}, s:""}};

      expect(diff).toEqual(expected);
    });

  });
});