# Raaka pohja

import telepot
import time
import pgdb
from datetime import timedelta

# Get config
with open('/opt/secrets/token.txt', 'r') as config:
    content = config.read().replace(" ", "").split(':')
    token = content[content.index('token') + 1]
    username = content[content.index('sqluser') + 1]
    password = content[content.index('sqlpassword') + 1]
    mname = content[content.index('measurename') + 1]
    hostname = content[content.index('hostname') + 1]
    dbase = content[content.index('database') + 1]
    table = content[content.index('table') + 1]


# Set up necessary variables and connect
started = False
stored_id = 'unknown'
highscore = 0
connection = pgdb.connect(host=hostname,
                          user=username,
                          password=password,
                          database=dbase
                          )
cur = connection.cursor


def get_highscore():
    cur.execute("SELECT * FROM %s ORDER \
                BY %s DESC, 'date' LIMIT 1" % (table, mname))
    return cur.fetchone()


def get_user(input_name):
    cur.execute("SELECT * FROM %s WHERE name = '%s' \
                ORDER BY %s DESC LIMIT 1" % (table, input_name, mname))
    return cur.fetchone()


# @param conditional checks whether the found highscore
# is higher than the known one.
def print_hs_data(conditional):
    result = get_highscore()
    if conditional:
        if highscore >= result.amount:
            return ''
    score = result.amount
    time = result.date + timedelta(hours=2)
    text = 'Kovin heiluttelija on %s indeksin arvolla %s.\n \
            Saavutus  tehtiin %s' % (result.name, score, time)
    return text


def handler(msg):
    try:
        text = msg['text']
        chat_id = msg['chat']['id']
        if started:
            if text.lower().find('heiluttelija') + 1:
                send_highscore(msg['message_id'], print_hs_data(False))
        else:
            if text == '/(^^)':
                with open('./stored_id.txt', 'w+') as memory:
                    global started
                    started = True
                    text = memory.read()
                    if text == '':
                        memory.write(str(chat_id))
                        stored_id = chat_id
                    else:
                        stored_id = text
                    bot.sendMessage(stored_id,
                                    'Moro!',
                                    reply_to_message_id=msg['message_id']
                                    )
            elif text == '/Stahp':
                global started
                started = False
                bot.sendMessage(stored_id,
                                "Ookoo.",
                                reply_to_message_id=msg['message_id']
                                )
    except Exception as e:
        pass


def send_highscore(msgid, text):
    if text != '':
        bot.sendMessage(stored_id,
                        text,
                        reply_to_message_id=stored_id
                        )


# Set up the bot
bot = telepot.Bot(token)
bot.message_loop(handler)


while 1:
    time.sleep(10)
    if started:
        send_highscore(stored_id, print_hs_data(True))
