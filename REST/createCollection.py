import requests
import sys

url = 'http://localhost:9090/ords/hr/soda/latest/' + sys.argv[1]
response = requests.put(url)
if (response.status_code == 201):
    print("Collection created")
else:
    print("Erro")
