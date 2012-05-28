#!/bin/bash
#
# This script grabs the whole medical facilities data from http://www.rejestrzoz.gov.pl in HTML.
#
# Author: barthand <barthand@gmail.com>
#

usage() {
cat << EOF
Usage:
$0 <count> <step> [<offset>]
EOF
}

if [ -z "$1" ] || [ -z "$2" ]; then
	usage
	exit 1
fi

# Read the args.
records_count=$1
step=$2
offset=$3

if [ -z "$offset" ]; then
	offset=0
fi

cookie_file=rzoz.cookie

# Get a jsessionid.
curl -b $cookie_file http://www.rejestrzoz.gov.pl/RZOZ/

# Get first 20 records.
curl -v -c $cookie_file -d "tryb=KS&ZOZSelectFormName=&ZOZSelectKodFName=&ZOZSelectNumerFName=&ZOZSelectNazwaFName=&akcja=&editOrg=true&ZOZSelectTeryt=ZOZSelectTerytAll&rowCount=20&rowOffset=$offset&pokazZawieszone=true&filtrType=0&resetFiltr=true&nazwaPelna=&miejscowosc=&dziedzinaMedyczna=%3C--NULL--%3E&trybLeczenia=0&rodzajZakladu=0&Szukaj=Szukaj" "http://www.rejestrzoz.gov.pl/RZOZ/ksiegiRej/ksiegaRejListInfo.do"

offset=`expr $offset + 20`
end_offset=`expr $offset + $records_count`

# Get the rest.
for i in `seq $offset $step $end_offset`; do
	row_count=$step
	row_offset=$i 
	if [ `expr $row_offset + $step` -gt $end_offset ]; then 
		row_count=`expr $end_off - $row_offset`
	fi
	curl -v -c $cookie_file -d "tryb=KS&ZOZSelectFormName=&ZOZSelectKodFName=&ZOZSelectNumerFName=&ZOZSelectNazwaFName=&akcja=&editOrg=true&ZOZSelectTeryt=ZOZSelectTerytAll&rowCount=20&rowOffset=0&pokazZawieszone=true&filtrType=0&resetFiltr=true&nazwaPelna=&miejscowosc=&dziedzinaMedyczna=%3C--NULL--%3E&trybLeczenia=0&rodzajZakladu=0&Szukaj=Szukaj" -e "http://www.rejestrzoz.gov.pl/RZOZ/ksiegiRej/ksiegaRejListInfo.do" "http://www.rejestrzoz.gov.pl/RZOZ/ksiegiRej/ksiegaRejListInfo.do?rowOffset=$row_offset&rowCount=$row_count"
done

rm $cookie_file
