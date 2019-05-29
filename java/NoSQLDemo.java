
import java.sql.*;
import oracle.soda.rdbms.OracleRDBMSClient;
import oracle.soda.*;
import java.util.Properties;
import oracle.jdbc.OracleConnection;
import java.io.*;
import org.json.*;
import java.util.*;

public class NoSQLDemo {
  public static void main(String[] args) {

    String url = "jdbc:oracle:oci:@service_name";
    OracleConnection conn = null;
    try{

       // JDBC connection
      conn = (OracleConnection) DriverManager.getConnection(url, "admin", "password");

       // JDBC implicit statement caching
      conn.setImplicitCachingEnabled(true);
      conn.setStatementCacheSize(50);
      conn.setAutoCommit(false);

      OracleRDBMSClient cl = new OracleRDBMSClient();
      OracleDatabase db = cl.getDatabase(conn);

      System.out.println("Create a collection");
      OracleCollection collection = db.admin().createCollection("mycollection");
      //collection.admin().drop();


      OracleDocument indexSpec = null;
      System.out.println("Create Index 1");
      indexSpec = db.createDocumentFromString("{\"name\" : \"requestor_idx\", \"fields\" : [ {\"path\" : \"Requestor\", \"datatype\" : \"string\"}]}");
      collection.admin().createIndex(indexSpec);

      System.out.println("Create Index 2");
      indexSpec = db.createDocumentFromString("{\"name\" : \"city_idx\", \"fields\" : [{\"path\": \"ShippingInstructions.Address.city\", \"datatype\" : \"string\"}]}");
      collection.admin().createIndex(indexSpec);

      System.out.println("Create Index 3");
      indexSpec = db.createDocumentFromString("{\"name\" : \"street_idx\", \"fields\" : [ { \"path\" : \"ShippingInstructions.Address.street\", \"datatype\" : \"string\"}]}");
      collection.admin().createIndex(indexSpec);


      System.out.println("Load a file");
      String payload = readFile("po.json");
      OracleDocument doc = collection.insertAndGet(db.createDocumentFromString(payload));
      conn.commit();
      System.out.println("The key of the new SODA document is: " + doc.getKey());


      System.out.println("Update a document");
      JSONObject jobj = new JSONObject(payload);
      System.out.println("old: " + jobj.getString("CostCenter"));
      String newPayload = payload.replace("A80", "A100");
      collection.find().key(doc.getKey()).replaceOne(db.createDocumentFromString(newPayload));
      OracleDocument newDoc = collection.find().key(doc.getKey()).getOne();
      jobj = new JSONObject(newDoc.getContentAsString());
      System.out.println("new: " + jobj.getString("CostCenter"));


      System.out.println("Load documents");
      try {
          BufferedReader br = new BufferedReader(new FileReader("PurchaseOrders.json"));
          StringBuilder sb = new StringBuilder();
          String line = br.readLine();
          while (line != null) {
              //sb.append(line);
              collection.insert(db.createDocumentFromString(line));
              line = br.readLine();
          }
      } catch(Exception e) {
          e.printStackTrace();
      }
      conn.commit();

      System.out.println("Retrieve document as String");
      doc = collection.find().key(doc.getKey()).getOne();
      System.out.println("Retrieved SODA document as String is: " + doc.getContentAsString());

      OracleDocument filterSpec = null;
      OracleCursor cur = null;
      OracleDocument resultDoc = null;
      JSONObject shipObj, addressObj = null;
      System.out.println("Names matching '2014 Oxford%'");
      filterSpec = db.createDocumentFromString("{\"ShippingInstructions.Address.street\": {\"$like\": \"2014 Oxford%\"}}");
      cur = collection.find().filter(filterSpec).getCursor();
      while ( cur.hasNext() ) {
        resultDoc = cur.next();
        jobj       = new JSONObject(resultDoc.getContentAsString());
        shipObj    = new JSONObject(jobj.getJSONObject("ShippingInstructions").toString());
        addressObj = new JSONObject(shipObj.getJSONObject("Address").toString());
        System.out.println(addressObj.getString("street"));
      }
      cur.close();


      System.out.println("Filter by Requestor");
      filterSpec = db.createDocumentFromString("{\"Requestor\": {\"$eq\": \"Alexander Hunold\"}}");
      cur = collection.find().filter(filterSpec).getCursor();
      while ( cur.hasNext() ) {
        resultDoc = cur.next();
        jobj       = new JSONObject(resultDoc.getContentAsString());
        System.out.println(jobj.getString("Requestor").toString());
      }
      cur.close();


      System.out.println("Filter by Requestor and city");
      filterSpec = db.createDocumentFromString("{\"Requestor\" : \"David Bernstein\", \"ShippingInstructions.Address.city\" : \"Oxford\"}");
      cur = collection.find().filter(filterSpec).getCursor();
      while ( cur.hasNext() ) {
        resultDoc = cur.next();
        jobj       = new JSONObject(resultDoc.getContentAsString());
        shipObj    = new JSONObject(jobj.getJSONObject("ShippingInstructions").toString());
        addressObj = new JSONObject(shipObj.getJSONObject("Address").toString());
        System.out.println(addressObj.getString("city"));
      }
      cur.close();



      conn.commit();
      collection.admin().drop();
      System.out.println ("\n* Collection dropped *\n");

    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally{
      if (conn != null) {
          try {
            conn.close();
          }
        catch (Exception e) {
        }
      }
    }
  }

  public static String readFile(String filename) {
    String result = "";
    try {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            line = br.readLine();
        }
        result = sb.toString();
    } catch(Exception e) {
        e.printStackTrace();
    }
    return result;
   }
}
