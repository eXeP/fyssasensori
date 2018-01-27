from flask import Flask, request
from config import *
from time import gmtime, strftime
import psycopg2

app = Flask(__name__)

pgsql_conn = psycopg2.connect('dbname={} user={} host={} password={}'.format(PGSQL_DATABASE, PGSQL_USER, PGSQL_HOST, PGSQL_PASSWORD))

cursor = pgsql_conn.cursor()

#class handwave(Resource):
#    def POST(self, name, date_added, amount):
#        print(parser.parse_args)
#        query = 'INSERT INTO testidb(name, date_added, amount) VALUES (%s, %s, %s);'
#        params = (name, timestamp, amount)
#        print(query)
#        cursor.execute(query, params)


#api.add_resource(handwave, '/handwave/<name>/<date_added>/<amount>')

@app.route('/handwave', methods=['POST'])
def handwave():
    name = request.args.get('name')
    amount = request.args.get('amount')
    timestamp = strftime("%Y-%m-%d %H:%M:%S", gmtime())

    query = 'INSERT INTO testtable(name, amount, date) VALUES (%s, %s, %s::TIMESTAMP WITHOUT TIME ZONE);'
    params = (name, int(amount), str(timestamp))

    print(query, params)
    cursor.execute(query, params)
    pgsql_conn.commit()
    return ('', 200)

if __name__ == '__main__':
     app.run()
