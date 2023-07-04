package iudx.auditing.server.rabbitmq.consumers;

import static iudx.auditing.server.common.Constants.*;
import static iudx.auditing.server.common.Constants.DELIVERY_TAG;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQConsumer;
import io.vertx.rabbitmq.RabbitMQOptions;
import iudx.auditing.server.common.RabitMqConsumer;
import iudx.auditing.server.processor.MessageProcessService;

public class SubscriptionMonitoringConsumer implements RabitMqConsumer{
  
  private static final Logger LOGGER = LogManager.getLogger(SubscriptionMonitoringConsumer.class);

  private final RabbitMQClient client;
  private final MessageProcessService msgService;

  private final QueueOptions options =
      new QueueOptions().setKeepMostRecent(true).setMaxInternalQueueSize(1000).setAutoAck(false);

  public SubscriptionMonitoringConsumer(
      Vertx vertx, RabbitMQOptions options, MessageProcessService msgService) {
    this.client = RabbitMQClient.create(vertx, options);
    this.msgService = msgService;
  }


  @Override
  public void start() {
    this.consume();
  }
  
  private void consume() {
    client.start().onSuccess(successHandler -> {
      client.basicConsumer(SUBS_MONITORING_QUEUE, options, receiveResultHandler -> {
        if (receiveResultHandler.succeeded()) {
          RabbitMQConsumer mqConsumer = receiveResultHandler.result();
          mqConsumer.handler(message -> {
            mqConsumer.pause();
            LOGGER.debug("message consumption paused.");
            long deliveryTag = message.envelope().getDeliveryTag();
            JsonObject request = message.body().toJsonObject().put(DELIVERY_TAG, deliveryTag);
            Future<Void> processResult = msgService.processSubscriptionMonitoringMessages(request);
            processResult.onComplete(handler -> {
              if (handler.succeeded()) {
                LOGGER.info("Latest message published in databases.");
                client.basicAck(deliveryTag, true);
                mqConsumer.resume();
                LOGGER.debug("message consumption resumed");
              } else {
                LOGGER.error("Error while publishing messages for processing " + handler.cause().getMessage());
                mqConsumer.resume();
                LOGGER.debug("message consumption resumed");
              }
            });
          });
        }
      });
    }).onFailure(failureHandler -> {
      LOGGER.fatal("Rabbit client startup failed for Latest message Q consumer.");
    });
  }

}
