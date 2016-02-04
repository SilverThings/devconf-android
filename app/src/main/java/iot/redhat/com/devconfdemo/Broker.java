package iot.redhat.com.devconfdemo;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jpechane on 18.1.16.
 */
public class Broker {
    private static final Logger LOG = LoggerFactory.getLogger(Broker.class.getSimpleName());
    public static final String STATUS_TOPIC = "ih/message/mobile";

    private MqttAndroidClient mqtt;
    private MainView mainView;
    private String brokerUrl;

    public Broker(final MainView mainView) {
        this.mainView = mainView;
    }

    public Broker reconnect(final String brokerUrl) {
        LOG.info("Re-connecting with URL {}", brokerUrl);
        if (this.brokerUrl != null && this.brokerUrl.equals(brokerUrl)) {
            LOG.debug("Same URL, skipping");
            return this;
        }
        this.brokerUrl = brokerUrl;
        disconnect();
        try {
            LOG.debug("Connecting");
            final MqttConnectOptions opts = new MqttConnectOptions();
            opts.setUserName("mqtt");
            opts.setPassword("mqtt".toCharArray());
            mqtt = new MqttAndroidClient(mainView, brokerUrl, MqttClient.generateClientId());
            mqtt.connect(opts, this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    try {
                        mqtt.subscribe(STATUS_TOPIC, 0);
                        mqtt.setCallback(new MessageListener());
                        LOG.info("Connected");
                    } catch (Exception e) {
                        disconnect();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    disconnect();
                }
            });
        }
        catch (Exception e) {
            LOG.error("Error during broker connection setup", e);
        }
        return this;
    }

    public Broker disconnect() {
        LOG.info("Disconnecting");
        try {
            if (mqtt != null) {
                mqtt.close();
            }
        } catch (Exception e) {
            LOG.error("Error during broker disconnection", e);
        }
        mqtt = null;
        return this;
    }

    public Broker close() {
        LOG.info("Final cleanup");
        try {
            if (mqtt != null) {
                mqtt.close();
                mqtt.unregisterResources();
            }
        } catch (Exception e) {
            LOG.error("Error during broker cleanup", e);
        }
        mqtt = null;
        return this;
    }

    private class MessageListener implements MqttCallback {
        @Override
        public void connectionLost(Throwable cause) {

        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            LOG.info("Topic {} got message {}", topic, message);
            switch (topic) {
                case STATUS_TOPIC:
                    String[] messageItems = new String(message.getPayload()).trim().split(":");
                    if (messageItems.length == 2) {
                        mainView.updateButton(messageItems[0], Integer.parseInt(messageItems[1]), null);
                    }
                    else if (messageItems.length == 3) {
                        mainView.updateButton(messageItems[0], Integer.parseInt(messageItems[1]), messageItems[2]);
                    }
                    break;
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {

        }
    }

}
