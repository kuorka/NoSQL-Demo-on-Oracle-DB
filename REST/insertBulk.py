import requests
import sys

try:
    url = 'http://localhost:9090/ords/hr/soda/latest/' + sys.argv[1] + "?action=insert"
    jsonFile = open(sys.argv[2], 'rb')
    header_input = {"Content-Type" : "application/json"}
    response = requests.post(url, data=jsonFile, headers=header_input)
    if (response.status_code == 201):
        print("Document inserted: " + str(response.json()))
    else:
        print("Error" + str(response.json()))
except:
    print("Usage: python insertBulk.py collection json_file")
