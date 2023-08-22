package iudx.auditing.server.querystrategy;

import static iudx.auditing.server.querystrategy.util.Constants.*;

import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class OgcAuditingStrategy implements AuditingServerStrategy {

  private final JsonObject config;

  public OgcAuditingStrategy(JsonObject config) {
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
    String itemType = request.getString(ITEM_TYPE);
    String databaseTableName = config.getString(OGC_PG_TABLE_NAME);

    ZonedDateTime zonedDateTime = ZonedDateTime.parse(isoTime);
    zonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));

    LocalDateTime utcTime = zonedDateTime.toLocalDateTime();
    // In case we need T and Z in UTC
    /*Timestamp timestamp = Timestamp.valueOf(zonedDateTime.toLocalDateTime());
    String utcTime = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'").format(timestamp);*/

    return OGC_WRITE_QUERY_PG
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", api)
        .replace("$3", userId)
        .replace("$4", Long.toString(time))
        .replace("$5", resourceId)
        .replace("$6", providerId)
        .replace("$7", itemType)
        .replace("$8", Long.toString(responseSize))
        .replace("$9", utcTime.toString());
  }

  @Override
  public String buildPostgresDeleteQuery(JsonObject request) {

    String databaseTableName = config.getString(OGC_PG_TABLE_NAME);
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
    String itemType = request.getString(ITEM_TYPE);
    String databaseTableName = config.getString(OGC_IMMUDB_TABLE_NAME);
    return OGC_WRITE_QUERY_IMMUDB
        .replace("$0", databaseTableName)
        .replace("$1", primaryKey)
        .replace("$2", api)
        .replace("$3", userId)
        .replace("$4", Long.toString(time))
        .replace("$5", resourceId)
        .replace("$6", isoTime)
        .replace("$7", providerId)
        .replace("$8", Long.toString(responseSize))
        .replace("$9", itemType);
  }
}
