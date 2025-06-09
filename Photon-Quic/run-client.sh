#!/bin/bash
if [ "$#" -ne 2 ]; then
  echo "Usage: ./run-client.sh <host> <port>"
  exit 1
fi

echo "Connecting to PhotonStream Server at $1:$2 ..."
java -cp target/Photon-Quic-1.0-SNAPSHOT-shaded.jar client.PhotonStreamClient "$1" "$2"
