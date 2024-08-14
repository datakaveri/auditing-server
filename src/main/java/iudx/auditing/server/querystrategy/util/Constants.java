package iudx.auditing.server.querystrategy.util;

public class Constants {
  public static final String REQUEST_JSON = "request_json";
  public static final String SIZE = "response_size";
  public static final String ID = "id";
  public static final String USER_ID = "userid";
  public static final String PRIMARY_KEY = "primaryKey";
  public static final String PROVIDER_ID = "providerID";
  public static final String PROVIDER_Id = "providerId";
  public static final String ISO_TIME = "isoTime";
  public static final String BODY = "body";
  public static final String IUDX_ID = "iudxID";
  public static final String IID = "iid";
  public static final String USER_ROLE = "userRole";
  public static final String HTTP_METHOD = "httpMethod";
  public static final String INFORMATION = "info";
  public static final String RESOURCE_GROUP = "resourceGroup";
  public static final String TYPE = "type";
  public static final String DELEGATOR_ID = "delegatorId";
  public static final String EPOCH_TIME = "epochTime";
  public static final String API = "api";
  public static final String RS_PG_TABLE_NAME = "postgresRsTableName";
  public static final String RS_SUBS_TABLE_NAME = "postgresSubscriptionAuditTableName";
  public static final String RS_IMMUDB_TABLE_NAME = "immudbRsTableName";
  public static final String AUTH_PG_TABLE_NAME = "postgresAuthTableName";
  public static final String AUTH_IMMUDB_TABLE_NAME = "immudbAuthTableName";
  public static final String CAT_PG_TABLE_NAME = "postgresCatTableName";
  public static final String CAT_IMMUDB_TABLE_NAME = "immudbCatTableName";
  public static final String APD_PG_TABLE_NAME = "postgresAclApdTableName";
  public static final String DMP_APD_PG_TABLE_NAME = "postgresDmpApdTableName";
  public static final String DMP_APD_IMMUDB_TABLE_NAME = "immudbDmpApdTableName";
  public static final String APD_IMMUDB_TABLE_NAME = "immudbApdTableName";
  public static final String CONSENT_LOG_PG_TABLE_NAME = "postgresConsentLogTableName";
  public static final String CONSENT_LOG_IMMUDB_TABLE_NAME = "immudbConsentLogTableName";
  public static final String OGC_IMMUDB_TABLE_NAME = "immudbOgcTableName";
  public static final String OGC_PG_TABLE_NAME = "postgresOgcTableName";
  public static final String ITEM_ID = "item_id";
  public static final String EVENT_TYPE = "event";
  public static final String AIU_ID = "aiu_id";
  public static final String AIP_ID = "aip_id";
  public static final String DP_ID = "dp_id";
  public static final String ARTIFACT_ID = "artifact";
  public static final String LOG_SIGN = "log";
  public static final String ITEM_TYPE = "item_type";
  public static final String RS_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size) "
          + "VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8)";
  public static final String RS_WRITE_QUERY_PG =
      "INSERT INTO $0 "
          + "(id,api,userid,epochtime,resourceid,isotime,providerid,size,time,resource_group,item_type,delegator_id,access_type)"
          + " VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8,'$9','$a','$b','$c','$d')";
  public static final String CAT_WRITE_QUERY_PG =
      "INSERT INTO $0 (id, userRole, userID, iid, api, method, time, iudxID) "
          + "VALUES ('$1','$2','$3','$4','$5','$6',$7,'$8')";
  public static final String CAT_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id, userRole, userID, iid, api, method, time, iudxID) "
          + "VALUES ('$1','$2','$3','$4','$5','$6',$7,'$8')";
  public static final String AUTH_WRITE_QUERY_PG =
      "INSERT INTO $0 (id,body,endpoint,method,time,userid) VALUES ('$1','$2','$3','$4','$5','$6')";
  public static final String AUTH_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id,body,endpoint,method,time,userid) VALUES ('$1','$2','$3','$4','$5','$6')";
  public static final String DELETE_QUERY = "DELETE FROM $0 WHERE id = '$1';";
  public static final String DELETE_QUERY_FOR_DMP = "DELETE FROM $0 WHERE _id = '$1';";
  public static final String DELETE_SUBSCRIPTION_QUERY =
      "DELETE FROM $0 WHERE subscription_id = '$1';";

  public static final String RS_SUBS_WRITE_QUERY_PG =
      "INSERT INTO $0 (subscription_id, user_id, event_type, subscription_type, resource_id)"
          + " VALUES ('$1','$2','$3','$4','$5');";
  public static final String RS_SUBS_UPDATE_QUERY_PG =
      "UPDATE $0 SET event_type = '$1' WHERE subscription_id = '$2'";
  public static final String APD_WRITE_QUERY_PG =
      "INSERT INTO $0 (id,userid,endpoint,method,body,size,time) VALUES ('$1','$2','$3','$4','$5',$6,'$7')";
  public static final String DMP_APD_WRITE_QUERY_POSTGRES =
      "INSERT INTO $0 (_id, user_id, api, method, info, time) VALUES ('$1', '$2', '$3', '$4', '$5', '$6');";
  public static final String DMP_APD_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (_id, user_id, api, method, info, epoch_time, iso_time)"
          + " VALUES ('$1','$2','$3','$4','$5','$6','$7')";
  public static final String APD_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id,userid,endpoint,method,body,size,epochtime,isotime)"
          + " VALUES ('$1','$2','$3','$4','$5',$6,$7,'$8')";
  public static final String CACHE_QUERY =
      "select queue_name, entity,user_id,resource_group,"
          + "delegator_id,item_type,provider_id from subscriptions where expiry > now()";

  public static final String CONSENT_LOG_WRITE_QUERY_PG =
      "INSERT INTO $0 "
          + "(_id,item_id,item_type,event,aiu_id,aip_id,dp_id,artifact,created_at,log)"
          + " VALUES ('$1','$2','$3','$4','$5','$6','$7','$8','$9','$a')";

  public static final String CONSENT_LOG_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 "
          + "(_id,item_id,item_type,event,aiu_id,aip_id,dp_id,artifact,isotime,shalog)"
          + " VALUES ('$1','$2','$3','$4','$5','$6','$7','$8','$9','$a')";
  public static final String DELETE_QUERY_CONSENT = "DELETE FROM $0 WHERE _id = '$1';";

  public static final String OGC_WRITE_QUERY_IMMUDB =
      "INSERT INTO $0 (id,api,userid,epochtime,resourceid,isotime,providerid,size,resource_group) "
          + "VALUES ('$1','$2','$3',$4,'$5','$6','$7',$8,'$9')";
  public static final String OGC_WRITE_QUERY_PG =
      "INSERT INTO $0 "
          + "(id,userid,api,request_json,size,resourceid,providerid,resource_group,epochtime,time,isotime,delegator_id)"
          + " VALUES ('$1','$2','$3','$4',$5,'$6','$7','$8','$9','$a','$b','$c')";

  public static final String ERROR_UNIQUE_KEY =
      "(.*)duplicate key value violates unique constraint(.*)";
}
