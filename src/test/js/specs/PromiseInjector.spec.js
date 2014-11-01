describe('lift-ng', function(){

  describe('Promise Injector', function(){
    var inj;
    var plumbing;

    beforeEach(module('lift-ng'));

    beforeEach(inject(['promiseInjector', 'plumbing', function(injector, p){
      inj = injector;
      plumbing = p;
    }]));

    it('should inject promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.futureId": "NG1234"
        }
      };

      inj.inject(model);

//      expect(Object.keys(plumbing.callbacks).length).toBe(1);
    })
  })
})
;