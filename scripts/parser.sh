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
clean_tmp_file=$1.`date +%Y%m%d`.clean.tmp

# Print welcome message.
echo "I am working at the moment. Go get some coffee.." >&2

# Convert the encoding of input file to the UTF-8.
iconv -f ISO-8859-2 -t UTF-8 $input_file > $utf8_tmp_file

# Split records into separate lines.
grep "tr class=\"ztrnormal\"><td" $utf8_tmp_file | sed 's/<\/tr>/\0\n/g' > $records_tmp_file

# Print the header of the CSV file.
echo "name|address|phone|e-mail"

while read line
do
	# Now extract the data. Basically it works like that (remember that HTML is actually XML-syntaxed):
	# 1. As we have many different HTML tags, just remove them.
	# 2. The remaining part is the actual data.
	echo "$line" | sed 's/<[^>]*./|/g' >> $clean_tmp_file
done < $records_tmp_file

# Clean the created data, extract only the required columns, print to stdout.
cat $clean_tmp_file | tr -s "|" | cut -f3-6 -d "|"

# Remove temporary files.
rm $utf8_tmp_file
rm $records_tmp_file
rm $clean_tmp_file
