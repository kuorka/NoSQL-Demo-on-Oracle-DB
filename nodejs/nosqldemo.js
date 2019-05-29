const oracledb = require('oracledb');
const config = require('./dbconfig.js');
const fs = require('fs');
const lineByLine = require('n-readlines');

async function run() {
    let connection;

    try {
          console.log('Connected to database...');
          connection = await oracledb.getConnection(config);
          //oracledb.autoCommit = true;


          console.log('Create a collection');
          soda = connection.getSodaDatabase();
		      collection = await soda.createCollection('mycollection');


          console.log('Create index 1');
          collection.createIndex({ "name"   : "requestor_idx",
                                   "fields" : [ { "path" : "Requestor", "datatype" : "string"}] });

          console.log('Create index 2');
          collection.createIndex({ "name"   : "city_idx",
                                   "fields" : [ { "path" : "ShippingInstructions.Address.city", "datatype" : "string"}] });

          console.log('Create index 3');
          collection.createIndex({ "name"   : "street_idx",
                                   "fields" : [ { "path" : "ShippingInstructions.Address.street", "datatype" : "string"}]});


          console.log('Insert one and get');
          let payload = JSON.parse(fs.readFileSync('po.json'));
          let doc = await collection.insertOneAndGet(payload)
          connection.commit()
          console.log('The key of the new SODA document is: ', doc.key)


          console.log('Update a document');
          console.log('old: ', payload.CostCenter)
          payload['CostCenter'] = 'A100';
          await collection.find().key(doc.key).replaceOne(payload)
          newDoc = await collection.find().key(doc.key).getOne();
          content = newDoc.getContent();
          console.log('new: ', content.CostCenter)


          console.log('Load more documents')
          let liner = new lineByLine('PurchaseOrders.json');
          let line;
          while (line = liner.next()) {
            payload = JSON.parse(line);
            await collection.insertOne(payload);
          }
          connection.commit();


          console.log('Retrieve by key as dictionary')
          jsonDoc = await collection.find().key(doc.key).getOne()
          console.log('Retrieved SODA document dictionary is:', jsonDoc.getContent())

          console.log('Retrieve by key as String')
          console.log('Retrieved SODA document string is:', jsonDoc.getContentAsString())


          console.log('Find all documents with street names like 2014 Oxford%');
          myDocuments = await collection.find().filter({'ShippingInstructions.Address.street': {'$like': '2014 Oxford%'}}).getDocuments()
          myDocuments.forEach(function(element) {
            let content = element.getContent();
            console.log(content.ShippingInstructions.Address.street);
          });

          console.log('Filter by Requestor');
          myDocuments = await collection.find().filter({'Requestor': {'$eq': 'Alexander Hunold'}}).getDocuments()
          myDocuments.forEach(function(element) {
            let content = element.getContent();
            console.log(content.Requestor);
          });

          console.log('Filter by Requestor and City');
          myDocuments = await collection.find().filter({'Requestor' : 'David Bernstein', 'ShippingInstructions.Address.city' : 'Oxford'}).getDocuments()
          myDocuments.forEach(function(element) {
            let content = element.getContent();
            console.log(content.ShippingInstructions.Address.city);
          });

          console.log('Drop collection');
          await collection.drop();

          console.log('the end');

	} catch (err) {
		console.error(err);
	} finally {
		if (connection) {
			try {
				await connection.close();
			} catch (err) {
				console.error(err);
			}
		}
	}
}

run();
