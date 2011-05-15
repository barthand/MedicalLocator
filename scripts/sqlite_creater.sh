#!/bin/bash
#
# This script generates the SQLite SQL script from the input data-file created by geoconverter.sh,
# which can be used to generate the Android-compatible SQLite DB to be deployed along with the application.
#
# Author: barthand <barthand@gmail.com>
#

if [ -z $1 ] || [ ! -e $1 ]; then
	echo "Please provide proper input file."
	exit 1
fi

input_file=$1

# First let's create the required DB schema.
cat << EOF
DROP TABLE IF EXISTS android_metadata;
DROP TABLE IF EXISTS facility;

CREATE TABLE android_metadata ("locale" TEXT DEFAULT 'en_US');
INSERT INTO android_metadata VALUES ('en_US');

CREATE TABLE facility (
	_id INTEGER PRIMARY KEY AUTOINCREMENT,
	name TEXT NOT NULL,
	address TEXT,
	phone TEXT,
	email TEXT,
	latitude REAL,
	longitude REAL);
			
EOF

# Now let's get the real data.
awk -F'|' '
{
	if (length($5) == 0) lat="NULL"; else lat=$5;
	if (length($6) == 0) lng="NULL"; else lng=$6;

	print "INSERT INTO facility (name, address, phone, email, latitude, longitude) VALUES (\047"$1"\047, \047"$2"\047, \047"$3"\047, \047"$4"\047, "lat", "lng");"
}' $input_file
