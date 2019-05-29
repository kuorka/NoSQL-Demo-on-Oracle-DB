import requests
import sys

try:
    url = 'http://localhost:9090/ords/hr/soda/latest/' + sys.argv[1] + '/' + sys.argv[2]
    response = requests.delete(url)
    if (response.status_code == 200):
        print("Document deleted")
    else:
        print("Error" + str(response.json()))
except:
    print("Usage: python deleteDocument.py collection doc_id")
