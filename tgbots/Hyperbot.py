import telepot
import time
from time import gmtime, strftime, mktime
import datetime
import pytz

import math

import random
import psycopg2
from psycopg2 import sql
from datetime import timedelta
import configparser

# Texts contain unicode letters not supported by Python2.

# Needed for python2 implementation
# import unidecode
# def strip_acc(t):
# return unidecode.unidecode(t)


# ------ HANDWAVE STUFF -----

# Get config
cp = configparser.ConfigParser()
cp.read('.liikemittari_config')
token = cp['BOT']['token']
username = cp['HANDWAVE']['PGSQL_USER']
password =cp['HANDWAVE']['PGSQL_PASSWORD']
mname = cp['HANDWAVE']['MEASURE_COLUMN']
hostname = cp['HANDWAVE']['PGSQL_HOST']
dbase = cp['HANDWAVE']['PGSQL_DATABASE']
table =cp['HANDWAVE']['PGSQL_TABLE']

# Set up necessary variables and connect
stored_id = []
highscore = 0
connection = psycopg2.connect('dbname={} user={} host={} password={}'.format(\
        dbase, username, hostname, password) )
cur = connection.cursor()


def get_highscore():
    cur.execute("SELECT * FROM {} ORDER \
                BY {} DESC, date LIMIT 1".format(table, mname))
    return cur.fetchone()


def get_user(input_name):
    cur.execute("SELECT * FROM {} WHERE name = '{}' \
                ORDER BY {} DESC LIMIT 1".format(table, '{' +input_name +'}', mname))
    result = cur.fetchone()
    if result is None:
        return -1
    else:
        return result

def get_all():
    cur.execute("SELECT * FROM {}".format(table))
    result = cur.fetchall()
    if len(result) == 0:
        return []
    else:
        return result

texts = ['Huh huh mitä käsienheiluttelua!\nTää jäbä (%s) heilutti arvon %s (%s)',
        'Uskomatonta! %s heilutteli arvon %s %s.',
        'Tää on jo tuta-tason pöhinää! %s heilutti %s %s']


# @param conditional checks whether the found highscore
# is higher than the known one.
dbEmpty = False
def print_hs_data(conditional, t_index):
    global dbEmpty
    global highscore
    result = get_highscore()
    if result is not None:
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
            text = texts[random.randint(0, 2)] % (result.name, score, time)
        return text
    else:
        if not dbEmpty:
            dbEmpty = True
            return 'Tietokanta on tyhjä.'
        else:
            return ''

def respondHandwave(msg):
    global stored_id
    global highscore
    text = msg['text']
    chat_id = msg['chat']['id']
    if text.lower().find('käsien heiluttelija') + 1:
        send_msg(chat_id, print_hs_data(False, 1))
        return True
    elif text.lower().find('kuinka paljon') + 1 and \
            text.lower().find('heiluttelee') + 1:
        data = text.split("'")
        if len(data) != 3:
            bot.sendMessage(chat_id,
                            "En tajua.",
                            reply_to_message_id=msg['message_id']
                            )
        else:
            value = get_user(str(data[1]))
            if value == -1:
                bot.sendMessage(chat_id,
                                'En ole havainnut tämän jäbän heilutelleen \
                                käsiään. Kummallista.',
                                reply_to_message_id=msg['message_id']
                                )
            else:
                message = value.name + ' on parhaimmillaan heilutellut \
                        käsiänsä ' + str(value.amount) + ' yksikköä.'
                bot.sendMessage(chat_id,
                                message,
                                reply_to_message_id=msg['message_id']
                                )
        return True
    elif text == '/liikemittari_lista':
        wavers = get_all()
        if wavers is None or len(wavers) == 0:
            bot.sendMessage(chat_id, "En  löytäny yhtään mitään servulta.",
                    reply_to_message_id=msg['message_id'])
        else:
            res_text = "Tässä heiluttelijat:\n"
            for t in wavers:
                res_text += str(t.name) + ": " + str(t.amount) + "\n"
            bot.sendMessage(chat_id, res_text, reply_to_message_id=msg['message_id'])
        return True
    elif text == '/liikemittari_heilutus_help':
        bot.sendMessage(chat_id,
                        "Käsienheiluttelun ennätyksen kuulette mainitsemalla käsien "
                        "heiluttelijan, ja nimellä voitte hakea "
                        "palttiarallaa näin: Kuinka paljon 'K Kekkonen' "
                        "heiluttelee käsiä?",
                            reply_to_message_id=msg['message_id']
                            )
        return True
    else:
        return False


# ------ PARTY STUFF -----


pgsql_conn_parties = psycopg2.connect('dbname={} user={} host={} password={}'.format(\
        cp['PARTIES']['PGSQL_DATABASE'], cp['PARTIES']['PGSQL_USER'],
        cp['PARTIES']['PGSQL_HOST'], cp['PARTIES']['PGSQL_PASSWORD']))
cursor_parties = pgsql_conn_parties.cursor()

class Party:
    def __init__(se, place, longitude, latitude, population, score, timestamp, description):
        se.place = place
        se.longitude = float(longitude)
        se.latitude = float(latitude)
        se.population = int(population)
        se.score = int(score)
        se.startedAt = timestamp
        se.latestTime = timestamp
        se.description = str(description)
        assert(timestamp.tzinfo is not None and timestamp.tzinfo.utcoffset(timestamp) is not None)

    def distanceInM(se, another):
        lat1 = float(se.latitude)
        long1 = float(se.longitude)
        lat2 = float(another.latitude)
        long2 = float(another.longitude)
        R = 6371.0 * 1000
        dlat = (lat2-lat1)*math.pi/180
        dlon = (lat2-lat1)*math.pi/180
        a = math.pow(math.sin(dlat/2), 2) +\
            math.pow(math.sin(dlon/2), 2) * math.cos(lat1*math.pi/180)*math.cos(lat2*math.pi/180)
        c = math.atan2(math.sqrt(a), math.sqrt(1-a)) * 2
        return c*R

    def timeInBetween(se, another):
        dif = another.latestTime - se.latestTime
        return dif.total_seconds()

    def isSame(se, another):
        dist = se.distanceInM(another)
        timeD = se.timeInBetween(another)/60

        max_t = int(cp['PARTIES']['TIME_SEPARATOR'])
        max_dist  = int(cp['PARTIES']['DISTANCE_SEPARATOR'])
        return (dist < max_dist and timeD < max_t)

    def merge(se, another):
        if se.isSame(another):
            se.place = another.place
            se.population = another.population
            se.score = max(another.score, se.score)
            se.latitude = another.latitude
            se.longitude = another.longitude
            se.latestTime = another.latestTime
            if another.description is not None and len(another.description) > 0 and another.description != 'None':
                se.description =  another.description
            return True
        else:
            return False

    def serialize(se):
        return {
                'place': se.place,
                'population': se.population,
                'score': se.score,
                'timeStarted': se.startedAt,
                'lastSeen': se.latestTime,
                'length': (se.latestTime-se.startedAt).total_seconds(),
                'description': se.description,
                }

parties = []

def addParty(party):
    global parties
    for p in parties:
        if p.merge(party):
            return False
    parties.append(party)
    return True

def updateParties():
    for p in parties:
        if p.latestTime < datetime.datetime.now()\
                .replace(tzinfo=psycopg2.tz.FixedOffsetTimezone(offset=180))\
                - datetime.timedelta(hours = 3):
            parties.remove(p)
    query = 'SELECT * FROM '  + cp['PARTIES']['PGSQL_TABLE'] + ' WHERE lastseen >= %s;'
    timeSince = datetime.datetime.now() - datetime.timedelta(hours = 3)
    params =  (str(timeSince.strftime("%Y-%m-%d %H:%M:%S %z")),)
    cursor_parties.execute(query, params)
    result = cursor_parties.fetchall()
    newFound = []
    if len(result) <= 0:
        return newFound
    for party in result:
        newP = Party(party[0],
                party[1], party[2], party[3],
                party[4], party[6], party[7])
        if addParty(newP):
            newFound.append(newP)
    return newFound


def sortfun(party):
    return party.score

def partyScoreToText(score):
    if score < 20:
        return 'Vähän kämäset bileet.'
    elif score < 50:
        return 'Ihan kivalta vaikuttaa.'
    elif score < 100:
        return 'Aika kova meno!'
    elif score < 200:
        return 'Kova meno!'
    elif score < 350:
        return 'Mayhemit!'
    elif score < 500:
        return 'Full bläst!!'
    else:
        return 'Ihan älytön meininki!!'

def advertisePartiesText():
    global parties
    if len(parties) <= 0:
        return "No mutta. Missään ei oo mitään!"
    parties.sort(key=sortfun, reverse=True)
    text = "Tänään!\n"
    for party in parties:
        text = text +"Osoitteessa "+ party.place
        text = text + " " + str(party.population) + " fyysikkoa!"
        if party.description is not None and len(party.description) > 0 and party.description != "None":
            text = text + " Sanovat että '" + party.description + "'.\n"
        else:
            text = text + "\n"
        text = text + "Heistä kuultu viimeksi " + party.latestTime.strftime("%H:%M") + ". " + \
                partyScoreToText(party.score) +"\n"
    return text



bestPartyscore = 30
def advertiseBest():
    global bestPartyscore
    global parties
    updateParties()
    for p in parties:
        if p.score > bestPartyscore:
            print('Löyty enkka')
            bestPartyscore = p.score
            text = "Nyt niin kovat bileet etten oo ennen nähnyt!\n"
            text = text +"Osoitteessa "+ p.place + " " + str(p.population) + " fyysikkoa "\
                    "saavutti bilepisteet " + str(p.score) + "."
            for id in stored_id:
                bot.sendMessage(id, text)


def respondParty(msg):
    global stored_id
    text = msg['text']
    chat_id = msg['chat']['id']
    if text == '/liikemittari_bailu_help':
        bot.sendMessage(chat_id,
                        "Mis bileet? Kerron kaikista tietämistäni bileistä"
                        " kun vaan kysytte asiaa. Saatan myös mainita jotain "
                        "bileiden laadusta",
                        reply_to_message_id=msg['message_id']
                        )
    elif "etkot" in text.lower():
        updateParties()
        bot.sendMessage(chat_id,
                        "Jaa etkot? " + advertisePartiesText(),
                        reply_to_message_id=msg['message_id']
                        )
    elif "bileet" in text.lower() or "bailu" in text.lower():
        updateParties()
        bot.sendMessage(chat_id,
                        "Jaa bileet? " + advertisePartiesText(),
                        reply_to_message_id=msg['message_id']
                        )
    elif "menoo" in text.lower():
        updateParties()
        bot.sendMessage(chat_id,
                        "Jaa menoo? " + advertisePartiesText(),
                        reply_to_message_id=msg['message_id']
                        )


# ------ BOT STUFF -----
def handler(msg):
    global stored_id
    try:
        # For python 2 without accents:
        # text = strip_acc(msg['text'])
        # For python 3:
        text = msg['text']
        chat_id = msg['chat']['id']
        if text == '/(^^)':
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
        elif text == '/liikemittari_help':
            bot.sendMessage(chat_id,
                            "Hei. Olen fyssasensoreiden ohessa toimiva botti, "
                            "ja tarkoisukseni on löristä satunnaisia noihin "
                            "liittyviä asioita. Tarkempia tietoja saa "
                            "'/liikemittari_bailu_help'- ja"
                            "'/liikemittari_heilutus_help'-komennoilla.",
                            reply_to_message_id=msg['message_id']
                            )
        elif not respondHandwave(msg):
            respondParty(msg)
    except Exception as e:
        print(e)


def send_msg(ch_id, text):
    if text != '':
        bot.sendMessage(ch_id,
                        text
                        )


# Set up the bot
bot = telepot.Bot(token)
bot.message_loop(handler)


while 1:
    time.sleep(10)
    advertiseBest()
    for i in stored_id:
        try:
            send_msg(i, print_hs_data(True, 2))
        except Exception as e:
            print("Error! {}".format(e.strerror))

