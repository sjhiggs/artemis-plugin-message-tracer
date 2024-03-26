package org.apache.activemq.artemis.plugin.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.activemq.artemis.api.core.ActiveMQException;
import org.apache.activemq.artemis.api.core.Message;
import org.apache.activemq.artemis.api.core.SimpleString;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.postoffice.RoutingStatus;
import org.apache.activemq.artemis.core.server.ActiveMQServer;
import org.apache.activemq.artemis.core.server.HandleStatus;
import org.apache.activemq.artemis.core.server.MessageReference;
import org.apache.activemq.artemis.core.server.ServerConsumer;
import org.apache.activemq.artemis.core.server.ServerSession;
import org.apache.activemq.artemis.core.server.cluster.Bridge;
import org.apache.activemq.artemis.core.server.impl.AckReason;
import org.apache.activemq.artemis.core.server.plugin.ActiveMQServerPlugin;
import org.apache.activemq.artemis.core.transaction.Transaction;

/**
 * ActiveMQServerPlugin to trace messages from producers, possibly through a
 * cluster of brokers, and finally to consumers.
 *
 * TODO: only supports String property values
 * 
 */
public class MessageTracer implements ActiveMQServerPlugin, Serializable {

    private List<String> idNamesList = new ArrayList<String>();

    private List<Pattern> filterAddresses = new ArrayList<Pattern>();

    private String brokerName = "broker";

    @Override
    public void init(Map<String, String> properties) {
        createIdList(properties.get("idNames"));
        createFilters(properties.get("filters"));

    }

    @Override
    public void messageExpired(MessageReference reference, SimpleString messageExpiryAddress, ServerConsumer consumer) throws ActiveMQException {
        if (filter(reference.getMessage())) {
            return;
        }

        MessageTracerLogger.LOGGER.expire(brokerName, getDestinationName(reference.getMessage()),
                String.valueOf(reference.getMessageID()), getCustomIdsMessage(reference.getMessage()),
                "message expired");
    }

    @Override
    public void registered(ActiveMQServer server) {
        Configuration config = server.getConfiguration();
        brokerName = config.getName();
    }

    @Override
    public void afterDeliver(ServerConsumer consumer, MessageReference reference) throws ActiveMQException {
        if (filter(reference.getMessage())) {
            return;
        }
        MessageTracerLogger.LOGGER.afterDeliver(brokerName, getDestinationName(reference.getMessage()),
                String.valueOf(reference.getMessageID()), getCustomIdsMessage(reference.getMessage()),
                "sent to consumer at " + consumer.getConnectionRemoteAddress());
    }

    @Override
    public void afterDeliverBridge(Bridge bridge, MessageReference ref, HandleStatus status) throws ActiveMQException {
        if (filter(ref.getMessage())) {
            return;
        }
        String sfQueueName = bridge.getQueue().getName().toString();
        String remoteAddress = bridge.getForwardingConnection().getRemoteAddress();

        MessageTracerLogger.LOGGER.afterDeliver(brokerName, getDestinationName(ref.getMessage()),
                String.valueOf(ref.getMessageID()), getCustomIdsMessage(ref.getMessage()),
                "sent to cluster at " + remoteAddress + " via queue " + sfQueueName);
    }

    @Override
    public void messageAcknowledged(MessageReference ref, AckReason reason, ServerConsumer consumer)
            throws ActiveMQException {
        if (filter(ref.getMessage())) {
            return;
        }
        if (consumer == null) {
            // TODO: when does this happen?
            // The ack probably from bridge when sending between brokers in the cluster
            // Is there a better way to log this?
        } else {
            MessageTracerLogger.LOGGER.afterAck(brokerName, getDestinationName(ref.getMessage()),
                    String.valueOf(ref.getMessageID()), getCustomIdsMessage(ref.getMessage()),
                    "ack received from consumer at " + consumer.getConnectionRemoteAddress());
        }

    }

    @Override
    public void afterSend(ServerSession session, Transaction tx, Message message, boolean direct,
            boolean noAutoCreateQueue, RoutingStatus result) throws ActiveMQException {
        if (filter(message)) {
            return;
        }
        MessageTracerLogger.LOGGER.afterSend(brokerName, getDestinationName(message),
                String.valueOf(message.getMessageID()), getCustomIdsMessage(message),
                "received by broker from " + session.getRemotingConnection().getRemoteAddress(),
                getRoutingStatus(result));

    }

    private boolean filter(Message m) {
        for (int i = 0; i < filterAddresses.size(); i++) {
            Pattern pattern = filterAddresses.get(i);
            if (pattern.matcher(m.getAddress()).matches()) {
                return true;
            }
        }
        return false;
    }

    private String getDestinationName(Message m) {
        if (m != null) {
            return m.getAddress();
        }
        return "unknown address";
    }

    private String getCustomIdsMessage(Message m) {

        if (m == null || idNamesList.size() < 1) {
            return "";
        }

        StringBuffer idMsg = new StringBuffer();
        int numIds = idNamesList.size();
        for (int i = 0; i < numIds; i++) {
            String idName = idNamesList.get(i);
            String id = m.getStringProperty(idName);
            idMsg.append(idName + "=" + id);
            if (i < numIds - 1) {
                idMsg.append(",");
            }
        }

        return idMsg.toString();
    }

    private void createIdList(String delimitedIdString) {
        if (delimitedIdString != null) {
            idNamesList = Arrays.asList(delimitedIdString.split(","));
        }
    }

    private void createFilters(String delimtedFilterString) {

        if (delimtedFilterString != null) {
            List<String> filterStrings = Arrays.asList(delimtedFilterString.split(","));
            for (String s : filterStrings) {
                filterAddresses.add(Pattern.compile(s));
            }
        }
    }

    private String getRoutingStatus(RoutingStatus status) {

        String statusStr = "UNKNOWN";

        if (status == RoutingStatus.DUPLICATED_ID) {
            statusStr = "duplicate ID";
        } else if (status == RoutingStatus.NO_BINDINGS) {
            statusStr = "no bindings";
        } else if (status == RoutingStatus.NO_BINDINGS_DLA) {
            statusStr = "no bindings DLA";
        } else if (status == RoutingStatus.OK) {
            statusStr = "OK";
        }

        return statusStr;
    }
}
