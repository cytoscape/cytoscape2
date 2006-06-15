#!/usr/bin/perl
#
# This script is used to generate javahelp from a docbook xml document
# generated from a MoinMoin 1.53 wiki page.  The script is needed because
# the xml as generated by MoinMoin is incorrectly formatted in a few cases,
# but also to fetch the images referenced in the document so that they
# can be used locally rather than through a URL.
#


use File::Copy;

die "USAGE: $0 <docbook file> <output dir>\n" if ( $#ARGV != 1 );

$xml = $ARGV[0];
$dir = $ARGV[1];
$xml_form = "$xml.form";
$xml_good = "$xml.good";

copy $xml, $xml_form;

#
# Format the xml so that (roughly) one element appears per line.
#
system "xmlformat -i $xml_form";

mkdir "$dir";
mkdir "$dir/images";

$prevWasSection = 0;

open OUT, ">$xml_good";
open FILE, "$xml_form" or die;
while(<FILE>) {
	chomp;

	#
	# This block is used to fetch the image data. It fetches
	# the image files referenced in imagedata elements and
	# copies them to a local "images" directory.
	#
	if ( $_ =~ /\<imagedata\s+fileref=\"(.+\=(.+))\"\s*\/\>/ ) {
		$url = $1;
		$file = $2;
		$url =~ s/\&amp\;/\\\&/g;

		if ( $url !~ /http.+/ ) {
			$url = "http://cytoscape.org" . $url;
		}

		$com =  "curl -G $url > $dir/images/$file";

		system $com;

		print OUT "<imagedata fileref=\"images/$file\"/>\n";

	#
	# These two blocks give section elements an id parameter.  
	# The id is generated by removing whitespace from the subsequent
	# title tag.  This is useful because it allows us to generate the
	# javahelp using the ids, which provides human readable names, as
	# opposed to marginally sensible generated strings.
	#
	} elsif ( $_ =~ /\<section\>/ ) {
		$prevWasSection = 1;
	} elsif ( $_ =~ /\<title\>(.+)\<\/title\>/ ) {
		if ( $prevWasSection == 1 ) {
			$title = $1;
			$title =~ s/\W+//g;
			print OUT "<section id=\"$title\">\n$_\n";
			$prevWasSection = 0;
		} else {
			print OUT "$_\n";
		}

	#
	# Like the previous blocks, this block provides the entire article
	# an id. This is how we reference the beginning of the helpset in
	# the java code.
	#
	} elsif ( $_ =~ /\<article\>/ ) {
		print OUT "<article id=\"index\">\n";
	
	#
	# These blocks fix a problem with the xml format.  They add a
	# cols parameter to the tgroup element that is otherwise missing.
	# Without this parameter, transformation of the document will fail.
	# We get the number of columns from the previous table element.
	#
	} elsif ( $_ =~ /\<table.*(cols\=\"\d+\").*\>/ ) {
		$cols = $1; 
		print OUT "$_\n"; 
	} elsif ( $_ =~ /\<tgroup\>/ ) {
		if ( $cols ne "" ) { print OUT "<tgroup $cols>\n"; }
		else { print OUT "$_\n"; }
	} elsif ( $_ =~ /\<\/table\>/ ) {
		$cols = "";
		print OUT "$_\n";
	
	#
	# Just prints the unmodified line.
	#
	} else {
		print OUT "$_\n";
	}
}
close FILE;
close OUT;

#
# Transform the corrected (good) xml document into javahelp.
#
system "xsltproc --stringparam use.id.as.filename 1 --stringparam base.dir $dir/ /usr/share/sgml/docbook/xsl-stylesheets/javahelp/javahelp.xsl $xml_good"; 


# This doesn't contain anything that isn't in the good file.
unlink $xml_form;
