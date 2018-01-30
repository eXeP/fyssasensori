from flask import Flask, request
from config import *
from time import gmtime, strftime
import psycopg2

app = Flask(__name__)

pgsql_conn = psycopg2.connect('dbname={} user={} host={} password={}'.format(PGSQL_DATABASE, PGSQL_USER, PGSQL_HOST, PGSQL_PASSWORD))

cursor = pgsql_conn.cursor()

@app.route('/handwave', methods=['POST'])
def handwave():
    name = request.args.get('name')
    amount = request.args.get('amount')
    timestamp = strftime("%Y-%m-%d %H:%M:%S %z", gmtime())


    query = 'INSERT INTO testtable2(name, amount, date) VALUES (%s, %s, %s::TIMESTAMP WITH TIME ZONE);'
    params = (name, int(amount), timestamp)

    print(query, params)
    cursor.execute(query, params)
    pgsql_conn.commit()
    return ('', 200)

if __name__ == '__main__':
     app.run(host='0.0.0.0')
