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

package org.jboss.byteman.agent.submit;

/**
 * storage for a script file name and the corresponding script text
 */
public class ScriptText
{
    private String fileName;
    private String text;

    public ScriptText(String fileName, String text)
    {
        this.fileName = fileName;
        this.text = text;
    }

    public ScriptText(String text)
    {
        this.fileName = "";
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public String getText() {
        return text;
    }
}
