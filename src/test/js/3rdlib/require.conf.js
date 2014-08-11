require.config({
    baseUrl: EnvJasmine.rootDir,
    paths: {
        mocks:      EnvJasmine.mocksDir,
        specs:      EnvJasmine.specsDir

        // Libraries
        // FIXME: we don't want to depend on jQuery necessarily, but for
        // now the sbt plugin requires it
        // jquery:     EnvJasmine.libDir+'jquery-1.8.1'
    }
});
