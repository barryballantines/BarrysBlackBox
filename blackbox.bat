set THIS_DIR=%~dp0
java -jar %THIS_DIR%target/blackbox-1.6-SNAPSHOT.jar -Djava.util.logging.config.file="%THIS_DIR%logging.properties" > blackbox.out 2> blackbox.err