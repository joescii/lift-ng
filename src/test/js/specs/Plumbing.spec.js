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

    it('should create a defer and an id', function(){
      var pair = plumbing.createDefer();
      var defer = pair[0];
      var id = pair[1];
      var data = {
        success: true,
        data: "string"
      };

      plumbing.fulfill(data, id);

      defer.promise.then(function(data) { expect(data).toBe("string") });

      rootScope.$apply();
    });
  })
})
;