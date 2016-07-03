#!/usr/bin/python

import sys
import socket
from time import sleep
import json
import os

FILEPATH = sys.argv[1]
SERVER_PORT = int(sys.argv[2])
SERVER_ADDR = sys.argv[3]

def check_line(line):
	if os.path.isfile(FILEPATH):
		with open(FILEPATH) as fp:
			for l in fp:
				if l.strip().upper() == line:
					print('OK')
					return
			print('MISSING')
	else:
		print('FILE ON CLIENT DOES NOT EXIST')

def process_response(text):
	message = json.loads(text)
	if 'err' in message:
		print(message['err']) 
	elif 'line' in message:
		check_line(message['line'])

def request_line():
	s = socket.socket()
	s.connect((SERVER_ADDR, SERVER_PORT))
	s.sendall('LINE'.encode())
	text = s.recv(16000).decode()
	process_response(text)
	s.close()

for i in range(10):
	request_line()
	sleep(3)

