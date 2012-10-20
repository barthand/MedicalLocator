#!/usr/bin/perl -w
#
# This PERL script asks The Google Geocoding API for geolocation of particular addresses 
# as taken from the output from the parser.sh script. The result is the same format
# as in the parser.sh script, although two additional columns are added - latitude and longitude.
#
# Usage: <parser.sh_output> | $0
#
# Requires: Perl, with URI::Escape, LWP::Simple, DateTime, JSON modules.
#
# Author: barthand <barthand@gmail.com>
use strict;
use warnings;

use URI::Escape;
use LWP::Simple;
use DateTime;
use JSON;

my $timestamp = DateTime->now;
my $file = "geocoder." . $timestamp->ymd . ".tmp";
while (my $line = <STDIN>) {
	chomp($line);
	my @fields = split(/\|/, $line);
	my $unescaped_address = $fields[1];
	my $address = uri_escape($fields[1]);

	my $url = "http://maps.googleapis.com/maps/api/geocode/json?address=" . $address . "&sensor=false";

	getstore($url, $file);
	
	# Read whole file at once, instead of per line reading.
	local $/;

	open(FILEHANDLE, $file);
	my $json_text   = <FILEHANDLE>;

	my $perl_scalar = decode_json( $json_text );

	my $location = $perl_scalar->{"results"}[0]->{"geometry"}->{"location"};
	my $lat = $location->{"lat"};
	my $lng = $location->{"lng"};

	if (!defined $lat || !defined $lng) {
		warn "Response problem for query [$unescaped_address]\n---------------\n$json_text\n";
	}
	print "$line|$lat|$lng\n";

	close(FILEHANDLE);
}
unlink($file);
