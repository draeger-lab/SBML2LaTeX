#!/bin/bash
#
# usage:
# ./displaySBML.sh infile [format landscape typewriter fontsize papersize addUnit titlePage]

export LD_LIBRARY_PATH="\
lib/linux/libSBML/lib:\
lib/linux/xerces/lib/"

export CLASSPATH="\
dist/displaySBML.jar:\
lib/linux/libSBML/lib/libsbmlj.jar"

MAIN_CLASS=org.sbml.displaysbml.SBMLconverter

JAVA_LIB_PATH="\
/usr/java/jdk1.6.0_01/jre/lib/i386/server:\
/usr/java/jdk1.6.0_01/jre/lib/i386:\
/usr/java/jdk1.6.0_01/lib/i386:\
lib/linux/libSBML/lib:\
lib/linux/xerces/lib/:\
/usr/java/packages/lib/i386:\
/lib:\
/usr/lib"

VM_ARGS="-Xms32M -Xmx512M -Djava.library.path=${JAVA_LIB_PATH}"

if [ $# -ge 1 ]; then
  outfile=${1%%\.xml}".tex"
  echo "Creating LaTeX file."
  java ${VM_ARGS} -cp ${CLASSPATH} ${MAIN_CLASS} $1 $outfile $3 $4 $5 $6 $7 $8
  if [ $# -ge 2 ]; then
    case $2 in
      [dD][vV][iI])
         echo "Creating DVI file"
         latex $outfile > /dev/null
         latex $outfile > /dev/null
      ;;
      [hH][tT][mM][lL])
         echo "Creating HTML file"
         latex2html $outfile > /dev/null
      ;;
      [pP][dD][fF])
         echo "Creating PDF file"
         pdflatex $outfile /dev/null
         pdflatex $outfile /dev/null
      ;;
      [eE][pP][sS])
         echo "Creating EPS file"
         pdflatex $outfile > /dev/null
         pdflatex $outfile > /dev/null
         pdffile=${outfile%%\.tex}".pdf"
         pdf2ps $pdffile > /dev/null > /dev/null
         ps2eps -f ${pdffile%%pdf}"ps" > /dev/null
      ;;
      [pP][sS])
         echo "Creating PS file"
         pdflatex $outfile > /dev/null
         pdflatex $outfile > /dev/null
         pdffile=${outfile%%\.tex}".pdf"
         pdf2ps $pdffile > /dev/null
      ;;
      [jJ][pP][gG])
         echo "Creating JPG file"
         pdflatex $outfile > /dev/null
         pdflatex $outfile > /dev/null
         pdffile=${outfile%%tex}"pdf"
         pdf2ps  $pdffile > /dev/null
         convert ${pdffile%%pdf}"eps" ${dvifile%%dvi}"jpg" > /dev/null
      ;;
      [pP][nN][gG])
         echo "Creating PNG file"
         latex $outfile > /dev/null
         latex $outfile > /dev/null
         dvifile=${outfile%%\.tex}".dvi"
         dvipng $dvifile > /dev/null
      ;;
      [tT][eE][xX])
         echo -n " ";;
      [lL][aA][tT][eE][xX])
         echo -n " ";;
      *)  echo ${2}" is no supported format.";;
    esac
  fi
else
  echo "usage: ./displaySBML infile [format]"
fi

exit 0
