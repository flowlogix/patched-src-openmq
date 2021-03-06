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
 * @(#)MessageConsumerTable.hpp	1.5 06/26/07
 */ 

#ifndef MESSAGECONSUMERTABLE_HPP
#define MESSAGECONSUMERTABLE_HPP

#include "../containers/BasicTypeHashtable.hpp"
#include "../basictypes/Monitor.hpp"
#include "../basictypes/Object.hpp"
#include <nspr.h>

class MessageConsumer;

/** 
 * This class maps a consumerID to a MessageConsumer. */ 

class MessageConsumerTable : public Object {
public:
  /**
   *
   */
  enum MessageConsumerOP {
    START_CONSUMER       = 0,
    STOP_CONSUMER        = 1,
    CLOSE_CONSUMER       = 2,
    UNSUBSCRIBE_DURABLE  = 3,
    RECOVER_RECEIVEQUEUE = 4
  };

private:
  /**
   * The hashtable that maps a consumerID to its MessageConsumer 
   */
  BasicTypeHashtable *  table;

  /**
   * Protects table for synchronous access
   */
  Monitor monitor;

public:
  /**
   * Initializes the hashtable so that the hashtable will automatically
   * delete each consumerID on destruction 
   */
  MessageConsumerTable();

  /**
   * Deconstructor that deletes the hashtable.
   */
  virtual ~MessageConsumerTable();
  
  /**
   * Removes the MessageConsumer associated with consumerID from the
   * MessageConsumerTable.
   *  
   * @param consumerID is the consumerID to remove from the table
   * @return MQ_SUCCESS if successful and an error otherwise 
   */
  MQError remove(PRUint64 consumerID, MessageConsumer ** const consumer);

  /**
   * Adds the consumerID to MessageConsumer mapping 
   *
   * @param consumerID is the consumerID to add to the table
   * @param consumer is the MessageConsumer to associate with consumerID
   * @return IMQ_SUCCESS if successful and an error otherwise 
   */
  MQError add(PRUint64 consumerID, MessageConsumer * const consumer);


  /**
   * @param consumerID is the consumerID to add to the table
   * @param consumer is the MessageConsumer to return 
   * @return IMQ_SUCCESS if successful and an error otherwise 
   */
  MQError get(PRUint64 consumerID, MessageConsumer ** const consumer);

  MQError operationAll(MessageConsumerOP op, const void * opData);


//
// Avoid all implicit shallow copies.  Without these, the compiler
// will automatically define implementations for us.
//
private:
  //
  // These are not supported and are not implemented
  //
  MessageConsumerTable(const MessageConsumerTable& readQTable);
  MessageConsumerTable& operator=(const MessageConsumerTable& readQTable);
};

#endif // MESSAGECONSUMERTABLE_HPP

