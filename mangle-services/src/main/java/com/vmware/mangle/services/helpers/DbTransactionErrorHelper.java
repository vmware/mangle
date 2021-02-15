/*
 * Copyright (c) 2016-2019 VMware, Inc. All Rights Reserved.
 *
 * This product is licensed to you under the Apache License, Version 2.0 (the "License").
 * You may not use this product except in compliance with the License.
 *
 * This product may include a number of subcomponents with separate copyright notices
 * and license terms. Your use of these subcomponents is subject to the terms and
 * conditions of the subcomponent's license, as noted in the LICENSE file.
 */

package com.vmware.mangle.services.helpers;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

import com.vmware.mangle.model.enums.DatabaseType;

/**
 *
 * @author kumargautam
 */
@UtilityClass
public class DbTransactionErrorHelper {

    private final Map<String, String> pgDbTransactionErrorCodes = new LinkedHashMap<>();

    static {
        pgDbTransactionErrorCodes.put("invalid_transaction_state", "25000");
        pgDbTransactionErrorCodes.put("active_sql_transaction", "25001");
        pgDbTransactionErrorCodes.put("branch_transaction_already_active", "25002");
        pgDbTransactionErrorCodes.put("no_active_sql_transaction_for_branch", "25005");
        pgDbTransactionErrorCodes.put("read_only_sql_transaction", "25006");
        pgDbTransactionErrorCodes.put("no_active_sql_transaction", "25P01");
        pgDbTransactionErrorCodes.put("in_failed_sql_transaction", "25P02");
        pgDbTransactionErrorCodes.put("idle_in_transaction_session_timeout", "25P03");
    }

    public Map<String, String> getDbTransactionErrorCodes(DatabaseType databaseType) {
        if (DatabaseType.POSTGRES.equals(databaseType)) {
            return pgDbTransactionErrorCodes;
        }
        return Collections.emptyMap();
    }
}
