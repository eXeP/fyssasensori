from flask import Flask, request
from time import gmtime, strftime
import psycopg2
import configparser

cp = configparser.ConfigParser()
cp.read('bailu_config')

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

pgsql_conn_bailu = psycopg2.connect('dbname={} user={} host={} password={}'.format(cp['BAILU']['PGSQL_DATABASE'], cp['BAILU']['PGSQL_USER'], cp['BAILU']['PGSQL_HOST'], cp['BAILU']['PGSQL_PASSWORD']))
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

if __name__ == '__main__':
    app.run(host='0.0.0.0')
