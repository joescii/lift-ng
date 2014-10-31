describe('lift-ng', function(){

  describe('Promise Injector', function(){
    var inj;
    var cbs;

    beforeEach(module('lift-ng'));

    beforeEach(inject(['promiseInjector', 'callbacks', function(injector, callbacks){
      inj = injector;
      cbs = callbacks;
    }]));

    it('should inject promises in the base level', function() {
      var model = {
        str: "a string",
        f: {
          "net.liftmodules.ng.Angular.futureId": "NG1234"
        }
      };

      inj.inject(model);

      expect(Object.keys(cbs.callbacks).length).toBe(1);
    })
  })
})
;