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

package com.vmware.mangle.services.config;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.classmate.TypeResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriTemplate;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.OperationBuilderPlugin;
import springfox.documentation.spi.service.contexts.OperationContext;

/**
 * Insert your comment for DirtyFixConfig here
 *
 * @author kumargautam
 */
@Configuration
@ConditionalOnProperty(name = "dirty.fix.enabled", havingValue = "true")
public class DirtyFixConfig {

    /**
     * Because of the new Actuator implementation in Spring Boot 2, all actuator endpoints are now
     * dynamically mapped to a single handler method:
     * {@link org.springframework.boot.actuate.endpoint.web.servlet.AbstractWebMvcEndpointHandlerMapping.OperationHandler#handle(javax.servlet.http.HttpServletRequest, java.util.Map)}
     *
     * This causes 2 issues: - Because the handler method has an @RequestBody annotated 'body'
     * parameter, this parameter appears in all actuator endpoints as body parameter, even for GET
     * and HEAD requests (which cannot have a request body). These endpoints cannot be executed from
     * the Swagger UI page. - If an Actuator endpoint contains path parameters, these are not
     * available as input fields on the Swagger UI page, because no @PathParam annotated arguments
     * are present on the handler method.
     *
     * The Swagger OperationBuilderPlugin below fixes these issues in a somewhat dirty, but
     * effective way.
     */
    @Bean
    public OperationBuilderPlugin operationBuilderPluginForCorrectingActuatorEndpoints(
            final TypeResolver typeResolver) {
        return new OperationBuilderPluginForCorrectingActuatorEndpoints(typeResolver);
    }

    private static class OperationBuilderPluginForCorrectingActuatorEndpoints implements OperationBuilderPlugin {

        private final TypeResolver typeResolver;

        OperationBuilderPluginForCorrectingActuatorEndpoints(final TypeResolver typeResolver) {
            this.typeResolver = typeResolver;
        }

        @Override
        public void apply(final OperationContext context) {
            removeBodyParametersForReadMethods(context);
            addOperationParametersForPathParams(context);
        }

        @Override
        public boolean supports(final DocumentationType delimiter) {
            return true;
        }

        private void removeBodyParametersForReadMethods(final OperationContext context) {
            if (HttpMethod.GET.equals(context.httpMethod()) || HttpMethod.HEAD.equals(context.httpMethod())) {
                final List<Parameter> parameters = getParameters(context);
                parameters.removeIf(param -> "body".equals(param.getName()));
            }
        }

        private void addOperationParametersForPathParams(final OperationContext context) {
            final UriTemplate uriTemplate = new UriTemplate(context.requestMappingPattern());

            final List<Parameter> pathParams =
                    uriTemplate.getVariableNames().stream().map(this::createPathParameter).collect(Collectors.toList());

            context.operationBuilder().parameters(pathParams);
        }

        private Parameter createPathParameter(final String pathParam) {
            return new ParameterBuilder().name(pathParam).description(pathParam).required(true)
                    .modelRef(new ModelRef("string")).type(typeResolver.resolve(String.class)).parameterType("path")
                    .build();
        }

        @SuppressWarnings("unchecked")
        private List<Parameter> getParameters(final OperationContext context) {
            final OperationBuilder operationBuilder = context.operationBuilder();
            try {
                Field paramField = OperationBuilder.class.getDeclaredField("parameters");
                paramField.setAccessible(true);
                return (List<Parameter>) paramField.get(operationBuilder);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Unable to modify parameter field!", e);
            }
        }
    }
}
