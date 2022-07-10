package example;

import java.time.Instant;
import java.util.List;

import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

public class InfluxDBDriverTest {



    public static void main(final String[] args) {

        String token = "FrQ5hTXOEnBUMpRMgehNcj-UJYpJJxyc5j2hZeHYWM-wCscILeM6LgZEcvZEOJ8HvnivI9DO5XHuy64cxXtqig==";
        String bucket = "MM";
        String org = "MM";

        InfluxDBClient client = InfluxDBClientFactory.create("http://192.168.31.253:8086", token.toCharArray());

        String data = "mem1,host=host1 used_percent=25.43234543";

        WriteApiBlocking writeApi = client.getWriteApiBlocking();
        writeApi.writeRecord(bucket, org, WritePrecision.NS, data);

//        String query = "from(bucket: \"MM\") |> range(start: -1h)";
//        List<FluxTable> tables = client.getQueryApi().query(query, org);
//
//        for (FluxTable table : tables) {
//            for (FluxRecord record : table.getRecords()) {
//                System.out.println(record);
//            }
//        }

        client.close();
    }

    @Measurement(name = "temperature")
    private static class Temperature {

        @Column(tag = true)
        String location;

        @Column
        Double value;

        @Column(timestamp = true)
        Instant time;
    }
}