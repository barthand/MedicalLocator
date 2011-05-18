#!/bin/bash
#
# This script splits the input file into 1MB chunks with decimal suffixes starting with 0 (*.0, *.1, *.2..)
# and returns the chunks count.
#
# Author: barthand <barthand@gmail.com>
#

if [ -z $1 ] || [ ! -e $1 ]; then
	echo "Please provide proper input file."
	exit 1
fi

input_file=$1

# Use the 'split' command to actually split the input file.
split -a1 -b1048576 -d $input_file $input_file.

# Report the no of chunks created.
echo "Number of the chunks created: `ls -l | grep "$input_file." | wc -l`"
