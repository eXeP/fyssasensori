from flask import Flask, request, jsonify
from time import gmtime, strftime, mktime
import math
import datetime
import pytz
import psycopg2
import configparser

cp = configparser.ConfigParser()
cp.read('server_config')

app = Flask(__name__)

pgsql_conn_wave = psycopg2.connect('dbname={} user={} host={} password={}'.format(\
        cp['HANDWAVE']['PGSQL_DATABASE'], cp['HANDWAVE']['PGSQL_USER'],\
        cp['HANDWAVE']['PGSQL_HOST'], cp['HANDWAVE']['PGSQL_PASSWORD']))
cursor_wave = pgsql_conn_wave.cursor()

@app.route('/handwave', methods=['POST'])
def handwave():
    name = request.args.get('name')
    amount = request.args.get('amount')
    timestamp = strftime("%Y-%m-%d %H:%M:%S %z", gmtime())

    query = 'INSERT INTO handwavetable (name, amount, date) VALUES (%s, %s, %s::TIMESTAMP WITH TIME ZONE);'
    params = (name, int(amount), str(timestamp))

    print(query, params)                                #debug

    cursor_wave.execute(query, params)
    pgsql_conn_wave.commit()
    return ('', 200)

#Fyssa bailu app

pgsql_conn_bailu = psycopg2.connect('dbname={} user={} host={} password={}'.format(\
        cp['BAILU']['PGSQL_DATABASE'], cp['BAILU']['PGSQL_USER'],
        cp['BAILU']['PGSQL_HOST'], cp['BAILU']['PGSQL_PASSWORD']))
cursor_bailu = pgsql_conn_bailu.cursor()

@app.route('/bailu/threshold', methods=['GET'])
def threshold():
    return ('%s'%cp['BAILU']['TEMPERATURE_THRESHOLD'], 200)

@app.route('/bailu/name/<name_id>', methods=['GET'])
def getName(name_id):
    query = 'SELECT name FROM bailutable WHERE mac=%s;'
    params = (name_id,)
    print(query, params)
    cursor_bailu.execute(query, params)
    result = cursor_bailu.fetchone()
    if result is not None:
      return ((name_id + result[0]).encode('utf-8'), 200)
    else:
      return (name_id + u'Anonymous partyer', 200)

@app.route('/bailu/name/insert', methods=['POST'])
def insertName():
    name = request.args.get('name')[:20]
    serial = request.args.get('mac')
    if name is None or serial is None or len(name) == 0 or len(serial) == 0:
        return 202
    query = 'UPDATE bailutable SET name=%s WHERE mac=%s;'
    params = (name, serial)
    print(query, params)
    cursor_bailu.execute(query, params)
    if cursor_bailu.rowcount == 0:
      query = 'INSERT INTO bailutable (name, mac) VALUES (%s, %s)'
      print(query, params)
      cursor_bailu.execute(query, params)
    pgsql_conn_bailu.commit()
    return (serial, 200)

# Where are the parties

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
        print(dist, max_dist, timeD, max_t)
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
@app.route('/bailu/parties', methods=['POST', 'GET'])
def partyHandle():
    if request.method == 'POST':
        place = request.args.get('place')
        longitude = request.args.get('longitude')
        latitude = request.args.get('latitude')
        population = request.args.get('population')
        score = request.args.get('score')
        desc = request.args.get('description')
        timestamp = strftime("%Y-%m-%d %H:%M:%S %z", gmtime())
        thisParty = Party(place, longitude, latitude, population, score, datetime.datetime.now().replace(tzinfo=psycopg2.tz.FixedOffsetTimezone(offset=120)), desc)

        found = False
        for p in parties:
            if p.merge(thisParty):
                found = True
                break

        if not found:
            query = 'INSERT INTO ' + cp['PARTIES']['PGSQL_TABLE'] + \
                ' (place, longitude, latitude, population, score,' + \
                ' timestamp, description) VALUES (%s, %s, %s, %s, %s, %s::TIMESTAMP WITH TIME ZONE, %s);'
            params = (place, float(longitude), float(latitude),int(population), int(score),str(timestamp), str(desc))
            print(query % params)
            cursor_parties.execute(query, params)

            pgsql_conn_parties.commit()
            if len(parties) < 100:
                parties.append(thisParty)
            else:
                getParties()
        return ('', 200)
    elif request.method == 'GET':
        if len(parties) is 0:
            getParties()
        filterParties():
        if len(parties) > 0:
            return jsonify(parties=[e.serialize() for e in parties])
        else: 
            return('', 202)
    else:
        return ('', 404)

def filterParties():
    for p in parties
        if p.latestTime < datetime.datetime.now().replace(tzinfo=psycopg2.tz.FixedOffsetTimezone(offset=120)) - datetime.timedelta(hours = 3):
            parties.remove(p)

def getParties():
    del parties[:]
    query = 'SELECT * FROM '  + cp['PARTIES']['PGSQL_TABLE'] + ' WHERE timestamp >= %s;'
    timeSince = datetime.datetime.now() - datetime.timedelta(hours = 10)
    params =  (str(timeSince.strftime("%Y-%m-%d %H:%M:%S %z")),)
    cursor_parties.execute(query, params)
    result = cursor_parties.fetchall()
    if len(result) <= 0:
        return False
    for party in result:
        print(party)
        dt = party[5]
        parties.append(Party(party[0], party[1], party[2], party[3],party[4], dt, party[6]))
    return True
getParties()

if __name__ == '__main__':
    app.run(host='0.0.0.0')
