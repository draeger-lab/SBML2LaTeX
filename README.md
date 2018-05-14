# SBML2LaTeX

<img align="right" src="resources/org/sbml/tolatex/gui/img/ICON_LATEX_64.png"/>

**Conversion of SBML files into human-readable reports**

[![License (GPL version 3)](https://img.shields.io/badge/license-GPLv3.0-blue.svg?style=plastic)](http://opensource.org/licenses/GPL-3.0)
[![Stable version](https://img.shields.io/badge/Stable_version-0.9.9-brightgreen.svg?style=plastic)](https://github.com/draeger-lab/SBML2LaTeX/releases/)
[![DOI](http://img.shields.io/badge/DOI-10.1093%20%2F%20bioinformatics%20%2F%20btp170-blue.svg?style=plastic)](http://dx.doi.org/10.1186/s12918-015-0212-9)

*Authors:* [Andreas Dräger](https://github.com/draeger/), Hannes Planatscher, Dieudonné Motsou Wouamba, Adrian Schröder, Michael Hucka, Lukas Endler, Martin Golebiewski, Wolfgang Müller, and Andreas Zell
___________________________________________________________________________________________________________

SBML2LATEX is a tool to convert files in the System Biology Markup Language SBML) format into LATEX files. A convenient online version is available, which allows the user to directly generate a report from SBML in the form of PDF or TeX, which can be further processed to various file types including DVI, PS, EPS, GIF, JPG, or PNG. SBML2LATEX can also be downloaded and used locally in batch mode or interactively with its Graphical User Interface or several command line options. The purpose of SBML2LATEX is to provide a way to read the contents of XML-based SBML files. It is helpful and important for, e.g., error detection, proofreading and model communication.

**Please cite:**
Dräger A, Planatscher H, Wouamba DM, Schröder A, Hucka M, Endler L, Golebiewski M, Müller W, and Zell A: “SBML2LATEX: Conversion of SBML files into human-readable reports”, Bioinformatics 2009. [ [DOI](https://dx.doi.org/10.1093/bioinformatics/btp170) ], [ [PDF](http://bioinformatics.oxfordjournals.org/cgi/reprint/25/11/1455.pdf) ] 

___________________________________________________________________________________________________________
## Time line

* New release: SBML2LATEX version 0.9.9 can now be downloaded at the [download section](https://github.com/draeger-lab/SBML2LaTeX/releases/). This new version has become much simpler, the file is smaller and can directly be launched by double clicking. It is based on the latest release of JSBML (version 0.8-rc1). See the release notes for new features and bug fixes.   -   ([draeger](https://github.com/draeger/) - 2011-12-15 15:48) 
* SBML2LATEX version 0.9.8 is now available on the [download page](https://github.com/draeger-lab/SBML2LaTeX/releases/). This new version supports SBML Level 3 Version 1 and is based on [JSBML](https://github.com/sbmlteam/jsbml/). It provides a new option to write the entire differential equation system instead of links to the rate equations only and is able to directly create PDF files using LATEX and to open the result in the system's default PDF viewer.   -   ([draeger](https://github.com/draeger/) - 2011-03-08 8:47)
* Full support of SBML Level 2 Version 4: SBML2LATEX version 0.9.6 was released that uses libSBML 3.3.1 and therefore supports the new specification of SBML. The new release contains a convenient bash script that automatically downloads and installs libSBML 3.3.1 with Xerces 3.0.0 on your system.   -   ([draeger](https://github.com/draeger/) - 2009-02-04 14:29)
* Web Service available: This web service provides a convenient way of using SBML2LATEX and creates various file formats including PDF, PS, DVI and TeX.   -   ([draeger](https://github.com/draeger/) - 2008-12-16 13:15)
* First Release: Launch the [download link](https://github.com/draeger-lab/SBML2LaTeX/releases/). Note that [libSBML](http://sbml.org/Software/libSBML/) 3.2 or higher is required for this program.  -  ([draeger](https://github.com/draeger/) - 2008-12-16 09:23)
___________________________________________________________________________________________________________

## Thanks for support

We would like to thank the SBML team (see http://sbml.org), and the LATEX3 team, especially the developer of the breqn package that introduces automatic line breaks into equations, Morten Høgholm.
We are grateful to the developers of HTML2LATEX because for the conversion of XHTML elements within the SBML notes element, SBML2LATEX includes a modified version of this program (see http://htmltolatex.sourceforge.net).
We would also like to thank the JSBML team for developing this new project.
Older versions of SBML2LATEX include software developed by the JDOM Project (http://www.jdom.org).

## Funding

This project was funded by the Federal Ministry of Education and Research (Germany, Bundesministerium für Bildung und Forschung, BMBF) in the projects National Genome Research Network (Nationales Genomforschungsnetz, NGFN-Plus), HepatoSys “Kompetenznetzwerk Systembiologie des Hepatozyten”, and the Virtual Liver Network and supported by the Center for Bioinformatics Tuebingen (Zentrum für Bioinformatik Tübingen, ZBIT). The German federal state Baden-Württenberg funded the Tübinger Bioinformatik-Grid project that enabled us to provide the convenient SBML2LATEX web service.
