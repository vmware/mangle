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

package com.vmware.mangle.services.mockdata;

import java.io.File;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;

/**
 * MockData for Plugin test.
 *
 * @author kumargautam
 */
@Log4j2
public class PluginMockData {

    public void getFileSystemResource(LinkedMultiValueMap<String, Object> multiPartMap, String pathname,
            String filename) {
        File file = new File(pathname);
        log.info("File path : {}", file.getAbsolutePath());
        if (file.exists()) {
            multiPartMap.add(filename, new FileSystemResource(file));
        }
    }
}
