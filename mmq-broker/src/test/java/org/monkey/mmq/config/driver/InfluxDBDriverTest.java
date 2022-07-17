//package example;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import com.influxdb.annotations.Column;
//import com.influxdb.annotations.Measurement;
//import com.influxdb.client.InfluxDBClient;
//import com.influxdb.client.InfluxDBClientFactory;
//import com.influxdb.client.QueryApi;
//import com.influxdb.client.WriteApiBlocking;
//import com.influxdb.client.domain.WritePrecision;
//import com.influxdb.query.FluxRecord;
//import com.influxdb.query.FluxTable;
//import org.influxdb.BatchOptions;
//import org.influxdb.InfluxDB;
//import org.influxdb.InfluxDBFactory;
//import org.influxdb.dto.Point;
//import org.influxdb.dto.Query;
//import org.influxdb.dto.QueryResult;
//
//public class InfluxDBDriverTest {
//
//
//
//    public static void main(final String[] args) throws InterruptedException {
//
////        String token = "FrQ5hTXOEnBUMpRMgehNcj-UJYpJJxyc5j2hZeHYWM-wCscILeM6LgZEcvZEOJ8HvnivI9DO5XHuy64cxXtqig==";
////        String bucket = "MM";
////        String org = "MM";
////
////        InfluxDBClient client = InfluxDBClientFactory.create("http://192.168.31.253:8086", token.toCharArray());
////
////        String data = "mem1,host=host1 used_percent=25.43234543";
////
////        WriteApiBlocking writeApi = client.getWriteApiBlocking();
////        writeApi.writeRecord(bucket, org, WritePrecision.NS, data);
////
//////        String query = "from(bucket: \"MM\") |> range(start: -1h)";
//////        List<FluxTable> tables = client.getQueryApi().query(query, org);
//////
//////        for (FluxTable table : tables) {
//////            for (FluxRecord record : table.getRecords()) {
//////                System.out.println(record);
//////            }
//////        }
////
////        client.close();
//
//        // Create an object to handle the communication with InfluxDB.
//// (best practice tip: reuse the 'influxDB' instance when possible)
//        final String serverURL = "http://192.168.31.8:8086", username = "root", password = "root";
//        final InfluxDB influxDB = InfluxDBFactory.connect(serverURL, username, password);
//
//// Create a database...
//// https://docs.influxdata.com/influxdb/v1.7/query_language/database_management/
//        String databaseName = "NOAA_water_database";
//        influxDB.query(new Query("CREATE DATABASE " + databaseName));
//        influxDB.setDatabase(databaseName);
//
//// ... and a retention policy, if necessary.
//// https://docs.influxdata.com/influxdb/v1.7/query_language/database_management/
//        String retentionPolicyName = "one_day_only";
////        influxDB.query(new Query("CREATE RETENTION POLICY " + retentionPolicyName
////                + " ON " + databaseName + " DURATION 1d REPLICATION 1 DEFAULT"));
//        influxDB.setRetentionPolicy(retentionPolicyName);
//
//// Enable batch writes to get better performance.
//        influxDB.enableBatch(
//                BatchOptions.DEFAULTS
//                        .threadFactory(runnable -> {
//                            Thread thread = new Thread(runnable);
//                            thread.setDaemon(true);
//                            return thread;
//                        })
//        );
//
//// Close it if your application is terminating or you are not using it anymore.
//        Runtime.getRuntime().addShutdownHook(new Thread(influxDB::close));
//
//// Write points to InfluxDB.
//        influxDB.write(Point.measurement("h2o_feet")
//                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .tag("location", "santa_monica")
//                .addField("level description", "below 3 feet")
//                .addField("water_level", "2.064d")
//                .build());
//
//        influxDB.write(Point.measurement("h2o_feet")
//                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
//                .tag("location", "coyote_creek")
//                .addField("level description", "between 6 and 9 feet")
//                .addField("water_level", "8.12d")
//                .build());
//
//// Wait a few seconds in order to let the InfluxDB client
//// write your points asynchronously (note: you can adjust the
//// internal time interval if you need via 'enableBatch' call).
//        Thread.sleep(5_000L);
//
//// Query your data using InfluxQL.
//// https://docs.influxdata.com/influxdb/v1.7/query_language/data_exploration/#the-basic-select-statement
//        QueryResult queryResult = influxDB.query(new Query("SELECT * FROM h2o_feet"));
//
//        System.out.println(queryResult);
//// It will print something like:
//// QueryResult [results=[Result [series=[Series [name=h2o_feet, tags=null,
////      columns=[time, level description, location, water_level],
////      values=[
////         [2020-03-22T20:50:12.929Z, below 3 feet, santa_monica, 2.064],
////         [2020-03-22T20:50:12.929Z, between 6 and 9 feet, coyote_creek, 8.12]
////      ]]], error=null]], error=null]
//    }
//
//    @Measurement(name = "temperature")
//    private static class Temperature {
//
//        @Column(tag = true)
//        String location;
//
//        @Column
//        Double value;
//
//        @Column(timestamp = true)
//        Instant time;
//    }
//}