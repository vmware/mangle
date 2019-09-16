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

package com.vmware.mangle.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 *
 *
 * @author chetanc
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MangleResponseResource<T> extends ResourceSupport {
    T response;

    public MangleResponseResource(T object) {
        this.response = object;
        add(new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withRel("self"));
    }

    public MangleResponseResource(T object, List<Link> links) {
        this.response = object;
        add(new Link(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri().toASCIIString())
                .withRel("self"));
        add(links);
    }
}
