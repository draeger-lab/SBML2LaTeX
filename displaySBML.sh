#!/bin/bash
#
# Usage:
# ./displaySBML.sh inFile.xml [tex|latex|pdf|ps|eps|dvi|jpg|png|gif|html] [options]
# For more information run the program with --help option.

PROCESSOR=""
if [ $(uname -p) == "x86_64" ]; then
  PROCESSOR="64"
fi

export DISPLAYSBMLPATH=${0%/*}
export TEXINPUTS=$(pwd):${DISPLAYSBMLPATH}/latex_packages//:/usr/share/texmf//
export JAVA_HOME="/usr/java/jdk1.6.0_04"

if [ "$PROCESSOR" != "64" ]; then
  JAVA_HOME="/usr/java/jdk1.6.0_01"
fi

options=""
if [ $# -gt 1 ]; then
  i=1
  if [ ${2:0:1} == "-" ]; then
    start=1
  else
    start=2
  fi
  for var in "$@"; do
    if [ $i -gt $start ]; then
      options=$options" "$var
    fi
    let i=i+1
  done
fi
options=${options}" --logo-file=${DISPLAYSBMLPATH}/resources/SBML2LaTeX --sbo-file=${DISPLAYSBMLPATH}/resources/SBO_OBO.obo --miriam-file=${DISPLAYSBMLPATH}/resources/MIRIAM.xml --html-config-file=${DISPLAYSBMLPATH}/resources/config.xml"

export LD_LIBRARY_PATH="\
${LD_LIBRARY_PATH}:\
${DISPLAYSBMLPATH}/lib/linux${PROCESSOR}/libSBML/lib:\
${DISPLAYSBMLPATH}/lib/linux${PROCESSOR}/xerces/lib"

export CLASSPATH="\
${DISPLAYSBMLPATH}/displaySBML.jar:\
${DISPLAYSBMLPATH}/lib/linux${PROCESSOR}/libSBML/lib/libsbmlj.jar:\
${DISPLAYSBMLPATH}/lib/ant.jar:\
${DISPLAYSBMLPATH}/lib/jdom.jar:\
${DISPLAYSBMLPATH}/lib/xalan.jar:\
${DISPLAYSBMLPATH}/lib/jaxen-core.jar:\
${DISPLAYSBMLPATH}/lib/xerces.jar:\
${DISPLAYSBMLPATH}/lib/jaxen-jdom.jar:\
${DISPLAYSBMLPATH}/lib/saxpath.jar:\
${DISPLAYSBMLPATH}/lib/xml-apis.jar"
#${DISPLAYSBMLPATH}/lib/MiriamJavaLib-standalone-20080421.jar"

MAIN_CLASS=org.sbml.displaysbml.SBMLconverter

JAVA_LIB_PATH="\
${DISPLAYSBMLPATH}/lib/linux${PROCESSOR}/libSBML/lib:\
${DISPLAYSBMLPATH}/lib/linux${PROCESSOR}/xerces/lib/:\
/lib:\
/usr/lib"

if [ "$PROCESSOR" == "64" ]; then
JAVA_LIB_PATH=${JAVA_LIB_PATH}":\
${JAVA_HOME}/jre/lib/amd64/server:\
${JAVA_HOME}/jre/lib/amd64:\
${JAVA_HOME}/lib/amd64:\
${JAVA_HOME}/bin/java/packages/lib/amd64"
else
PROCESSOR=$(uname -p)
# i386
JAVA_LIB_PATH=${JAVA_LIB_PATH}":\
${JAVA_HOME}/jre/lib/${PROCESSOR}/server:\
${JAVA_HOME}/jre/lib/${PROCESSOR}:\
${JAVA_HOME}/lib/${PROCESSOR}:\
/usr/java/packages/lib/${PROCESSOR}"
PROCESSOR=""
fi

VM_ARGS="-Xms32M -Xmx512M -Djava.library.path=${JAVA_LIB_PATH}"

if [ $# -ge 1 ]; then
  extension=${1:(-4)}
  prefix=${1%%$extension}
  outfile=${1%%$extension}".tex"
  #outfile=$(pwd)/${outfile#*/}
  echo "Creating LaTeX file."
  ${JAVA_HOME}/bin/java ${VM_ARGS} -cp ${CLASSPATH} ${MAIN_CLASS} $1 $outfile $options 
  if [ $# -ge 2 ]; then
    case $2 in
      [dD][vV][iI])
         echo "Creating DVI file"
         latex -interaction batchmode $outfile #> /dev/null
         latex -interaction batchmode $outfile #> /dev/null
      ;;
      [hH][tT][mM][lL])
         echo "Creating HTML file"
         latex2html $outfile #> /dev/null
      ;;
      [pP][dD][fF])
         echo "Creating PDF file"
         pdflatex -interaction batchmode $outfile #> /dev/null 
         pdflatex -interaction batchmode $outfile #> /dev/null
      ;;
      [eE][pP][sS])
         echo "Creating EPS file"
         pdflatex -interaction batchmode $outfile #> /dev/null
         pdflatex -interaction batchmode $outfile #> /dev/null
         pdf2ps ${prefix}".pdf" #> /dev/null
         ps2eps -f ${prefix}".ps" #> /dev/null
         rm ${prefix}".pdf"
         rm ${prefix}".ps"
      ;;
      [pP][sS])
         echo "Creating PS file"
         pdflatex -interaction batchmode $outfile #> /dev/null
         pdflatex -interaction batchmode $outfile #> /dev/null
         pdf2ps ${prefix}".pdf" #> /dev/null
         rm ${prefix}".pdf"
      ;;
      [jJ][pP][gG])
         echo "Creating JPG file"
         latex -interaction batchmode $outfile #> /dev/null
         latex -interaction batchmode $outfile #> /dev/null
         dvipng ${prefix}".dvi" #> /dev/null
         for f in ${prefix}*.png; do
           convert $f ${f%%png}"jpg" #> /dev/null
         done
         rm ${prefix}*.png
         zip $prefix ${prefix}*.jpg
         rm ${prefix}*.jpg
         rm ${prefix}".dvi"
      ;;
      [pP][nN][gG])
         echo "Creating PNG file"
         latex -interaction batchmode $outfile #> /dev/null
         latex -interaction batchmode $outfile #> /dev/null
         dvipng ${prefix}".dvi" #> /dev/null
         zip $prefix ${prefix}*.png
         rm ${prefix}*.png
         rm ${prefix}".dvi"
      ;;
      [gG][iI][fF])
         echo "Creating GIF file"
         latex -interaction batchmode $outfile #> /dev/null
         latex -interaction batchmode $outfile #> /dev/null
         dvigif ${prefix}".dvi" #> /dev/null
         zip $prefix ${prefix}*.gif
         rm ${prefix}*.gif
         rm ${prefix}".dvi"
      ;;
      [tT][eE][xX])
         echo -n " ";;
      [lL][aA][tT][eE][xX])
         echo -n " ";;
      *)  echo ${2}" is no supported format.";;
    esac
  fi
else
  echo "usage: ./displaySBML.sh infile.xml [format] [options]"
  ${JAVA_HOME}/bin/java ${VM_ARGS} -cp ${CLASSPATH} ${MAIN_CLASS} --help
fi

exit 0

