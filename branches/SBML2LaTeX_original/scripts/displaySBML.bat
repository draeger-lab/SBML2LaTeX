rem @echo off
set CLASSPATH="dist/displaySBML.jar;"
set CLASSPATH=%CLASSPATH%lib/windows/libSBML-3.3.2-xerces/bindings/java/classes/sbmlj.jar;
set CLASSPATH=%CLASSPATH%lib/ant.jar;
set CLASSPATH=%CLASSPATH%lib/jdom.jar;
set CLASSPATH=%CLASSPATH%lib/xalan.jar;
set CLASSPATH=%CLASSPATH%lib/jaxen-core.jar;
set CLASSPATH=%CLASSPATH%lib/xerces.jar;
set CLASSPATH=%CLASSPATH%lib/jaxen-jdom.jar;
set CLASSPATH=%CLASSPATH%lib/saxpath.jar;
set CLASSPATH=%CLASSPATH%lib/xml-apis.jar

set MAIN_CLASS="org.sbml.displaysbml.SBMLconverter"
rem set VM_ARGS="-Xms32M -Xmx512M"
rem set SYS_PROPS="-Djava.library.path=.;lib/windows/libSBML-3.3.2-xerces;lib/windows/libSBML-3.3.2-xerces/bindings/java/classes/sbmlj.jar;lib/windows/libSBML-3.3.2-xerces/"
rem %VM_ARGS%
java %SYS_PROPS% -cp %CLASSPATH% %MAIN_CLASS% --gui
