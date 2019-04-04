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

package com.vmware.mangle.services;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dozer.DozerBeanMapper;
import org.springframework.stereotype.Service;

/**
 *
 *
 * @author chetanc
 */
@Service
@SuppressWarnings("unchecked")
public class MappingService {
    private DozerBeanMapper mapper;

    public MappingService() {
        mapper = new DozerBeanMapper();
    }

    public <T> T map(Object source, Class<T> target) {
        return mapper.map(source, target);
    }

    public <T> void map(Object source, T target) {
        mapper.map(source, target);
    }

    public <T, O> List<O> map(List<T> objects, Class<O> target) {
        return (List<O>) internalMap(objects, target, (List<O>) Lists.newArrayList());
    }

    public <T, O> List<O> map(List<T> objects, Class<O> target, List<O> destination) {
        return (List<O>) internalMap(objects, target, destination);
    }

    public <T, O> Set<O> map(Set<T> objects, Class<O> target, Set<O> destination) {
        return (Set<O>) internalMap(objects, target, destination);
    }

    public <T, O> Set<O> map(Set<T> objects, Class<O> target) {
        return map(objects, target, (Set<O>) Sets.newHashSet());
    }

    private <T, O> Collection<O> internalMap(Collection<T> objects, Class<O> target, Collection<O> destination) {
        for (T t : objects) {
            destination.add(mapper.map(t, target));
        }
        return destination;
    }
}
