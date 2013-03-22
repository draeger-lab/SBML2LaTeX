#!/bin/bash
#
# This program downloads the latest version of the
# Systems Biology Ontology (SBO) into the folder
# ./resources/ relative to the location of this script.
#
# Created at 2007-04-05
# last modified: 2009-02-03
# Author: Andreas Draeger
#
# usage: ./updateSBO.sh

echo "updateSBO.sh Copyright (C) 2009 Andreas Dräger"
echo "This program comes with ABSOLUTELY NO WARRANTY."
echo "This is free software, and you are welcome to redistribute it"
echo "under certain conditions. See GNU GPL for details."

wget http://www.ebi.ac.uk/sbo/exports/Main/SBO_OBO.obo
mv SBO_OBO.obo resources/SBO_OBO.obo
chmod 664 ${0%%/*}/resources/SBO_OBO.obo

exit 0
