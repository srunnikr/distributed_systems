#!/bin/bash  

# build contaners
docker build -t data -f volume_dockerfile .
docker build -t server -f server_dockerfile .
docker build -t client -f client_dockerfile .

# run containers
docker stop data
docker rm data
docker run --name data data

docker stop server
docker rm server
docker run --name server \
	--volumes-from data \
	-itd \
	server \
	python catserver.py /data/string.txt 5555

docker stop client
docker rm client
docker run --name client \
	--volumes-from data \
	--link server:server1 \
	-itd \
	client \
	python catclient.py /data/string.txt 5555 server1

# output results
echo "=============="
echo "Waiting 30s..."
sleep 33s
docker logs client

echo "=============="
echo "Cleaning..."

# clean
docker stop data
docker rm data
docker stop server
docker rm server
docker stop client
docker rm client
