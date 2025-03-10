/*
* Created by: IBM.
* Adapted and upgraded by: Santiago Garcia Arango.
*/

package com.ibm.mq.samples.jms;


import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * A minimal and simple application for Point-to-point messaging.
 *
 * Application makes use of fixed literals, any customisations will require
 * re-compilation of this source file. Application assumes that the named queue
 * is empty prior to a run.
 *
 * Notes:
 *
 * API type: JMS API (v2.0, simplified domain)
 *
 * Messaging domain: Point-to-point
 *
 * Provider type: IBM MQ
 *
 * Connection mode: Client connection
 *
 * JNDI in use: No
 *
 */
public class JmsPutGet {

	// System exit status value (assume unset value to be 1)
	private static int status = 1;

	// Create variables for the connection to MQ
	private static final String HOST = "ibm_mq_santi_data"; // Host name or IP address
	private static final int PORT = 1414; // Listener port for your queue manager
	private static final String CHANNEL = "DEV.APP.SVRCONN"; // Channel name
	private static final String QMGR = "MY_QMGR_DEV"; // Queue manager name
	private static final String APP_USER = System.getenv("APP_USER"); // User name that application uses to connect to MQ
	private static final String APP_PASSWORD = System.getenv("APP_PASSWORD"); // Password that the application uses to connect to MQ
	private static final String QUEUE_NAME = "DEV.SANTI.QL.1"; // Queue that the application uses to put and get messages to and from


	/**
	 * Main method
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		// Add initial delay of 20 seconds while MQ starts (to work properly)
		try {
			Thread.sleep(20000);
		} catch(InterruptedException ex) {
			Thread.currentThread().interrupt();
		}

		// Continuosly send/read the messages sent to the QUEUE (with delays)
		while (true) {
			// Variables
			JMSContext context = null;
			Destination destination = null;
			JMSProducer producer = null;
			JMSConsumer consumer = null;

			try {
				// Create a connection factory
				JmsFactoryFactory ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
				JmsConnectionFactory cf = ff.createConnectionFactory();

				// Set the properties
				cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, HOST);
				cf.setIntProperty(WMQConstants.WMQ_PORT, PORT);
				cf.setStringProperty(WMQConstants.WMQ_CHANNEL, CHANNEL);
				cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
				cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, QMGR);
				cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME, "JmsPutGet (JMS)");
				cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
				cf.setStringProperty(WMQConstants.USERID, APP_USER);
				cf.setStringProperty(WMQConstants.PASSWORD, APP_PASSWORD);
				//cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, "*TLS12");

				// Create JMS objects
				context = cf.createContext();
				destination = context.createQueue("queue:///" + QUEUE_NAME);

				long uniqueNumber = System.currentTimeMillis() % 1000;
				TextMessage message = context.createTextMessage("Santi says that your lucky number is: " + uniqueNumber);

				producer = context.createProducer();
				producer.send(destination, message);
				System.out.println("--> Sent message was:\n" + message);

				// Add delay of 10 seconds (to see how the messages are added/deleted on the queue)
				try {
					Thread.sleep(10000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				consumer = context.createConsumer(destination); // autoclosable
				String receivedMessage = consumer.receiveBody(String.class, 15000); // in ms or 15 seconds

				System.out.println("\n--> Received message was:\n" + receivedMessage);

				// Add delay of 5 seconds (to see how the messages are added/deleted on the queue)
				try {
					Thread.sleep(5000);
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}

				recordSuccess();
			} catch (JMSException jmsex) {
				recordFailure(jmsex);
			}
		}

	}

	/**
	 * Record this run as successful.
	 */
	private static void recordSuccess() {
		System.out.println("SUCCESS");
		status = 0;
		return;
	}

	/**
	 * Record this run as failure.
	 *
	 * @param ex
	 */
	private static void recordFailure(Exception ex) {
		if (ex != null) {
			if (ex instanceof JMSException) {
				processJMSException((JMSException) ex);
			} else {
				System.out.println(ex);
			}
		}
		System.out.println("FAILURE");
		status = -1;
		return;
	}

	/**
	 * Process a JMSException and any associated inner exceptions.
	 *
	 * @param jmsex
	 */
	private static void processJMSException(JMSException jmsex) {
		System.out.println(jmsex);
		Throwable innerException = jmsex.getLinkedException();
		if (innerException != null) {
			System.out.println("Inner exception(s):");
		}
		while (innerException != null) {
			System.out.println(innerException);
			innerException = innerException.getCause();
		}
		return;
	}

}