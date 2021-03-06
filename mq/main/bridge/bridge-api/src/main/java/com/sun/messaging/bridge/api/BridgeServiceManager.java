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

package com.sun.messaging.bridge.api;

import java.util.Locale;
import java.util.Properties;
import org.jvnet.hk2.annotations.Contract;
import javax.inject.Singleton;
import com.sun.messaging.bridge.api.Bridge;

/**
 * Bridge Services Manager interface
 *
 * @author amyk
 */
@Contract
@Singleton
public abstract class BridgeServiceManager 
{

    /**
     * Initialize Bridge Service Manager
     */
    public abstract void init(BridgeBaseContext ctx) throws Exception; 

    /**
     * Start Bridge Service Manager
     */
    public abstract void start() throws Exception; 

    /**
     * Stop Bridge Service Manager 
     */
    public abstract void stop() throws Exception;

   /**
    */
    public abstract BridgeBaseContext getBridgeBaseContext();

    /**
     * @return true if the bridge service manager is running
     */
    public abstract boolean isRunning();

    /**
     *
     */
    public abstract String getAdminDestinationName() throws Exception;
    public abstract String getAdminDestinationClassName() throws Exception;

    public static Object getExportedService(Class c, String bridgeType, Properties props) throws Exception {
        if (c == null) throw new IllegalArgumentException("null class");
        if (bridgeType == null) throw new IllegalArgumentException("null bridge type");

        Bridge b = null;
        Locale loc = Locale.getDefault();
        if (bridgeType.equalsIgnoreCase(Bridge.JMS_TYPE)) {
            b = (Bridge)Class.forName("com.sun.messaging.bridge.service.jms.BridgeImpl").newInstance();
        } else if (bridgeType.toUpperCase(loc).equals(Bridge.STOMP_TYPE)) {
            b = (Bridge)Class.forName("com.sun.messaging.bridge.service.stomp.StompBridge").newInstance();
        } else {
            throw new IllegalArgumentException("Invalid bridge type: "+bridgeType);
        }

        return  b.getExportedService(c, props);
    }

}
