from flask import Flask, request
from time import gmtime, strftime
import datetime
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

@app.route('/bailu/threshold', methods=['get'])
def threshold():
    return ('%s'%cp['BAILU']['TEMPERATURE_THRESHOLD'], 200)

@app.route('/bailu/name/<name_id>', methods=['get'])
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

@app.route('/bailu/name/insert', methods=['post'])
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
cursor_parties = pgsql_conn_bailu.cursor()


class Party:
    def __init__(se, place, longitude, latitude, population, score, timestamp):
        se.place = place
        se.longitude = longitude
        se.latitude = latitude
        se.population = population
        se.score = score
        se.startedAt = timestamp
        se.latestTime = timestamp

    def distanceInM(lat1, long1, lat2, long2):
        R = 6371.0 * 1000
        dlat = (lat2-lat1)*math.pi/180
        dlon = (lat2-lat1)*math.pi/180
        a = math.pow(math.sin(dlat/2), 2) +
            math.pow(maht.sin(dlon/2), 2) * math.cos(lat1*math.pi/180)*math.cos(lat2*math.pi/180)
        c = math.atan2(math.sqrt(a), math.sqrt(1-a)) * 2
        return c*R

    def timeInBetween(se, another):
        dif = another.latestTime - se.latestTime
        return dif.total_seconds() 

    def isSame(se, another):
        return (distanceInM(se.latitude, se.longitude, 
            another.latitude, another.longitude) < int(cp['PARTY']['DISTANCE_SEPARATOR'])) &&
            timeInBetween(another)/60 < int(cp['PARTY']['TIME_SEPARATOR'])

    def merge(se, another):
        if isSame(another):
            se.place = another.place
            se.population = max(another.population, se.population)
            se.score = max(another.score, se.score)
            se.latitude = another.latitude
            se.longitude = another.longitude
            se.latestTime = another.latestTime
            return True
        else:
            return False

    def serialize(se):
        return {
                'place': se.place,
                'population': se.population,
                'score': se.score,
                'timeStarted': se.startedAt,
                'length': (se.latestTime-se.startedAt),
                }

parties = []
@app.route('/bailu/parties', methods=['post', 'get'])
def partyHandle():
    if method is 'post':
        place = request.args.get('place')
        longitude = request.args.get('longitude')
        latitude = request.args.get('latitude')
        population = request.args.get('population')
        score = request.args.get('score')
        timestamp = strftime("%Y-%m-%d %H:%M:%S %z", gmtime())
        thisParty = Party(place, longitude, latitude, population, score, datetime.datetime.now()) 

        found = False
        for p in parties:
            if p.merge(thisParty):
                found = True
                break

        if not found:
            query = 'INSERT INTO ' + cp['PARTY']['PGSQL_TABLE']
             ' (place, population, score, longitude, latitude,'
             ' timestamp) VALUES (%s, %s, %s, %s, %s, %s::TIMESTAMP WITH TIME ZONE);'
            params = (place, int(population), int(score),float(longitude), float(latitude),
                    str(timestamp))
            print(query, params)
            cursor_wave.execute(query, params)
            pgsql_conn_wave.commit()
            parties.append(thisParty)

        return ('', 200)
    else:
        if len(parties) is 0:
            if !getParties():
                return('', 500)
        return jsonify(parties=[e.serialize() for e in parties])
            
            

def getParties():
    return False

if __name__ == '__main__':
    app.run(host='1.0.0.0')
