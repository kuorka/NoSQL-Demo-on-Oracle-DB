import cx_Oracle
import json

# Connect to Database
print('Connect to database')
connection = cx_Oracle.connect("admin", "password", "service_name")
soda = connection.getSodaDatabase()

# Create a Collection
print('create a collection')
collection = soda.createCollection("mycollection")

# create btree index
print('Create Index 1')
collection.createIndex({ "name"   : "requestor_idx",
                         "fields" : [ { "path" : "Requestor", "datatype" : "string"}] })

print('Create Index 2')
collection.createIndex({ "name"   : "city_idx",
                         "fields" : [ { "path" : "ShippingInstructions.Address.city", "datatype" : "string"}] })

print('Create Index 3')
collection.createIndex({ "name"   : "street_idx",
                         "fields" : [ { "path" : "ShippingInstructions.Address.street", "datatype" : "string"}] })

# Insert one and get
print('Load a document')
payload = json.load(open('po.json'))
doc = collection.insertOneAndGet(payload)
connection.commit()
print('The key of the new SODA document is: ', doc.key)

# Update a document
print('old: ' + payload['CostCenter'])
payload.update({'CostCenter': 'A100'})
collection.find().key(doc.key).replaceOne(payload)
print('new: ' + collection.find().key(doc.key).getOne().getContent()['CostCenter'])

# Load more documents
print('Load more documents')
with open('PurchaseOrders.json', 'r') as f_in:
    for line in f_in:
        collection.insertOne(json.loads(line))
connection.commit()

# Retrieve document as dictionary
doc = collection.find().key(doc.key).getOne() # A SodaDocument
print('Retrieved SODA document dictionary is:')
print(doc.getContent())

# Return document as String
print('Retrieved SODA document string is:')
print(doc.getContentAsString())

# Find all documents with street names like '2014 Oxford%'
print("Names matching '2014 Oxford%'")
documents = collection.find().filter({'ShippingInstructions.Address.street': {'$like': '2014 Oxford%'}}).getDocuments()
for d in documents:
    content = d.getContent()['ShippingInstructions']['Address']['street']
    print(content)

## Filter by Requestor
print("Filter by Requestor")
documents = collection.find().filter({'Requestor': {'$eq': 'Alexander Hunold'}}).getDocuments()
results = [document.getContent() for document in documents]
print(results)

## Filter by city
print("Filter by Requestor and City")
documents = collection.find().filter({'Requestor' : 'David Bernstein', 'ShippingInstructions.Address.city' : 'Oxford'}).getDocuments()
results = [document.getContent() for document in documents]
print(results)

# Drop collection
#collection.drop()
#print('Collection droped')
