# Welcome to the New Cytoscape Build Process

## Introduction
From February 2013, Cytoscape core code will be managed at [GitHub](https://github.com/cytoscape/).
This is a how-to document for building [Cytoscape 2.x core code](https://github.com/cytoscape/cytoscape2) 
from git repository.

### Important Note for App Developers
Cytoscape 2.x series are now in maintenance phase.  As of February 2013, version 2.8.3 is the most 
stable Cytoscape release and is encouraged to use for your scientific projects, but if you are 
planning to implement new features as an App, we recommend to develop it for 
[3.x series](http://www.cytoscape.org/cy3.html).


## I. Clone Cytoscape 2.x Project Repository

If this is the first time reading this, make sure that you have cloned our git repository.  

You will need to have git installed (http://git-scm.com/).

Then, clone 2.x repository:

For read-only access:

	git clone git://github.com/cytoscape/cytoscape2.git

For committers (read & write access):

	git clone git@github.com:cytoscape/cytoscape2.git


### Branches

Cytoscape 2.x project has two branches:

1. **Master Branch** - For releases.  HEAD of this branch is always pointing to the latest release code.

1. **Develop Branch** - For development.  New features/fixes for next release will be maintained in this 
branch.  If you want to access the latest version, you need to checkout this branch.


## II. Directory Structure

Cytoscape now contains the following directory structure:

* cytoscape
    * corelibs - This directoy contains all libraries written by the Cytoscape 
	 project and used in the Cytoscape Application.

    * application - This directoy contains the code for the Cytoscape application.
    * coreplugins - This directoy contains all plugins delivered as part of the Cytoscape Application.
    * distribution - Contains a maven project that assembles the Cytoscape 
		distribution based on the core plugins and the jar file built by application.  
		This directory includes all shell scripts, sample data, and licenses for the distribution.
    * javadoc - This directory contains a pom that creates a Javadoc jar file 
		that only includes javadocs for corelibs and application, i.e. the public Cytoscape API.
    * packaging - This directoy contains a maven pom file that creates Install4j 
		release bundles and then puts the distribution zip file and javadocs 
		in the same directory. Note that this packaging ONLY happens in the deploy phase.
    * webstart - This directoy contains a maven pom file that creates a 
		webstart (JNLP) distribution of Cytoscape. The webstart bundle is not created 
		as part of the normal maven life cycle.  See the README.txt in that directory for 
		instructions on building a webstart distribution.
    * archetypes - Contains maven archetypes to assist in the construction of Cytoscape plugins.
    * legacy - Contains old and currently unused test data and resources.


## III. Build Process

To build cytoscape:

1. Download and install Apache Maven: http://maven.apache.org/
1. To compile everything:

    a. in the top level directory:

		mvn install


Note: this may not work from within Eclipse; run on command line
Note: you may need to bump up your memory allocation:

	export MAVEN_OPTS=-Xmx1024m

  b. skip tests and javadocs for quick-and-dirty builds:

	mvn install -Dmaven.javadoc.skip=true -Dmaven.test.skip=true

 c. take advantage of multiple threads/cores:

	mvn -T 4  (4 threads)
	mvn -T 1C (1 thread per core)

 d. to build for release, be sure to specify a profile that includes packaging, e.g.,

	mvn install -Prelease 

3.  To run cytoscape (assuming you've run mvn install):

     a. you'll find the normal cytoscape distribution directory here:


	cd distribution/target/distribution-${version}-null.dir/distribution-${version}


## IV. Release Process

A release is created using the Maven Release plugin. So, incrementing the 
version numbers, tagging the release, incrementing the version numbers again, 
and then building the tagged version of the release and deploying properly 
versioned artifacts is accomplished as follows:

	mvn release:prepare -Prelease
	mvn release:perform -Prelease

The next step is to copy the downloadable artifacts to the Cytoscape website.
Everything to be copied over can be found in the packaging/target/install4j 
directory.

## V. To Build Installer

To build the installer bundles, cd to the packaging directory and edit
pom.xml to change the **executable** for install4jc to where your install4j
installation is.  Then run:

	mvn install4j:compile

Note: to package with plugins, first copy plugins into:

	packaging/target/cytoscape-#.#.#/plugins/.

Note: to package with additional plugins, first copy plugins into:

	distribution/target/cytoscape-#.#.#-null/cytoscape-#.#.#/plugins/.

The installers will be created in the target/install4j subdirectory

For questions, please send them to our 
[help desk](https://groups.google.com/forum/?fromgroups#!forum/cytoscape-helpdesk).
