#!/bin/bash
#
# This script converts the HTML input with medical facilities into the 'human-readable' form (csv file).
#
# Author: barthand <barthand@gmail.com>
#

if [ -z $1 ]; then
	echo "Usage: $0 <input_file>"
	exit 1
fi

input_file=$1
utf8_tmp_file=$1.`date +%Y%m%d`.utf8.tmp
records_tmp_file=$1.`date +%Y%m%d`.records.tmp

# Convert the encoding of input file to the UTF-8.
iconv -f ISO-8859-2 -t UTF-8 $input_file > $utf8_tmp_file

# Split records into separate lines.
grep "tr class=\"ztrnormal\"><td" $utf8_tmp_file | sed 's/<\/tr>/\0\n/g' > $records_tmp_file

cat $records_tmp_file | perl -w -e '
use strict;

# Print the header of the CSV file.
print "name|address|phone|e-mail|www|type\n";

my %type_patterns = (
	CENTRE => [ "centrum", "przychodnia", "poradnia", "ośrodek", "zakład" ],
	DENTIST => [ "stomatolog", "dentyst" ],
	EYE_DOCTOR => [ "okulist", "oczu" ],
	GYNECOLOGIST => [ "ginekolog" ],
	DOCTOR => [ "lekarz", "rodzinny" ],
	HOSPITAL => [ "szpital" ],
	AMBULATORY => [ "laboratorium", "ambulatorium" ],
);

my @type_preferences = (
	"DENTIST", "EYE_DOCTOR", "GYNECOLOGIST", "DOCTOR", "AMBULATORY", "HOSPITAL", "CENTRE"
);

my %type_to_id = (
	CENTRE => 0,
	DENTIST => 1,
	EYE_DOCTOR => 2,
	GYNECOLOGIST => 3,
	DOCTOR => 4,
	HOSPITAL => 5,
	AMBULATORY => 6,
	OTHER => 100,
);

my %elcount, my $changes = 0;

while (my $line = <STDIN>) {
	# Clean the dataset
	$line =~ s/<[^>]*./|/g;
	$line =~ s/\|{2,}/|/g;
	$line =~ s/\n//g;
	$line =~ s/&#034;/"/g;
	$line =~ s/\s{2,}/ /g;

	my @fields = split(/\|/, $line);

	# Omit non-required fields
	@fields = @fields[2..$#fields];
 
	my ($name, $address, $phone, $email, $www) = @fields[0..4];
	my $type;

	# Skip empty records
	if (!$name || length($name) == 0) {
		next;
	}

	# Stats purposes
	if (!$elcount{$#fields}) {
		$elcount{$#fields} = 1;
	} else {
		$elcount{$#fields} = ++$elcount{$#fields};
	}

	# Check for type of the facility
	foreach my $key (@type_preferences) {
		my $value = $type_patterns{$key};
		foreach my $pattern (@{$value}) {
			if ($name =~ m/($pattern)/i) {
				$type = $key;
				last;
			}
		}

		if ($type) { last }
	}

	if (!$type) {
		$type = "OTHER";
	}


	if ($email && length($email) > 0 && !($email =~ m/@/)) {
		if ($www && length($www) > 0) {
			my $tmp = $email;
			$email = $www;
			$www = $tmp;
			$changes++;
		}
	}

	# Check for non-initialized fields.
	if (!$email) {
		$email = "";
	}

	if (!$www) {
		$www = "";
	}

	# Print the result
	print "$name|$address|$phone|$email|$www|$type_to_id{$type}\n";
}

warn "Type mapping statistics\n-------------------\n";

my %inverted = reverse %type_to_id;
foreach my $key (keys %elcount) {
	warn "$inverted{$key}\[$key\] => $elcount{$key}\n";
}
warn "\nemail<->www replacements: $changes\n"

'

# Remove temporary files.
rm $utf8_tmp_file
rm $records_tmp_file
