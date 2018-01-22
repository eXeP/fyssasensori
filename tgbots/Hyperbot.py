#Raaka pohja

import telepot
import mysql
import time
import getpass
import pgdb

#TODO Connecting to server 

	pass
started = false
stored_id = 'unknown'
def getHighscore():
	pass

def broadcaster

username = input('Enter username for SQL: ')
password = getpass.getpass()
hostname = 'localhost'
db = ''
connection = pgdb.connect(host = hostname, user = _username, password = _password, database = db)

def handler(msg):
    try:
        text = msg['text']
        chat_id = msg['chat']['id']
        if started:
            if text = '/heiluttelu':
                #TODO
        else
            if text = '/(^^)':
            with open('./stored_id.txt, w+) as f:
                started = true
                if stored_id = f.read() == '':
                    f.write(str(chat_id ))
                    stored_id = chat_id
                bot.sendMessage(stored_id,
                                'Jassoo.',
                                reply_to_message_id=msg['message_id']
                                )
    except: 
        pass

#Sets the bot up with a required token.
with open('/opt/secrets/Hypersense_token.txt', 'r') as t:
	bot = telepot.Bot(t)
	bot.message_loop(handler)


	
	
while 1:
	time.sleep(30)
	