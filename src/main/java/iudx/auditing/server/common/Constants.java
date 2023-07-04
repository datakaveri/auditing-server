package iudx.auditing.server.common;

public class Constants {
  public static final String ORIGIN = "origin";
  public static final String DELIVERY_TAG = "deliveryTag";
  public static final String RESULT = "result";
  public static final String RMQ_SERVICE_ADDRESS = "iudx.auditing.rabbit.service";
  public static final String MSG_PROCESS_ADDRESS = "iudx.auditing.msg.service";
  public static final String IMMUDB_SERVICE_ADDRESS = "iudx.auditing.immudb.service";
  public static final String PG_SERVICE_ADDRESS = "iudx.auditing.postgres.service";
  public static final String PG_INSERT_QUERY_KEY = "postgresInsertQuery";
  public static final String PG_DELETE_QUERY_KEY = "postgresDeleteQuery";
  public static final String IMMUDB_WRITE_QUERY = "immudbWriteQuery";
  
  public static final String AUDIT_LATEST_QUEUE = "auditing-messages";
  public static final String SUBS_MONITORING_QUEUE="";

  public static final String RS_SERVER = "rs-server";
  public static final String CAT_SERVER = "cat-server";

  public static final String AAA_SERVER = "auth-server";

  public static final String FILE_SERVER = "file-server";

  public static final String GIS_SERVER = "gis-server";

  public static final String DI_SERVER = "data-ingestion";
}
