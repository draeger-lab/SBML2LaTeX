#!/bin/bash

wget http://www.ebi.ac.uk/sbo/exports/Main/SBO_OBO.obo
mv SBO_OBO.obo resources/SBO_OBO.obo
chmod 664 resources/SBO_OBO.obo

