package iudx.auditing.server.processor;

import static iudx.auditing.server.common.Constants.*;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditService;
import iudx.auditing.server.querystrategy.AuditingServerStrategy;
import iudx.auditing.server.querystrategy.ServerOrigin;
import iudx.auditing.server.querystrategy.ServerOriginContextFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MessageProcessorImpl implements MessageProcessService {

  private static final Logger LOGGER = LogManager.getLogger(MessageProcessorImpl.class);
  private final PostgresService postgresService;
  private final ImmudbService immudbService;
  private final JsonObject config;
  private final CacheService cacheService;
  private SubscriptionAuditService subsAuditService;

  public MessageProcessorImpl(
      PostgresService postgresService,
      ImmudbService immudbService,
      SubscriptionAuditService subsAuditService,
      JsonObject config,
      CacheService cacheService) {
    this.postgresService = postgresService;
    this.immudbService = immudbService;
    this.config = config;
    this.subsAuditService = subsAuditService;
    this.cacheService = cacheService;
  }

  @Override
  public Future<JsonObject> processAuditEventMessages(JsonObject message) {
    LOGGER.info("message processing starts : ");
    JsonObject queries = queryBuilder(message);
    queries.put(DELIVERY_TAG, message.getLong(DELIVERY_TAG));
    queries.put(ORIGIN, message.getString(ORIGIN));
    Promise<JsonObject> promise = Promise.promise();
    if (message.containsKey(EVENT)) {
      cacheService.refreshCache();
    }
    databaseOperations(queries)
        .onComplete(
            dbHandler -> {
              if (dbHandler.succeeded()) {
                LOGGER.info("Inserted successfully for the Origin {}", message.getString(ORIGIN));
                promise.complete(dbHandler.result());
              } else {
                LOGGER.error(dbHandler.cause());
                promise.fail(dbHandler.cause());
              }
            });
    return promise.future();
  }

  private JsonObject queryBuilder(JsonObject request) {
    LOGGER.trace("queryBuilder started");
    String origin = request.getString(ORIGIN);
    ServerOrigin serverOrigin = ServerOrigin.fromRole(origin);
    ServerOriginContextFactory serverOriginContextFactory = new ServerOriginContextFactory(config);
    AuditingServerStrategy serverStrategy = serverOriginContextFactory.create(serverOrigin);
    String postgresWriteQuery = serverStrategy.buildPostgresWriteQuery(request);
    String postgresDeleteQuery = serverStrategy.buildPostgresDeleteQuery(request);
    String immudbWriteQuery = serverStrategy.buildImmudbWriteQuery(request);
    return new JsonObject()
        .put(PG_INSERT_QUERY_KEY, postgresWriteQuery)
        .put(PG_DELETE_QUERY_KEY, postgresDeleteQuery)
        .put(IMMUDB_WRITE_QUERY, immudbWriteQuery)
        .put(ORIGIN, origin);
  }

  @Override
  public Future<Void> processSubscriptionMonitoringMessages(JsonObject message) {
    LOGGER.trace("processSubscriptionMonitoringMessages started");
    Promise<Void> promise = Promise.promise();
    subsAuditService
        .generateAuditLog(message)
        .onComplete(
            logHandler -> {
              if (logHandler.succeeded()) {
                promise.complete();
              } else {
                promise.fail(logHandler.cause());
              }
            });
    return promise.future();
  }

  private Future<JsonObject> databaseOperations(JsonObject queries) {
    LOGGER.trace("databaseOperations started");
    Promise<JsonObject> promise = Promise.promise();
    Future<JsonObject> insertInPostgres = postgresService.executeWriteQuery(queries);
    LOGGER.debug(
        "Queries from origin is {} ", queries.getString(ORIGIN));
    insertInPostgres
        .onSuccess(
            insertInImmudbHandler -> {
              Future<JsonObject> insertInImmudb = immudbService.executeWriteQuery(queries);
              insertInImmudb.onComplete(
                  immudbHandler -> {
                    if (insertInImmudb.succeeded()) {
                      promise.complete(queries);
                    } else {
                      LOGGER.error(
                          "Failed: unable to update immudb table for server origin" + " {}",
                          queries.getString(ORIGIN));
                      Future<JsonObject> deleteFromPostgres =
                          postgresService.executeDeleteQuery(queries);
                      deleteFromPostgres.onComplete(
                          postgresHandler -> {
                            if (deleteFromPostgres.succeeded()) {
                              LOGGER.error(
                                  "Rollback : success delete. Message Origin: {}",
                                  queries.getString(ORIGIN));
                              promise.fail(immudbHandler.cause().getMessage());
                            } else {
                              LOGGER.info("Rollback : delete failed");
                              promise.fail(deleteFromPostgres.cause().getMessage());
                            }
                          });
                    }
                  });
            })
        .onFailure(
            failureHandler -> {
              String serverOrigin = queries.getString(ORIGIN);
              promise.fail(
                  "failed to insert in postgres for server origin["
                      + serverOrigin
                      + "]"
                      + failureHandler.getCause());
            });

    return promise.future();
  }
}
