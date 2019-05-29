import requests
import sys

try:
    url = 'http://localhost:9090/ords/hr/soda/latest/' + sys.argv[1] + '/' + sys.argv[2]
    response = requests.get(url)
    print(response.json())
except:
    print("Usage: getDocumentID.py collection doc_id")

