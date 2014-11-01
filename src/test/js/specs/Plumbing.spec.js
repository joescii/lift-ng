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

  })
})
;