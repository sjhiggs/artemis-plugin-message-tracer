/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.artemis.plugin.message;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * Logger Code 99
 *
 * each message id must be 6 digits long starting with 99, the 3rd digit donates the level so
 *
 * INF0  1
 * WARN  2
 * DEBUG 3
 * ERROR 4
 * TRACE 5
 * FATAL 6
 *
 * so an INFO message would be 841000 to 841999
 */
@MessageLogger(projectCode = "AMQ")
public interface MessageTracerLogger extends BasicLogger {

   MessageTracerLogger LOGGER = Logger.getMessageLogger(MessageTracerLogger.class, MessageTracerLogger.class.getPackage().getName());

   @LogMessage(level = Logger.Level.INFO)
   @Message(id = 991000, value = "{0}: {1}: messageId={2},{3}: {4}", format = Message.Format.MESSAGE_FORMAT)
   void afterDeliver(String brokerName, String destination, String messageId, String customIdMsg, String msg);

   @LogMessage(level = Logger.Level.INFO)
   @Message(id = 991001, value = "{0}: {1}: messageId={2},{3}: {4} (status {5})", format = Message.Format.MESSAGE_FORMAT)
   void afterSend(String brokerName, String destination, String messageId, String customIdMsg, String msg, String status);

   @LogMessage(level = Logger.Level.INFO)
   @Message(id = 991002, value = "{0}: {1}: messageId={2},{3}: {4}", format = Message.Format.MESSAGE_FORMAT)
   void afterAck(String brokerName, String destination, String messageId, String customIdMsg, String msg);
}

