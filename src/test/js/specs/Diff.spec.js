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


  });
});