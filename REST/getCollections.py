import requests
url = 'http://localhost:9090/ords/hr/soda/latest/'
response = requests.get(url)
print(response.json())

