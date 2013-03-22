#!/bin/bash
#
# Created at 2007-04-05
# Author: Andreas Draeger
#
# usage: ./install.sh [bit of the processor]
#
# if no bit number is given 32 bit is assumed.

if [ "$1" == "" ]; then
  bit="32"
else
  bit=$1
fi

cd ..
installdir=$(pwd)

if test -e linux${1}; then
  echo "Directory linux"${1}" is already existing."
else
  mkdir linux${1}
fi
if test -e tmp; then
  echo "Directory tmp is already existing."
else
  mkdir tmp
fi
cd tmp

#########################################
# X E R C E S - I N S T A L L A T I O N #
#########################################
#
echo "Installing XERXES on your system"
#
#wget http://www.apache.org/dist/xml/xerces-c/xerces-c-current.tar.gz
cp ../install/xerces-c-current.tar.gz .
mkdir ../linux${1}/xerces
gunzip xerces-c-current.tar.gz
tar -xvf xerces-c-current.tar
rm xerces-c-current.tar
cd xerces-c-src_2_7_0
export XERCESCROOT=$(pwd)
cd $XERCESCROOT/src/xercesc
autoconf
./runConfigure -p 'linux' -c gcc -x g++ -d no debug -m 'inmem' -n 'socket' -t 'native' -r 'pthread' -b $bit -P $installdir/linux${1}/xerces
gmake
gmake install
gmake clean
cd ../../../
# now we are in the tmp directory.

###########################################
# S W I G - I N S T A L L A T I O N       #
###########################################
#
#wget http://ovh.dl.sourceforge.net/sourceforge/swig/swig-1.3.31.tar.gz
#cp ../install/swig-1.3.31.tar.gz .
#gunzip swig-1.3.31.tar.gz
#tar -xvf swig-1.3.31.tar
#rm swig-1.3.31.tar
#cd swig-1.3.31
#mkdir $installdir/linux${1}/swig
#./configure --prefix="${installdir}/linux${1}/swig" --exec-prefix="${installdir}/linux${1}/swig"
#make
#make install
#make clean
#cd ..
#rm -rf swig-1.3.31

###########################################
# L I B S B M L - I N S T A L L A T I O N #
###########################################


echo "Installing libSBML on your system"
#wget http://switch.dl.sourceforge.net/sourceforge/sbml/libsbml-3.1.1-src.zip
cp ../install/libsbml-3.1.1-src.zip .
mkdir $installdir/linux${1}/libSBML
unzip libsbml-3.1.1-src.zip
#echo $(pwd)
cd libsbml-3.1.1
chmod +x configure
./configure --with-java --with-xerces="${installdir}/linux${1}/xerces" --prefix="${installdir}/linux${1}/libSBML" --exec-prefix="${installdir}/linux${1}/libSBML"
#--with-swig="${installdir}/linux${1}/swig" #--with-perl --with-lisp --with-python --with-matlab
make
make install
/sbin/ldconfig
if [ "$LD_LIBRARY_PATH" == "" ]; then
  export LD_LIBRARY_PATH=$installdir/linux${1}/libSBML
else
  export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$installdir/linux${1}/libSBML
fi
#make docs         # optional
#make install-docs # optional
cd ..
# now we are in tmp.
rm *.zip


##########################################################
#
# #
# ########################################################
# # S U N D I A L S  S u i t e - I N S T A L L A T I O N #
# ########################################################
# #
# #wget http://www.llnl.gov/casc/sundials/download/code/sundials-2.3.0.tar.gz
# cp install/sundials-2.3.0.tar.gz tmp/
# mkdir $installdir/linux${1}/sundials
# cd tmp
# gunzip sundials-2.3.0.tar.gz
# tar -xvf sundials-2.3.0.tar
# rm sundials-2.3.0.tar
# cd sundials-2.3.0
# ./configure --prefix=$installdir/linux${1}/sundials --exec-prefix=$installdir/linux${1}/sundials
# make
# make install
# make clean
# make distclean
# cd ../..
# rm -rf tmp/*
# #wget http://www.llnl.gov/CASC/sundials/download/code/cvode-2.5.0.tar.gz
# cp cvode-2.5.0.tar.gz tmp
# cd tmp
# gunzip cvode-2.5.0.tar.gz
# tar -xvf cvode-2.5.0.tar
# rm cvode-2.5.0.tar
# cd cvode-2.5.0
# ./configure --prefix=$installdir/linux${1}/sundials --exec-prefix=$installdir/linux${1}/sundials
# make
# make install
# make clean
# cd ../..
# rm -rf tmp/*
# #
# #######################################################
# # G R A P H  V I Z  -  I N S T A L L A T I O N        #
# #######################################################
# #
# #yum install graphviz
# #
# #######################################################
# # G R A C E  -  I N S T A L L A T I O N               #
# #######################################################
# #
# #
# #######################################################
# # S B M L O D E S O L V E R - I N S T A L L A T I O N #
# #######################################################
# #
# #wget http://switch.dl.sourceforge.net/sourceforge/sbmlsolver/SBML_odeSolver-1.6.0.tar.gz
# #
# # Libraries have been installed in:
# #    /usr/local/lib
# #
# # If you ever happen to want to link against installed libraries
# # in a given directory, LIBDIR, you must either use libtool, and
# # specify the full pathname of the library, or use the `-LLIBDIR'
# # flag during linking and do at least one of the following:
# #    - add LIBDIR to the `LD_LIBRARY_PATH' environment variable
# #      during execution
# #    - add LIBDIR to the `LD_RUN_PATH' environment variable
# #      during linking
# #    - use the `-Wl,--rpath -Wl,LIBDIR' linker flag
# #    - have your system administrator add LIBDIR to `/etc/ld.so.conf'
# #
# #
# cp install/SBML_odeSolver-1.6.0.tar.gz tmp/
# cd tmp
# gunzip SBML_odeSolver-1.6.0.tar.gz
# tar -xvf SBML_odeSolver-1.6.0.tar
# rm SBML_odeSolver-1.6.0.tar
# cd SBML_odeSolver-1.6.0
# #export LD_LIBRARY_PATH=$LD_LIBRARY_PATH":/usr/local/lib:/usr/local/include"
# #export LD_RUN_PATH=$LD_RUN_PATH":/usr/local/lib:/usr/local/include"
# mkdir $installdir/linux${1}/SBML_odeSolver
# ./configure --with-sundials=$installdir/linux${1}/sundials --prefix=$installdir/linux${1}/SBML_odeSolver --exec-prefix=$installdir/linux${1}/SBML_odeSolver
# make # /opt/sundials-2.3.0
#
# cd ../..


#######################################################
# C L E A R  W O R K S P A C E                        #
#######################################################
cd $installdir/install
rm -rf ../tmp
rm -rf $installdir/linux${1}/libSBML/include
#rm -rf $installdir/linux${1}/swig/share
rm -rf $installdir/linux${1}/xerces/include
