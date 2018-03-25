#!/bin/bash

# This script is used to update the Aliyuan's dns explaination.
# Header: IpAddress, Modify Time

function usage() {
  echo -e "usage: $(basename "$0") -u access_key_id -p access_secret -d domain -r recordName"

  exit 1
}

ip_history_file="$HOME/ip-address-history.log"
current_ip_address=""
access_key_id=""
access_secret=""
domain=""
record_name=""

function get_current_ip() {
  echo "Getting ip address..."

  current_ip_address=$(java -jar get-public-ip-address-1.0.0.jar)

  echo "Current ip address is: $current_ip_address"
}

function update_dns_record() {
  if java -jar update-dns-record-1.0.0.jar -n "$access_key_id" -p "$access_secret" -d "$domain" -r "$record_name"; then
    printf "\\n%s, %s" "$current_ip_address" "$(date "+%G-%m-%d %H:%M:%S")" >> "$ip_history_file"
  fi
}

function need_update_record() {
  if [ -f "$ip_history_file" ]; then
    original_ifs=$IFS
    IFS=','

    read -ra history_ip_addresses <<< "$(tail -n 1 "$ip_history_file")"
    local latest_ip_in_history=${history_ip_addresses[0]}

    IFS=$original_ifs

    if [[ "$latest_ip_in_history" == *"$current_ip_address"* ]]; then
      echo "IP address hasn't changed yet."
      return 1;
    else
      return 0;
    fi
  else
    printf "IP Address, Modified Time" >> "$ip_history_file"
    printf "\\n%s, %s" "$current_ip_address" "$(date "+%G-%m-%d %H:%M:%S")" >> "$ip_history_file"
    return 0;
  fi
}

while getopts "n:p:d:r:h" option; do
  case $option in
    n) access_key_id=${OPTARG};;
    p) access_secret=${OPTARG};;
    d) domain=${OPTARG};;
    r) record_name=${OPTARG};;
    h) usage
       ;;
    *) usage
       ;;
  esac
done

shift $((OPTIND-1))

if [[ -z $access_key_id ]]; then
  echo "Please provide access key id."
  exit 1
elif [[ -z $access_secret ]]; then
  echo "Please provide access secret."
  exit 1
elif [[ -z $domain ]]; then
  echo "Please provide valid domain."
  exit 1
elif [[ -z $record_name ]]; then
  echo "Please provide record name like downloads."
  exit 1
else
  get_current_ip

  if need_update_record; then
    update_dns_record
  fi

  echo "Done."
fi
