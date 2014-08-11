describe('lift-ng', function(){

  describe('json diff', function(){
    it('should produce empty add/sub for the same object', function() {
      var val1 = {str: "a string"};
      var val2 = {str: "a string"};
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

  });
});