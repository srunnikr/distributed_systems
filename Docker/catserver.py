#!/usr/bin/python

import socket
import sys
import os

FILEPATH = sys.argv[1]
SERVER_PORT = int(sys.argv[2])

def process_request(s):
	if not os.path.isfile(FILEPATH):
		send_not_exist(s)
	elif os.path.getsize(FILEPATH) == 0:
		send_empty(s)
	else:
		send_lines(s)

def send_not_exist(s):
	send_line(s, '{"err": "FILE ON SERVER DOES NOT EXIST"}')

def send_empty(s):
	send_line(s, '{"err": "FILE ON SERVER IS EMPTY"}')

def send_lines(s):
	with open(FILEPATH) as fp:
		for line in fp:
			send_line(s, '{"line": "'+line.strip().upper()+'"}')

def send_line(s, line):
	c, addr = s.accept()     
	request = c.recv(16000).decode()
	if request == 'LINE':
		c.sendall(line.encode())
	c.close()


s = socket.socket()         
host = socket.gethostname()
s.bind((host, SERVER_PORT))

s.listen(5)                 
while True:
	process_request(s)
	