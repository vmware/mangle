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

package com.vmware.mangle.cassandra.model.slack;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

import com.vmware.mangle.model.constants.Constants;
import com.vmware.mangle.model.enums.EncryptField;

/**
 * SlackInfo model.
 *
 * @author kumargautam
 */
@UserDefinedType("SlackInfo")
@Data
public class SlackInfo implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(position = 0, value = "Provide OAuth token of Slack which will be used to get connection from slack")
    @NotEmpty
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @EncryptField
    private String token;

    @ApiModelProperty(position = 1, value = "Provide list of slack channels for sending notification")
    @NotEmpty
    @UniqueElements
    private List<String> channels;

    @ApiModelProperty(position = 2, value = "Provide Sender name as User or App name of slack", example = "Mangle")
    @JsonProperty(required = false)
    private String senderName = Constants.DEFAULT_SENDER_NAME;

}
