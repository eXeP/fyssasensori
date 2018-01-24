# Raaka pohja

import telepot
import time
import pgdb
from datetime import timedelta

# Get config
with open('/opt/secrets/liikemittari_config.txt', 'r') as config:
    content = config.read().replace(" ", "").replace("\n","=").split('=')
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
stored_msg = 'ukno'
highscore = 0
connection = pgdb.connect(host=hostname,
                          user=username,
                          password=password,
                          database=dbase
                          )
cur = connection.cursor()


def get_highscore():
    cur.execute("SELECT * FROM %s ORDER \
                BY %s DESC, date LIMIT 1" % (table, mname))
    return cur.fetchone()


def get_user(input_name):
    cur.execute("SELECT * FROM %s WHERE name = '%s' \
                ORDER BY %s DESC LIMIT 1" % (table, input_name, mname))
    return cur.fetchone()


# @param conditional checks whether the found highscore
# is higher than the known one.
def print_hs_data(conditional, t_index):
    global highscore
    result = get_highscore()
    if conditional:
        if highscore >= result.amount:
            return ''
    score = result.amount
    highscore = score
    time = result.date + timedelta(hours=2)
    if t_index == 1:
        text = 'Kovin heiluttelija on %s indeksin arvolla %s.\n \
Saavutus  tehtiin %s.' % (result.name, score, time)
    else:
        text = 'Uskomatonta! %s heilutteli \
arvon %s %s.' % (result.name, score, time)
    return text


def handler(msg):
    global started
    global stored_id
    global stored_msg
    try:
        text = msg['text']
        chat_id = msg['chat']['id']
        if text.lower().find('heilu') + 1:
            send_highscore(chat_id, print_hs_data(False, 1))
        else:
            if text == '/(^^)':
                with open('./stored_id.txt', 'w+') as memory:
                    started = True
                    text = memory.read()    
                    stored_id = chat_id
                    bot.sendMessage(stored_id,
                                    'Moro!',
                                    reply_to_message_id=msg['message_id']
                                    )
            elif text == '/Stahp':
                started = False
                bot.sendMessage(stored_id,
                                "Ookoo.",
                                reply_to_message_id=msg['message_id']
                                )
    except Exception as e:
        pass


def send_highscore(ch_id, text):
    if text != '':
        bot.sendMessage(ch_id,
                        text
                        )


# Set up the bot
bot = telepot.Bot(token)
bot.message_loop(handler)


while 1:
    time.sleep(10)
    if started:
        send_highscore(stored_id, print_hs_data(True, 2))
