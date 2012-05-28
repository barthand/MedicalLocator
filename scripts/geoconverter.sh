#!/bin/bash
#
# This script asks The Google Geocoding API for geolocation of particular addresses 
# as taken from the output from the parser.sh script. The result is the same format
# as in the parser.sh script, although two additional columns are added - latitude and longitude.
#
# Usage: <parser.sh_output> | $0
#
# Requires: Perl, with URI::Escape module.
#
# Author: barthand <barthand@gmail.com>

GEOCODER_API_TEMP="geocoder.`date +%Y%m%d`.tmp"

# NOTICE: We are reading from the stdin.
while read line; do
	# Parse the address from the input line.
	address=`echo "$line" | cut -f2 -d"|"`
	# Make the address URL-escaped using URI::Escape module within perl.
	address="$(perl -MURI::Escape -e 'print uri_escape($ARGV[0]);' "$address")"

	# Send request to the Google Geocoding API with particular address.
	wget -q "http://maps.googleapis.com/maps/api/geocode/json?address=$address&sensor=false" -O $GEOCODER_API_TEMP

	# Parse the returned result from Google.
	geolocation=`cat $GEOCODER_API_TEMP | perl -e '
use JSON;

# from file content
local $/;
my $json_text   = <STDIN>;
my $perl_scalar = decode_json( $json_text );

my $location = $perl_scalar->{"results"}[0]->{"geometry"}->{"location"};
my $lat = $location->{"lat"};
my $lng = $location->{"lng"};

print "$lat|$lng";
	'`

	# Print output line.
	echo "$line|$geolocation"
done < /dev/stdin

rm $GEOCODER_API_TEMP
