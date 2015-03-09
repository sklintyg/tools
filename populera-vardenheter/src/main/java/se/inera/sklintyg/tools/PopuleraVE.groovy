package se.inera.sklintyg.tools

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.xml.StreamingMarkupBuilder
import groovyx.gpars.GParsPool

import java.sql.SQLException;
import java.sql.Timestamp
import java.util.concurrent.atomic.AtomicInteger

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.dbcp2.BasicDataSource

class PopuleraVE {
	BasicDataSource dataSource;
	Sql sql = null;
	
    public PopuleraVE() {
		def properties = new Properties();
		
		new File(System.getProperty("dataSourceFile", "dataSource.properties")).withInputStream { stream ->
			properties.load(stream);
		}
		
		def userName = properties.get("dataSource.username");
		def password = properties.get("dataSource.password");
		def driverClassName = properties.get("dataSource.driver");
		def url = properties.get("dataSource.url");
		
		dataSource =
		new BasicDataSource(driverClassName: driverClassName, url: url,
							username: userName, password: password,
							initialSize: 1, maxTotal: 1);
						
		sql = new Sql(dataSource);
	}
	
	def run(){

		new File(System.getProperty("csvFile", "ve.csv")).withInputStream {
			def parser = new CSVParser(new InputStreamReader(it), CSVFormat.DEFAULT.withHeader().withIgnoreSurroundingSpaces());
			parser.each { writeRecord(it) }
		}
	}
	
	def writeRecord(def record){
		def vals = [record.get(0), record.get(1), record.get(2), record.get(3), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis())];
		
		try{
			sql.execute('INSERT INTO INTEGRERADE_VARDENHETER (VARDGIVAR_ID, VARDGIVAR_NAMN, ENHETS_ID, ENHETS_NAMN, SKAPAD_DATUM, SENASTE_KONTROLL_DATUM) VALUES (?,?,?,?,?,?)', vals);
		} catch(SQLException e){
			println e.message
		}
	}

	static void main(String[] args) {
		new PopuleraVE().run();
    }

}
