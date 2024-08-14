package iudx.auditing.server.processor;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import iudx.auditing.server.cache.CacheService;
import iudx.auditing.server.immudb.ImmudbService;
import iudx.auditing.server.postgres.PostgresService;
import iudx.auditing.server.processor.subscription.SubscriptionAuditService;
import iudx.auditing.server.rabbitmq.RabbitMqService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static iudx.auditing.server.common.Constants.*;
import static iudx.auditing.server.querystrategy.util.Constants.*;
import static javax.xml.transform.OutputKeys.METHOD;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith({VertxExtension.class, MockitoExtension.class})
class MessageProcessorImplTest {
    private static final Logger LOGGER = LogManager.getLogger(MessageProcessorImplTest.class);
    @Mock
    PostgresService postgresService;
    @Mock
    ImmudbService immudbService;
    @Mock
    JsonObject config;
    MessageProcessorImpl messageProcessor;
    JsonObject message;
    RabbitMqService rabbitMqService;
    @Mock
    CacheService cacheService;
    @Mock
    SubscriptionAuditService subscriptionAuditService;


    @BeforeEach
    public void setUp(VertxTestContext vertxTestContext) {
        config = mock(JsonObject.class);
        messageProcessor =
                new MessageProcessorImpl(postgresService, immudbService, subscriptionAuditService, config, cacheService);
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "prmary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("access_type","api");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin cat-server")
    void testPocess4CatSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject()
                .put(ORIGIN, "cat-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(USER_ID, "userid")
                .put(IID, "iid")
                .put(API, "api")
                .put(HTTP_METHOD, "httpMethod")
                .put(IUDX_ID, "iudxId")
                .put(EPOCH_TIME, 5000)
                .put(CAT_PG_TABLE_NAME, "tableName")
                .put(USER_ROLE, "userRole");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin auth-server")
    void testPocess4AuthSuccess(VertxTestContext vertxTestContext) {

        message = new JsonObject()
                .put(ORIGIN, "auth-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(BODY, new JsonObject())
                .put(IID, "iid")
                .put(API, "api")
                .put(METHOD, "method")
                .put(EPOCH_TIME, 5000)
                .put(USER_ID, "userid")
                .put(AUTH_PG_TABLE_NAME, "tableName");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }


    @Test
    @DisplayName("Test processAuditEventMessages method for DMP server audit logs: Success")
    public void testForDmpSuccess(VertxTestContext vertxTestContext) {
        ZonedDateTime zst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        long time = zst.toInstant().toEpochMilli();
        String isoTime = zst.truncatedTo(ChronoUnit.SECONDS).toString();

        String primaryKey = UUID.randomUUID().toString().replace("-", "");
        String userId = UUID.randomUUID().toString();
        JsonObject information = new JsonObject().put("policyId", UUID.randomUUID().toString()).put("createdAt", isoTime).put("policyStatus", "ACTIVE");
        LOGGER.info("primary key is : {}", primaryKey);
        when(config.getString(DMP_APD_PG_TABLE_NAME)).thenReturn("DMP_APD_PG_TABLE_NAME");
        when(config.getString(DMP_APD_IMMUDB_TABLE_NAME)).thenReturn("DMP_APD_IMMUDB_TABLE_NAME");
        when(immudbService.executeWriteQuery(any())).thenReturn(Future.succeededFuture());
        when(postgresService.executeWriteQuery(any())).thenReturn(Future.succeededFuture());
        message =
                new JsonObject()
                        .put(ORIGIN, DMP_APD_SERVER)
                        .put(DELIVERY_TAG, Long.valueOf(12334245))
                        .put(PRIMARY_KEY, primaryKey)
                        .put(USER_ID, userId)
                        .put(INFORMATION, information)
                        .put(API, "/dx/apd/dmp/v1/policies")
                        .put(HTTP_METHOD, "POST")
                        .put(EPOCH_TIME, time)
                        .put(ISO_TIME, isoTime)
                        .put(DMP_APD_IMMUDB_TABLE_NAME, "DMP_APD_IMMUDB_TABLE_NAME")
                        .put(DMP_APD_PG_TABLE_NAME, "DMP_APD_PG_TABLE_NAME");

        messageProcessor.processAuditEventMessages(message).onComplete(handler -> {
            if (handler.succeeded()) {
                LOGGER.info("Success : " + handler.result().encodePrettily());
                JsonObject result = handler.result();
                assertEquals(result.getLong(DELIVERY_TAG), Long.valueOf(12334245));
                assertEquals(result.getString(ORIGIN), DMP_APD_SERVER);
                assertTrue(result.containsKey(PG_INSERT_QUERY_KEY));
                assertTrue(result.containsKey(PG_DELETE_QUERY_KEY));
                assertTrue(result.containsKey(IMMUDB_WRITE_QUERY));
                vertxTestContext.completeNow();

            } else {
                LOGGER.error("Failure");
                vertxTestContext.failNow("Failure");
            }
        });
    }

    @Test
    @DisplayName("Test processAuditEventMessages method for DMP server audit logs when executeWriteQuery for postgres fails : Failure")
    public void testForDmpFailure(VertxTestContext vertxTestContext) {
        ZonedDateTime zst = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
        long time = zst.toInstant().toEpochMilli();
        String isoTime = zst.truncatedTo(ChronoUnit.SECONDS).toString();

        String primaryKey = UUID.randomUUID().toString().replace("-", "");
        String userId = UUID.randomUUID().toString();
        JsonObject information = new JsonObject().put("policyId", UUID.randomUUID().toString()).put("createdAt", isoTime).put("policyStatus", "ACTIVE");
        LOGGER.info("primary key is : {}", primaryKey);
        when(config.getString(DMP_APD_PG_TABLE_NAME)).thenReturn("DMP_APD_PG_TABLE_NAME");
        when(config.getString(DMP_APD_IMMUDB_TABLE_NAME)).thenReturn("DMP_APD_IMMUDB_TABLE_NAME");
        when(postgresService.executeWriteQuery(any())).thenReturn(Future.failedFuture("Some failure"));
        message =
                new JsonObject()
                        .put(ORIGIN, DMP_APD_SERVER)
                        .put(DELIVERY_TAG, Long.valueOf(12334245))
                        .put(PRIMARY_KEY, primaryKey)
                        .put(USER_ID, userId)
                        .put(INFORMATION, information)
                        .put(API, "/dx/apd/dmp/v1/policies")
                        .put(HTTP_METHOD, "POST")
                        .put(EPOCH_TIME, time)
                        .put(ISO_TIME, isoTime)
                        .put(DMP_APD_IMMUDB_TABLE_NAME, "DMP_APD_IMMUDB_TABLE_NAME")
                        .put(DMP_APD_PG_TABLE_NAME, "DMP_APD_PG_TABLE_NAME");

        messageProcessor.processAuditEventMessages(message).onComplete(handler -> {
            if (handler.succeeded()) {
                LOGGER.info("Success : " + handler.result().encodePrettily());
                vertxTestContext.failNow("Succeeded when the postgres write query failed");

            } else {
                LOGGER.error("Failed to execute postgres query");
                assertTrue(handler.cause().getMessage().contains("failed to insert in postgres"));
                vertxTestContext.completeNow();
            }
        });
    }

    @Test
    @DisplayName("Testing Failed process as origin auth-server")
    void testPocess4AuthFailure(VertxTestContext vertxTestContext) {
        message = new JsonObject()
                .put(ORIGIN, "auth-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(BODY, new JsonObject())
                .put(IID, "iid")
                .put(API, "api")
                .put(METHOD, "method")
                .put(EPOCH_TIME, 5000)
                .put(USER_ID, "userid")
                .put(AUTH_IMMUDB_TABLE_NAME, "tableName");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertEquals("immudb failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Failed process as origin cat-server")
    void testPocess4CatFailure(VertxTestContext vertxTestContext) {
        message = new JsonObject()
                .put(ORIGIN, "cat-server")
                .put(PRIMARY_KEY, "prmary_key")
                .put(USER_ID, "userid")
                .put(IID, "iid")
                .put(API, "api")
                .put(HTTP_METHOD, "httpMethod")
                .put(IUDX_ID, "iudxId")
                .put(EPOCH_TIME, 5000)
                .put(CAT_PG_TABLE_NAME, "tableName")
                .put(USER_ROLE, "userRole");
        when(config.getString((anyString()))).thenReturn("tableName");

        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());
        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertEquals("immudb failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Failed process as origin rs-server")
    void testPocess4RsFailure(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "prmary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "delegator")
                .put("access_type","api");

        when(config.getString((anyString()))).thenReturn("tableName");

        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.failedFuture("failed")).when(immudbService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeDeleteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertEquals("immudb failed", resultJson.cause().getMessage());
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin acl-apd-server")
    void testPocess4AclApdSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, ACL_APD_SERVER)
                .put(USER_ID, "49b52be3-bc00-4548-97d7-99cee5bfc8cd")
                .put(PRIMARY_KEY, "PRIMARY_KEY")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(HTTP_METHOD, "post")
                .put(BODY, new JsonObject())
                .put(APD_WRITE_QUERY_PG, "APD_PG_TABLE_NAME")
                .put(APD_IMMUDB_TABLE_NAME, "APD_IMMUDB_TABLE_NAME");
        when(config.getString((anyString()))).thenReturn("tablename");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess4Created(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "prmary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("eventType", "SUBS_CREATED")
                .put("subscriptionID", "subscriptionID")
                .put("access_type","sub");;

        when(config.getString((anyString()))).thenReturn("");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess4Deleted(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "prmary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("eventType", "SUBS_DELETED")
                .put("subscriptionID", "SUBSDELETED")
                .put("access_type","api");;

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server")
    void testPocess4RsSuccess4Append(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "prmary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("eventType", "SUBS_APPEND")
                .put("subscriptionID", "SUBSDELETED")
                .put("access_type","api");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin consent-log")
    void testPocess4ConsentSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, CONSENT_LOG_ADEX)
                .put(ITEM_ID, "49b52be3-bc00-4548-97d7-99cee5bfc8cd")
                .put(PRIMARY_KEY, "PRIMARY_KEY")
                .put(AIU_ID, "aiuid")
                .put(AIP_ID, "aipid")
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(EVENT_TYPE, "DATA_SENT")
                .put(DP_ID, "dpid")
                .put(ARTIFACT_ID, "Artifactid")
                .put(ITEM_TYPE, "resource")
                .put(LOG_SIGN, "logsign")
                .put(BODY, new JsonObject())
                .put(CONSENT_LOG_PG_TABLE_NAME, "CONSENT_PG_TABLE_NAME")
                .put(CONSENT_LOG_IMMUDB_TABLE_NAME, "CONSENT_IMMUDB_TABLE_NAME");

        when(config.getString((anyString()))).thenReturn("tablename");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin consent-log2")
    void testPocess4ConsentSuccess2(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, CONSENT_LOG_ADEX)
                .put(ITEM_ID, "49b52be3-bc00-4548-97d7-99cee5bfc8cd")
                .put(PRIMARY_KEY, "PRIMARY_KEY")
                .put(AIU_ID, "aiuid")
                .put(AIP_ID, "aipid")
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(EVENT_TYPE, "DATA_SENT")
                .put(DP_ID, "dpid")
                .put(ARTIFACT_ID, "Artifactid")
                .put(ITEM_TYPE, "resource")
                .put(LOG_SIGN, "logsign")
                .put(BODY, new JsonObject())
                .put(CONSENT_LOG_PG_TABLE_NAME, "CONSENT_PG_TABLE_NAME")
                .put(CONSENT_LOG_IMMUDB_TABLE_NAME, "CONSENT_IMMUDB_TABLE_NAME")
                .put(EVENT, "new-event");

        when(config.getString((anyString()))).thenReturn("tablename");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());
        when(cacheService.refreshCache()).thenReturn(null);
        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }


    @Test
    public void testProcessSubscriptionMonitoringMessages(VertxTestContext vertxTestContext) {
        JsonObject message = new JsonObject();
        when(subscriptionAuditService.generateAuditLog(any(JsonObject.class)))
                .thenReturn(Future.succeededFuture());
        messageProcessor.processSubscriptionMonitoringMessages(message);
        verify(subscriptionAuditService, times(1)).generateAuditLog(any(JsonObject.class));
        vertxTestContext.completeNow();
    }

    @Test
    public void testProcessSubscriptionMonitoringMessagesFailure(VertxTestContext vertxTestContext) {
        JsonObject message = new JsonObject();
        when(subscriptionAuditService.generateAuditLog(any(JsonObject.class)))
                .thenReturn(Future.failedFuture("Mocked failure"));
        try {
            messageProcessor.processSubscriptionMonitoringMessages(message).onComplete(handler -> {
                assertTrue(handler.failed());
                assertEquals("Mocked failure", handler.cause().getMessage());
            });
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }
        verify(subscriptionAuditService, times(1)).generateAuditLog(any(JsonObject.class));
        vertxTestContext.completeNow();
    }


    @Test
    @DisplayName("Testing Success process as origin rs-subs-server (subs_updated)")
    void testProcess4RsSubsSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server-subscriptions")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "primary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("eventType", "SUBS_UPDATED")
                .put("subscriptionID", "subsid");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-subs-server (SUBS_CREATED)")
    void testProcess4RsSubsCreateSuccess(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server-subscriptions")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "primary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put("resource", "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("eventType", "SUBS_CREATED")
                .put("subscriptionID", "subsid")
                .put("subscriptionType", "new");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin rs-server-subscriptions")
    void testPocess4RsSubSuccess4Deleted(VertxTestContext vertxTestContext) {
        message = new JsonObject().put(ORIGIN, "rs-server-subscriptions")
                .put(USER_ID, "userid")
                .put(PRIMARY_KEY, "prmary_key")
                .put(ID, "id")
                .put(PROVIDER_ID, "providerId")
                .put(API, "api")
                .put(EPOCH_TIME, 5000)
                .put(ISO_TIME, "2000-03-03T21:00:00Z")
                .put(SIZE, 0)
                .put(RS_PG_TABLE_NAME, "RS_PG_TABLE_NAME")
                .put(RS_IMMUDB_TABLE_NAME, "RS_IMMUDB_TABLE_NAME")
                .put(RESOURCE_GROUP, "resourceGroup")
                .put(TYPE, "type")
                .put(DELEGATOR_ID, "del")
                .put("eventType", "SUBS_DELETED")
                .put("subscriptionID", "SUBSDELETED");

        when(config.getString((anyString()))).thenReturn("tableName");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }

    @Test
    @DisplayName("Testing Success process as origin ogc-server")
    void testPocess4OgcSuccess(VertxTestContext vertxTestContext) {
        message =
                new JsonObject()
                        .put(ORIGIN, "ogc-server")
                        .put(USER_ID, "userid")
                        .put(PRIMARY_KEY, "primary_key")
                        .put(ID, "id")
                        .put(PROVIDER_Id, "providerId")
                        .put(RESOURCE_GROUP, "respourceGroup")
                        .put(API, "api")
                        .put(EPOCH_TIME, 5000)
                        .put(ISO_TIME, "2000-03-03T21:00:00Z")
                        .put(SIZE, 0)
                        .put(OGC_PG_TABLE_NAME, "OGC_PG_TABLE_NAME")
                        .put(OGC_IMMUDB_TABLE_NAME, "OGC_IMMUDB_TABLE_NAME")
                        .put(ITEM_TYPE, "itemType")
                        .put(REQUEST_JSON, new JsonObject());

        when(config.getString((anyString()))).thenReturn("auditing_ogc");
        doAnswer(Answer -> Future.succeededFuture()).when(postgresService).executeWriteQuery(any());
        doAnswer(Answer -> Future.succeededFuture()).when(immudbService).executeWriteQuery(any());

        Future<JsonObject> resultJson = messageProcessor.processAuditEventMessages(message);
        assertTrue(resultJson.result().containsKey(DELIVERY_TAG));
        assertTrue(resultJson.result().containsKey(PG_INSERT_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(PG_DELETE_QUERY_KEY));
        assertTrue(resultJson.result().containsKey(IMMUDB_WRITE_QUERY));
        vertxTestContext.completeNow();
    }
}