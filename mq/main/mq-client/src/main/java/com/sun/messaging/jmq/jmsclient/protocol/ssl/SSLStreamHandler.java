/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)SSLStreamHandler.java	1.16 06/27/07
 */ 

package com.sun.messaging.jmq.jmsclient.protocol.ssl;

import java.io.*;
import javax.jms.*;

import com.sun.messaging.PropertyOwner;
import com.sun.messaging.AdministeredObject;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.jmsclient.*;

 /**
  * This class is the SSL protocol handler for the iMQ JMS client
  * implementation.  It uses SSL protocol to communicate with the Broker.
  */
public class SSLStreamHandler implements StreamHandler, PropertyOwner {

    /**
     * Null constructor for use by AdministeredObject when used as a PropertyOwner
     */  
    public SSLStreamHandler() {}

    public String[] getPropertyNames() {
        String [] propnames = new String [6];
        propnames[0] = ConnectionConfiguration.imqBrokerHostName;
        propnames[1] = ConnectionConfiguration.imqBrokerHostPort;
        propnames[2] = ConnectionConfiguration.imqBrokerServicePort;
        propnames[3] = ConnectionConfiguration.imqBrokerServiceName;
        propnames[4] = ConnectionConfiguration.imqSSLProviderClassname;
        propnames[5] = ConnectionConfiguration.imqSSLIsHostTrusted;
        return propnames;
    }

    public String getPropertyType(String propname) {
        if (ConnectionConfiguration.imqBrokerHostName.equals(propname) ||
            ConnectionConfiguration.imqBrokerServiceName.equals(propname) || 
            ConnectionConfiguration.imqSSLProviderClassname.equals(propname)) {
            return AdministeredObject.AO_PROPERTY_TYPE_STRING;
        } else {
            if (ConnectionConfiguration.imqBrokerHostPort.equals(propname)) {
                return AdministeredObject.AO_PROPERTY_TYPE_INTEGER;
            } else {
                if (ConnectionConfiguration.imqSSLIsHostTrusted.equals(propname)) {
                    return AdministeredObject.AO_PROPERTY_TYPE_BOOLEAN;
                }
            }    
        }
        return null;
    }

    public String getPropertyLabel(String propname) {
        if (ConnectionConfiguration.imqBrokerHostName.equals(propname)) {
            return (AdministeredObject.cr.L_JMQBROKER_HOST_NAME);
        } else {
            if (ConnectionConfiguration.imqBrokerHostPort.equals(propname)) {
                return (AdministeredObject.cr.L_JMQBROKER_HOST_PORT);
            } else {
                if (ConnectionConfiguration.imqSSLProviderClassname.equals(propname)) {
                    return (AdministeredObject.cr.L_JMQSSL_PROVIDER_CLASSNAME);
                } else {
                    if (ConnectionConfiguration.imqSSLIsHostTrusted.equals(propname)) {
                        return (AdministeredObject.cr.L_JMQSSL_IS_HOST_TRUSTED);
                    } else {
                        if (ConnectionConfiguration.imqBrokerServicePort.equals(propname)) {
                            return (AdministeredObject.cr.L_JMQBROKER_SERVICE_PORT);
                        } else {
                            if (ConnectionConfiguration.imqBrokerServiceName.equals(propname)) {
                                return (AdministeredObject.cr.L_JMQBROKER_SERVICE_NAME);
                            }
                        }
                    }
                }
            }    
        }
        return null;
    }

    public String getPropertyDefault(String propname) {
        if (ConnectionConfiguration.imqBrokerHostName.equals(propname)) {
            return "localhost";
        } else {
            if (ConnectionConfiguration.imqBrokerHostPort.equals(propname)) {
                return ("7676");
            } else {
                if (ConnectionConfiguration.imqSSLProviderClassname.equals(propname)) {
                    return "com.sun.net.ssl.internal.ssl.Provider";
                } else {
                    if (ConnectionConfiguration.imqSSLIsHostTrusted.equals(propname)) {
                        return "false"; //TCR#3 default to false
                    } else {
                        if (ConnectionConfiguration.imqBrokerServicePort.equals(propname)) {
                            return ("0"); 
                        } else { 
                            if (ConnectionConfiguration.imqBrokerServiceName.equals(propname)) { 
                                return ("");
                            } 
                        } 
                    }
                }
            }
        }
        return null;
    }    

    /**
     * Open socket a new connection.
     *
     * @param connection is the ConnectionImpl object.
     * @return a new instance of SSLConnectionHandler.
     * @exception throws IOException if socket creation failed.
     */
    public ConnectionHandler
    openConnection(Object connection) throws JMSException {
        return new SSLConnectionHandler(connection);
    }

    public ConnectionHandler openConnection(
        MQAddress addr, ConnectionImpl connection) throws JMSException {
        return new SSLConnectionHandler(addr, connection);
    }
}
