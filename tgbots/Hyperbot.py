# Raaka pohja

import telepot
import time

import pgdb
from datetime import timedelta

# Texts contain unicode letters not supported by Python2.

# Needed for python2 implementation
# import unidecode
# def strip_acc(t):
# return unidecode.unidecode(t)


# Get config
with open('/opt/secrets/liikemittari_config.txt', 'r') as config:
    content = config.read().replace(" ", "").replace("\n", "=").split('=')
    token = content[content.index('token') + 1]
    username = content[content.index('sqluser') + 1]
    password = content[content.index('sqlpassword') + 1]
    mname = content[content.index('measurename') + 1]
    hostname = content[content.index('hostname') + 1]
    dbase = content[content.index('database') + 1]
    table = content[content.index('table') + 1]


# Set up necessary variables and connect
stored_id = []
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
    result = cur.fetchone
    if result is None:
        return -1
    else:
        return result


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
    global stored_id
    try:
        # For python 2 without accents:
        # text = strip_acc(msg['text'])
        # For python 3:
        text = msg['text']
        chat_id = msg['chat']['id']
        if text.lower().find('käsien heiluttelija') + 1:
            send_highscore(chat_id, print_hs_data(False, 1))
        elif text == '/(^^)':
            print('Initialising into ' + str(chat_id))
            with open('./stored_id.txt', 'w+') as memory:
                text = memory.read()
                if chat_id not in stored_id:
                    stored_id.append(chat_id)
                bot.sendMessage(chat_id,
                                'Moro!',
                                reply_to_message_id=msg['message_id']
                                )
        elif text == '/Stahp':
            stored_id.remove(chat_id)
            bot.sendMessage(chat_id,
                            "Ookoo.",
                            reply_to_message_id=msg['message_id']
                            )
        elif text.lower().find('kuinka paljon') + 1 and \
                text.lower().find('heiluttelee') + 1:
            data = text.split("'")
            if len(data) != 3:
                bot.sendMessage(chat_id,
                                "En tajua.",
                                reply_to_message_id=msg['message_id']
                                )
            else:
                value = get_user(data[1])
                if value == -1:
                    bot.sendMessage(chat_id,
                                    'En ole havainnut tämän jäbän heilutelleen \
                                    käsiään. Kummallista.',
                                    reply_to_message_id=msg['message_id']
                                    )
                else:
                    message = data[1] + ' on heilutellut käsiänsä ' + \
                        str(value) + ' yksikköä.'
                    bot.sendMessage(chat_id,
                                    message,
                                    reply_to_message_id=msg['message_id']
                                    )
        elif text.lower() == '\liikemittari_help':
            bot.sendMessage(chat_id,
                            "Aloitan ennätysspämmin komennolla /(^^), ja \
lopetan sen /Stahp komennolla. Kuuntelen höpinöitänne muutenkin! Ennätyksen \
kuulette mainitsemalla käsien heiluttelijan, ja nimellä voitte hakea \
palttiarallaa näin: Kuinka paljon 'K Kekkonen' heiluttelee käsiä?",
                            reply_to_message_id=msg['message_id']
                            )
    except Exception as e:
        print(e)


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
    for i in stored_id:
        send_highscore(i, print_hs_data(True, 2))
