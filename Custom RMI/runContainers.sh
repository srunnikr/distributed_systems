#!/bin/bash

docker build -t server .
docker build -t client .

echo "### Removing any existing containers of name server or client ###"
docker stop server
docker stop client

docker rm server
docker rm client
echo "###"

docker run --name server \
	-itd \
	server \
	java pingpong.PingPongServer 5000

docker run --name client \
	-itd \
	--link server:server_ip \
	client \
	java pingpong.PingPongClient server_ip 5000

# Need to sleep for a few seconds to get the output
sleep 4

# Print the logs 
echo "############ OUTPUT #############"
docker logs client
echo "#################################"

# Stop the client and server containers
echo "### Stopping and cleaning the containers ###"
docker stop client
docker rm client
docker stop server
docker rm server