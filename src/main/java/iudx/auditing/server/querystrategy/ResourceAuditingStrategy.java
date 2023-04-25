package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.API;
import static iudx.auditing.server.querystrategy.util.Constants.DELETE_QUERY;
import static iudx.auditing.server.querystrategy.util.Constants.EPOCH_TIME;
import static iudx.auditing.server.querystrategy.util.Constants.ID;
import static iudx.auditing.server.querystrategy.util.Constants.ISO_TIME;
import static iudx.auditing.server.querystrategy.util.Constants.PRIMARY_KEY;
import static iudx.auditing.server.querystrategy.util.Constants.PROVIDER_ID;
import static iudx.auditing.server.querystrategy.util.Constants.RS_IMMUDB_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.RS_PG_TABLE_NAME;
import static iudx.auditing.server.querystrategy.util.Constants.RS_WRITE_QUERY_IMMUDB;
import static iudx.auditing.server.querystrategy.util.Constants.RS_WRITE_QUERY_PG;
import static iudx.auditing.server.querystrategy.util.Constants.SIZE;
import static iudx.auditing.server.querystrategy.util.Constants.USER_ID;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ResourceAuditingStrategy implements AuditingServerStrategy {
  private final JsonObject config;

  public ResourceAuditingStrategy(JsonObject config) {
    this.config = config;
  }

  @Override
  public String buildPostgresWriteQuery(JsonObject request) {
    String userId = request.getString(USER_ID);
    String primaryKey = request.getString(PRIMARY_KEY);
    String resourceId = request.getString(ID);
    String providerId = request.getString(PROVIDER_ID);
    String api = request.getString(API);
    long time = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    long responseSize = request.getLong(SIZE);
    String databaseTableName = config.getString(RS_PG_TABLE_NAME);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();
    // In case we need T and Z in UTC
    /*Timestamp timestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());
    String utcTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(timestamp);*/

    return RS_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", api)
        .replace("$3", userId)
        .replace("$4", Long.toString(time))
        .replace("$5", resourceId)
        .replace("$6", isoTime)
        .replace("$7", providerId)
        .replace("$8", Long.toString(responseSize))
        .replace("$9", utcTime.toString());
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {
    String databaseTableName = config.getString(RS_PG_TABLE_NAME);
    String primaryKey = request.getString(PRIMARY_KEY);

    return DELETE_QUERY.replace("$0", databaseTableName).replace("$1", primaryKey);
  }

  @Override
  public String buildImmudbWriteQuery(JsonObject request) {
    String userId = request.getString(USER_ID);
    String primaryKey = request.getString(PRIMARY_KEY);
    String resourceId = request.getString(ID);
    String providerId = request.getString(PROVIDER_ID);
    String api = request.getString(API);
    long time = request.getLong(EPOCH_TIME);
    String isoTime = request.getString(ISO_TIME);
    long responseSize = request.getLong(SIZE);
    String databaseTableName = config.getString(RS_IMMUDB_TABLE_NAME);

    return RS_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", api)
        .replace("$3", userId)
        .replace("$4", Long.toString(time))
        .replace("$5", resourceId)
        .replace("$6", isoTime)
        .replace("$7", providerId)
        .replace("$8", Long.toString(responseSize));
  }
}