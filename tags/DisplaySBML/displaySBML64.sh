#!/bin/bash
#
# Usage:
# ./displaySBML64.sh inFile.xml [tex|latex|pdf|ps|eps|dvi|jpg|png|gif|html] [options]
#
# Possible command line options (case is ignored):
# -addMissingUnitDeclarations=<true|false>
# -landscape=<true|false>
# -printNameIfAvailable=<true|false>
# -titlePage=<true|false>
# -typeWriter=<true|false>
# -fontSize=<8|9|10|11|12|14|16|17>
# -paperSize=<a[0..9]|b[0..9]|c[0..9]|d[0..9]|letter|legal|executive>
# --help

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

export LD_LIBRARY_PATH="\
${LD_LIBRARY_PATH}:\
${DISPLAYSBMLPATH}/lib/linux64/libSBML/lib:\
${DISPLAYSBMLPATH}/lib/linux64/xerces/lib"

export CLASSPATH="\
${DISPLAYSBMLPATH}/dist/displaySBML.jar:\
${DISPLAYSBMLPATH}/lib/linux64/libSBML/lib/libsbmlj.jar"

MAIN_CLASS=org.sbml.displaysbml.SBMLconverter

#JAVA_LIB_PATH="\
#/usr/java/jdk1.6.0_03/jre/lib/i386/server:\
#/usr/java/jdk1.6.0_03/jre/lib/i386:\
#/usr/java/jdk1.6.0_03/lib/i386:\
#${DISPLAYSBMLPATH}/lib/linux64/libSBML/lib:\
#${DISPLAYSBMLPATH}/lib/linux64/xerces/lib/:\
#/usr/java/jdk1.6.0_03/bin/java/packages/lib/i386:\
#/lib:\
#/usr/lib"

TEXINPUTS=.:/rahome/webservices/DisplaySBML:/usr/share/texmf//
DISPLAYSBMLPATH=/rahome/webservices/DisplaySBML
JAVA_HOME="/usr/java/jdk1.6.0_04"

JAVA_LIB_PATH="\
${JAVA_HOME}/jre/lib/amd64/server:\
${JAVA_HOME}/jre/lib/amd64:\
${JAVA_HOME}/lib/amd64:\
${DISPLAYSBMLPATH}/lib/linux64/libSBML/lib:\
${DISPLAYSBMLPATH}/lib/linux64/xerces/lib/:\
${JAVA_HOME}/bin/java/packages/lib/amd64:\
/lib:\
/usr/lib"



VM_ARGS="-Xms32M -Xmx512M -Djava.library.path=${JAVA_LIB_PATH}"

if [ $# -ge 1 ]; then
  extension=${1:(-4)}
  prefix=${1%%$extension}
  outfile=${1%%$extension}".tex"
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
	 echo  $outfile
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
  echo "usage: ./displaySBML64.sh infile.xml [format] [options]"
  ${JAVA_HOME}/bin/java ${VM_ARGS} -cp ${CLASSPATH} ${MAIN_CLASS} --help
fi

exit 0
