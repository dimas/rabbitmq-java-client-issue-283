# rabbitmq-java-client-issue-283
Demonstrate RabbitMQ Java client issue 283

https://github.com/rabbitmq/rabbitmq-java-client/issues/283

```
mvn clean install
java -jar target/rabbitmq-java-client-issue-283-0.1-shaded.jar
```
With the RabbitMQ client 4.1.0 it prints
```
2017-05-29 00:32:24,717 DEBUG [DumbAmqpServer] DumbAmqpServer Accepted connection /127.0.0.1:54673 => /127.0.0.1:30012
2017-05-29 00:32:24,729 ERROR [AMQP Connection 127.0.0.1:30012] c.r.c.i.ForgivingExceptionHandler An unexpected connection driver error occured
com.rabbitmq.client.MalformedFrameException: AMQP protocol version mismatch; we are version 0-9-1, server sent signature 0,0,9,1
	at com.rabbitmq.client.impl.Frame.protocolVersionMismatch(Frame.java:170)
	at com.rabbitmq.client.impl.Frame.readFrom(Frame.java:107)
	at com.rabbitmq.client.impl.SocketFrameHandler.readFrame(SocketFrameHandler.java:164)
	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:578)
	at java.lang.Thread.run(Thread.java:745)
2017-05-29 00:32:24,733 DEBUG [DumbAmqpServer] DumbAmqpServer Accepted connection /0:0:0:0:0:0:0:1:42973 => /0:0:0:0:0:0:0:1:30012
2017-05-29 00:32:24,733 ERROR [AMQP Connection 0:0:0:0:0:0:0:1:30012] c.r.c.i.ForgivingExceptionHandler An unexpected connection driver error occured
com.rabbitmq.client.MalformedFrameException: AMQP protocol version mismatch; we are version 0-9-1, server sent signature 0,0,9,1
	at com.rabbitmq.client.impl.Frame.protocolVersionMismatch(Frame.java:170)
	at com.rabbitmq.client.impl.Frame.readFrom(Frame.java:107)
	at com.rabbitmq.client.impl.SocketFrameHandler.readFrame(SocketFrameHandler.java:164)
	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:578)
	at java.lang.Thread.run(Thread.java:745)
2017-05-29 00:32:24,734 ERROR [main] Test Unexpected exception
java.io.IOException: null
	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:116)
	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:112)
	at com.rabbitmq.client.impl.AMQConnection.start(AMQConnection.java:360)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:920)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:870)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:828)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:996)
	at Test.main(Test.java:34)
Caused by: com.rabbitmq.client.ShutdownSignalException: connection error
	at com.rabbitmq.utility.ValueOrException.getValue(ValueOrException.java:66)
	at com.rabbitmq.utility.BlockingValueOrException.uninterruptibleGetValue(BlockingValueOrException.java:36)
	at com.rabbitmq.client.impl.AMQChannel$BlockingRpcContinuation.getReply(AMQChannel.java:398)
	at com.rabbitmq.client.impl.AMQConnection.start(AMQConnection.java:304)
	... 5 common frames omitted
Caused by: com.rabbitmq.client.MalformedFrameException: AMQP protocol version mismatch; we are version 0-9-1, server sent signature 0,0,9,1
	at com.rabbitmq.client.impl.Frame.protocolVersionMismatch(Frame.java:170)
	at com.rabbitmq.client.impl.Frame.readFrom(Frame.java:107)
	at com.rabbitmq.client.impl.SocketFrameHandler.readFrame(SocketFrameHandler.java:164)
	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:578)
	at java.lang.Thread.run(Thread.java:745)

```
There were clearly two connection attempts to the dumb server - one to IPv4 address and one - to IPv6.

If you change RabbitMQ client dependency in the POM file to 3.6.5:
```
2017-05-29 00:28:07,722 INFO  [main] DumbAmqpServer Started server on port 30012
2017-05-29 00:28:07,748 DEBUG [DumbAmqpServer] DumbAmqpServer Accepted connection /127.0.0.1:54358 => /127.0.0.1:30012
2017-05-29 00:28:07,762 ERROR [main] Test Unexpected exception
java.io.IOException: null
	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:105)
	at com.rabbitmq.client.impl.AMQChannel.wrap(AMQChannel.java:101)
	at com.rabbitmq.client.impl.AMQConnection.start(AMQConnection.java:349)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:824)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:778)
	at com.rabbitmq.client.ConnectionFactory.newConnection(ConnectionFactory.java:898)
	at Test.main(Test.java:34)
Caused by: com.rabbitmq.client.ShutdownSignalException: connection error
	at com.rabbitmq.utility.ValueOrException.getValue(ValueOrException.java:66)
	at com.rabbitmq.utility.BlockingValueOrException.uninterruptibleGetValue(BlockingValueOrException.java:36)
	at com.rabbitmq.client.impl.AMQChannel$BlockingRpcContinuation.getReply(AMQChannel.java:366)
	at com.rabbitmq.client.impl.AMQConnection.start(AMQConnection.java:292)
	... 4 common frames omitted
Caused by: com.rabbitmq.client.MalformedFrameException: AMQP protocol version mismatch; we are version 0-9-1, server sent signature 0,0,9,1
	at com.rabbitmq.client.impl.Frame.protocolVersionMismatch(Frame.java:173)
	at com.rabbitmq.client.impl.Frame.readFrom(Frame.java:110)
	at com.rabbitmq.client.impl.SocketFrameHandler.readFrame(SocketFrameHandler.java:138)
	at com.rabbitmq.client.impl.AMQConnection$MainLoop.run(AMQConnection.java:541)
	at java.lang.Thread.run(Thread.java:745)
```
Only one connection to IPv4 address
