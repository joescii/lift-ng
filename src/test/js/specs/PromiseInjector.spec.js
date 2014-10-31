describe('lift-ng', function(){

  describe('Promise Injector', function(){
    var inj;

    beforeEach(module('lift-ng'));

    beforeEach(inject(['promiseInjector', function(injector){
      inj = injector;
    }]));

    it('should inject promises in the base level', function() {
      expect(inj).toEqual({})
    })
  })
})
;