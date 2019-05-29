import requests
import sys

url = 'http://localhost:9090/ords/hr/soda/latest/' + sys.argv[1]
response = requests.delete(url)
if (response.status_code == 200):
    print("Collection droped")
else:
    print("Erro")
