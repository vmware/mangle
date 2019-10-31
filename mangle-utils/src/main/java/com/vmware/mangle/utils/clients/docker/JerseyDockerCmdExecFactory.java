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

package com.vmware.mangle.utils.clients.docker;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.WebTarget;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.dockerjava.api.command.AttachContainerCmd;
import com.github.dockerjava.api.command.AuthCmd;
import com.github.dockerjava.api.command.BuildImageCmd;
import com.github.dockerjava.api.command.CommitCmd;
import com.github.dockerjava.api.command.ConnectToNetworkCmd;
import com.github.dockerjava.api.command.ContainerDiffCmd;
import com.github.dockerjava.api.command.CopyArchiveFromContainerCmd;
import com.github.dockerjava.api.command.CopyArchiveToContainerCmd;
import com.github.dockerjava.api.command.CopyFileFromContainerCmd;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateImageCmd;
import com.github.dockerjava.api.command.CreateNetworkCmd;
import com.github.dockerjava.api.command.CreateVolumeCmd;
import com.github.dockerjava.api.command.DisconnectFromNetworkCmd;
import com.github.dockerjava.api.command.DockerCmdExecFactory;
import com.github.dockerjava.api.command.EventsCmd;
import com.github.dockerjava.api.command.ExecCreateCmd;
import com.github.dockerjava.api.command.ExecStartCmd;
import com.github.dockerjava.api.command.InfoCmd;
import com.github.dockerjava.api.command.InitializeSwarmCmd.Exec;
import com.github.dockerjava.api.command.InspectContainerCmd;
import com.github.dockerjava.api.command.InspectExecCmd;
import com.github.dockerjava.api.command.InspectImageCmd;
import com.github.dockerjava.api.command.InspectNetworkCmd;
import com.github.dockerjava.api.command.InspectVolumeCmd;
import com.github.dockerjava.api.command.KillContainerCmd;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.ListNetworksCmd;
import com.github.dockerjava.api.command.ListVolumesCmd;
import com.github.dockerjava.api.command.LoadImageCmd;
import com.github.dockerjava.api.command.LogContainerCmd;
import com.github.dockerjava.api.command.PauseContainerCmd;
import com.github.dockerjava.api.command.PingCmd;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PushImageCmd;
import com.github.dockerjava.api.command.RemoveContainerCmd;
import com.github.dockerjava.api.command.RemoveImageCmd;
import com.github.dockerjava.api.command.RemoveNetworkCmd;
import com.github.dockerjava.api.command.RemoveVolumeCmd;
import com.github.dockerjava.api.command.RenameContainerCmd;
import com.github.dockerjava.api.command.RestartContainerCmd;
import com.github.dockerjava.api.command.SaveImageCmd;
import com.github.dockerjava.api.command.SearchImagesCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.command.StopContainerCmd;
import com.github.dockerjava.api.command.TagImageCmd;
import com.github.dockerjava.api.command.TopContainerCmd;
import com.github.dockerjava.api.command.UnpauseContainerCmd;
import com.github.dockerjava.api.command.UpdateContainerCmd;
import com.github.dockerjava.api.command.VersionCmd;
import com.github.dockerjava.api.command.WaitContainerCmd;
import com.github.dockerjava.api.exception.DockerClientException;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.SSLConfig;
import com.github.dockerjava.jaxrs.AttachContainerCmdExec;
import com.github.dockerjava.jaxrs.AuthCmdExec;
import com.github.dockerjava.jaxrs.BuildImageCmdExec;
import com.github.dockerjava.jaxrs.CommitCmdExec;
import com.github.dockerjava.jaxrs.ConnectToNetworkCmdExec;
import com.github.dockerjava.jaxrs.ContainerDiffCmdExec;
import com.github.dockerjava.jaxrs.CopyArchiveFromContainerCmdExec;
import com.github.dockerjava.jaxrs.CopyArchiveToContainerCmdExec;
import com.github.dockerjava.jaxrs.CopyFileFromContainerCmdExec;
import com.github.dockerjava.jaxrs.CreateContainerCmdExec;
import com.github.dockerjava.jaxrs.CreateImageCmdExec;
import com.github.dockerjava.jaxrs.CreateNetworkCmdExec;
import com.github.dockerjava.jaxrs.CreateVolumeCmdExec;
import com.github.dockerjava.jaxrs.DisconnectFromNetworkCmdExec;
import com.github.dockerjava.jaxrs.EventsCmdExec;
import com.github.dockerjava.jaxrs.ExecCreateCmdExec;
import com.github.dockerjava.jaxrs.ExecStartCmdExec;
import com.github.dockerjava.jaxrs.InfoCmdExec;
import com.github.dockerjava.jaxrs.InspectContainerCmdExec;
import com.github.dockerjava.jaxrs.InspectExecCmdExec;
import com.github.dockerjava.jaxrs.InspectImageCmdExec;
import com.github.dockerjava.jaxrs.InspectNetworkCmdExec;
import com.github.dockerjava.jaxrs.InspectVolumeCmdExec;
import com.github.dockerjava.jaxrs.KillContainerCmdExec;
import com.github.dockerjava.jaxrs.ListContainersCmdExec;
import com.github.dockerjava.jaxrs.ListImagesCmdExec;
import com.github.dockerjava.jaxrs.ListNetworksCmdExec;
import com.github.dockerjava.jaxrs.ListVolumesCmdExec;
import com.github.dockerjava.jaxrs.LoadImageCmdExec;
import com.github.dockerjava.jaxrs.LogContainerCmdExec;
import com.github.dockerjava.jaxrs.PauseContainerCmdExec;
import com.github.dockerjava.jaxrs.PingCmdExec;
import com.github.dockerjava.jaxrs.PullImageCmdExec;
import com.github.dockerjava.jaxrs.PushImageCmdExec;
import com.github.dockerjava.jaxrs.RemoveContainerCmdExec;
import com.github.dockerjava.jaxrs.RemoveImageCmdExec;
import com.github.dockerjava.jaxrs.RemoveNetworkCmdExec;
import com.github.dockerjava.jaxrs.RemoveVolumeCmdExec;
import com.github.dockerjava.jaxrs.RenameContainerCmdExec;
import com.github.dockerjava.jaxrs.RestartContainerCmdExec;
import com.github.dockerjava.jaxrs.SaveImageCmdExec;
import com.github.dockerjava.jaxrs.SearchImagesCmdExec;
import com.github.dockerjava.jaxrs.StartContainerCmdExec;
import com.github.dockerjava.jaxrs.StatsCmdExec;
import com.github.dockerjava.jaxrs.StopContainerCmdExec;
import com.github.dockerjava.jaxrs.TagImageCmdExec;
import com.github.dockerjava.jaxrs.TopContainerCmdExec;
import com.github.dockerjava.jaxrs.UnixConnectionSocketFactory;
import com.github.dockerjava.jaxrs.UnpauseContainerCmdExec;
import com.github.dockerjava.jaxrs.UpdateContainerCmdExec;
import com.github.dockerjava.jaxrs.VersionCmdExec;
import com.github.dockerjava.jaxrs.WaitContainerCmdExec;
import com.github.dockerjava.jaxrs.filter.JsonClientFilter;
import com.github.dockerjava.jaxrs.filter.ResponseStatusExceptionFilter;
import com.github.dockerjava.jaxrs.filter.SelectiveLoggingFilter;
import lombok.extern.log4j.Log4j2;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.LoggerFactory;

import com.vmware.mangle.utils.exceptions.MangleRuntimeException;
import com.vmware.mangle.utils.exceptions.handler.ErrorCode;

/**
 * @author bkaranam
 *
 * import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
 * see https://github.com/docker-java/docker-java/issues/196
 */
@Log4j2
public class JerseyDockerCmdExecFactory implements DockerCmdExecFactory {

    private Client client;

    private WebTarget baseResource;

    private Integer readTimeout = null;

    private Integer connectTimeout = null;

    private Integer maxTotalConnections = null;

    private Integer maxPerRouteConnections = null;

    private Integer connectionRequestTimeout = null;

    private ClientRequestFilter[] clientRequestFilters = null;

    private ClientResponseFilter[] clientResponseFilters = null;

    private DockerClientConfig dockerClientConfig;

    private PoolingHttpClientConnectionManager connManager = null;

    private static final String NOT_SUPPORTING_DOCKER_SWARM_MESSAGE =
            "Currently mangle doesn't support docker swarm, hence not implementing method: ";

    @Override
    public void init(DockerClientConfig dockerClientConfig) {
        checkNotNull(dockerClientConfig, "config was not specified");
        this.dockerClientConfig = dockerClientConfig;

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.connectorProvider(new ApacheConnectorProvider());
        clientConfig.property(CommonProperties.FEATURE_AUTO_DISCOVERY_DISABLE, true);

        clientConfig.register(ResponseStatusExceptionFilter.class);
        clientConfig.register(JsonClientFilter.class);
        clientConfig.register(JacksonJsonProvider.class);
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();
        // logging may disabled via log level
        clientConfig
                .register(new SelectiveLoggingFilter(LoggerFactory.getLogger(JerseyDockerCmdExecFactory.class), true));

        if (readTimeout != null) {
            requestConfigBuilder.setSocketTimeout(readTimeout);
            clientConfig.property(ClientProperties.READ_TIMEOUT, readTimeout);
        }

        if (connectTimeout != null) {
            requestConfigBuilder.setConnectTimeout(connectTimeout);
            clientConfig.property(ClientProperties.CONNECT_TIMEOUT, connectTimeout);
        }

        if (clientResponseFilters != null) {
            for (ClientResponseFilter clientResponseFilter : clientResponseFilters) {
                if (clientResponseFilter != null) {
                    clientConfig.register(clientResponseFilter);
                }
            }
        }

        if (clientRequestFilters != null) {
            for (ClientRequestFilter clientRequestFilter : clientRequestFilters) {
                if (clientRequestFilter != null) {
                    clientConfig.register(clientRequestFilter);
                }
            }
        }

        URI originalUri = dockerClientConfig.getDockerHost();

        String protocol = null;

        SSLContext sslContext = null;

        try {
            final SSLConfig sslConfig = dockerClientConfig.getSSLConfig();
            if (sslConfig != null) {
                sslContext = sslConfig.getSSLContext();
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw new DockerClientException("Error in SSL Configuration", ex);
        }

        if (sslContext != null) {
            protocol = "https";
        } else {
            protocol = "http";
        }

        if (!originalUri.getScheme().equals("unix")) {

            try {
                originalUri = new URI(originalUri.toString().replaceFirst("tcp", protocol));
            } catch (URISyntaxException e) {
                throw new MangleRuntimeException(e.getMessage(), e, ErrorCode.GENERIC_ERROR, (Object) null);
            }

            configureProxy(clientConfig, originalUri, protocol);
        }

        connManager = new PoolingHttpClientConnectionManager(getSchemeRegistry(originalUri, sslContext)) {

            @Override
            public void close() {
                super.shutdown();
            }

            @Override
            public void shutdown() {
                // Disable shutdown of the pool. This will be done later, when this factory is closed
                // This is a workaround for finalize method on jerseys ClientRuntime which
                // closes the client and shuts down the connection pool when it is garbage collected
            }
        };

        if (maxTotalConnections != null) {
            connManager.setMaxTotal(maxTotalConnections);
        }
        if (maxPerRouteConnections != null) {
            connManager.setDefaultMaxPerRoute(maxPerRouteConnections);
        }

        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connManager);

        // Configure connection pool timeout
        if (connectionRequestTimeout != null) {
            requestConfigBuilder.setConnectionRequestTimeout(connectionRequestTimeout);
        }
        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, requestConfigBuilder.build());
        ClientBuilder clientBuilder = ClientBuilder.newBuilder().withConfig(clientConfig);

        if (sslContext != null) {
            clientBuilder.sslContext(sslContext);
        }

        client = clientBuilder.build();

        baseResource = client.target(sanitizeUrl(originalUri).toString())
                .path(dockerClientConfig.getApiVersion().asWebPathPart());
    }

    private URI sanitizeUrl(URI originalUri) {
        if (originalUri.getScheme().equals("unix")) {
            return UnixConnectionSocketFactory.sanitizeUri(originalUri);
        }
        return originalUri;
    }

    private void configureProxy(ClientConfig clientConfig, URI originalUri, String protocol) {

        List<Proxy> proxies = ProxySelector.getDefault().select(originalUri);

        for (Proxy proxy : proxies) {
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            if (address != null) {
                String hostname = address.getHostName();
                int port = address.getPort();

                clientConfig.property(ClientProperties.PROXY_URI, "http://" + hostname + ":" + port);

                String httpProxyUser = System.getProperty(protocol + ".proxyUser");
                if (httpProxyUser != null) {
                    clientConfig.property(ClientProperties.PROXY_USERNAME, httpProxyUser);
                    String httpProxyPassword = System.getProperty(protocol + ".proxyPassword");
                    if (httpProxyPassword != null) {
                        clientConfig.property(ClientProperties.PROXY_PASSWORD, httpProxyPassword);
                    }
                }
            }
        }
    }

    private org.apache.http.config.Registry<ConnectionSocketFactory> getSchemeRegistry(final URI originalUri,
            SSLContext sslContext) {
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.create();
        registryBuilder.register("http", PlainConnectionSocketFactory.getSocketFactory());
        if (sslContext != null) {
            registryBuilder.register("https", new SSLConnectionSocketFactory(sslContext, (arg0, arg1) -> true));
        }
        registryBuilder.register("unix", new UnixConnectionSocketFactory(originalUri));
        return registryBuilder.build();
    }

    protected WebTarget getBaseResource() {
        checkNotNull(baseResource,
                "Factory not initialized, baseResource not set. You probably forgot to call init()!");
        return baseResource;
    }

    protected DockerClientConfig getDockerClientConfig() {
        checkNotNull(dockerClientConfig,
                "Factor not initialized, dockerClientConfig not set. You probably forgot to call init()!");
        return dockerClientConfig;
    }

    @Override
    public AuthCmd.Exec createAuthCmdExec() {
        return new AuthCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public InfoCmd.Exec createInfoCmdExec() {
        return new InfoCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public PingCmd.Exec createPingCmdExec() {
        return new PingCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public VersionCmd.Exec createVersionCmdExec() {
        return new VersionCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public PullImageCmd.Exec createPullImageCmdExec() {
        return new PullImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public PushImageCmd.Exec createPushImageCmdExec() {
        return new PushImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public SaveImageCmd.Exec createSaveImageCmdExec() {
        return new SaveImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CreateImageCmd.Exec createCreateImageCmdExec() {
        return new CreateImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public LoadImageCmd.Exec createLoadImageCmdExec() {
        return new LoadImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public SearchImagesCmd.Exec createSearchImagesCmdExec() {
        return new SearchImagesCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public RemoveImageCmd.Exec createRemoveImageCmdExec() {
        return new RemoveImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ListImagesCmd.Exec createListImagesCmdExec() {
        return new ListImagesCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public InspectImageCmd.Exec createInspectImageCmdExec() {
        return new InspectImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ListContainersCmd.Exec createListContainersCmdExec() {
        return new ListContainersCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CreateContainerCmd.Exec createCreateContainerCmdExec() {
        return new CreateContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public StartContainerCmd.Exec createStartContainerCmdExec() {
        return new StartContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public InspectContainerCmd.Exec createInspectContainerCmdExec() {
        return new InspectContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ExecCreateCmd.Exec createExecCmdExec() {
        return new ExecCreateCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public RemoveContainerCmd.Exec createRemoveContainerCmdExec() {
        return new RemoveContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public WaitContainerCmd.Exec createWaitContainerCmdExec() {
        return new WaitContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public AttachContainerCmd.Exec createAttachContainerCmdExec() {
        return new AttachContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ExecStartCmd.Exec createExecStartCmdExec() {
        return new ExecStartCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public InspectExecCmd.Exec createInspectExecCmdExec() {
        return new InspectExecCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public LogContainerCmd.Exec createLogContainerCmdExec() {
        return new LogContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CopyArchiveFromContainerCmd.Exec createCopyArchiveFromContainerCmdExec() {
        return new CopyArchiveFromContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CopyFileFromContainerCmd.Exec createCopyFileFromContainerCmdExec() {
        return new CopyFileFromContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CopyArchiveToContainerCmd.Exec createCopyArchiveToContainerCmdExec() {
        return new CopyArchiveToContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public StopContainerCmd.Exec createStopContainerCmdExec() {
        return new StopContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ContainerDiffCmd.Exec createContainerDiffCmdExec() {
        return new ContainerDiffCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public KillContainerCmd.Exec createKillContainerCmdExec() {
        return new KillContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public UpdateContainerCmd.Exec createUpdateContainerCmdExec() {
        return new UpdateContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public RenameContainerCmd.Exec createRenameContainerCmdExec() {
        return new RenameContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public RestartContainerCmd.Exec createRestartContainerCmdExec() {
        return new RestartContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CommitCmd.Exec createCommitCmdExec() {
        return new CommitCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public BuildImageCmd.Exec createBuildImageCmdExec() {
        return new BuildImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public TopContainerCmd.Exec createTopContainerCmdExec() {
        return new TopContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public TagImageCmd.Exec createTagImageCmdExec() {
        return new TagImageCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public PauseContainerCmd.Exec createPauseContainerCmdExec() {
        return new PauseContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public UnpauseContainerCmd.Exec createUnpauseContainerCmdExec() {
        return new UnpauseContainerCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public EventsCmd.Exec createEventsCmdExec() {
        return new EventsCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public StatsCmd.Exec createStatsCmdExec() {
        return new StatsCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CreateVolumeCmd.Exec createCreateVolumeCmdExec() {
        return new CreateVolumeCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public InspectVolumeCmd.Exec createInspectVolumeCmdExec() {
        return new InspectVolumeCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public RemoveVolumeCmd.Exec createRemoveVolumeCmdExec() {
        return new RemoveVolumeCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ListVolumesCmd.Exec createListVolumesCmdExec() {
        return new ListVolumesCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ListNetworksCmd.Exec createListNetworksCmdExec() {
        return new ListNetworksCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public InspectNetworkCmd.Exec createInspectNetworkCmdExec() {

        return new InspectNetworkCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public CreateNetworkCmd.Exec createCreateNetworkCmdExec() {

        return new CreateNetworkCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public RemoveNetworkCmd.Exec createRemoveNetworkCmdExec() {

        return new RemoveNetworkCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public ConnectToNetworkCmd.Exec createConnectToNetworkCmdExec() {

        return new ConnectToNetworkCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public DisconnectFromNetworkCmd.Exec createDisconnectFromNetworkCmdExec() {

        return new DisconnectFromNetworkCmdExec(getBaseResource(), getDockerClientConfig());
    }

    @Override
    public void close() throws IOException {
        checkNotNull(client, "Factory not initialized. You probably forgot to call init()!");
        client.close();
        connManager.close();
    }

    public JerseyDockerCmdExecFactory withReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public JerseyDockerCmdExecFactory withConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public JerseyDockerCmdExecFactory withMaxTotalConnections(Integer maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
        return this;
    }

    public JerseyDockerCmdExecFactory withMaxPerRouteConnections(Integer maxPerRouteConnections) {
        this.maxPerRouteConnections = maxPerRouteConnections;
        return this;
    }

    public JerseyDockerCmdExecFactory withConnectionRequestTimeout(Integer connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
        return this;
    }

    public JerseyDockerCmdExecFactory withClientResponseFilters(ClientResponseFilter... clientResponseFilter) {
        this.clientResponseFilters = clientResponseFilter;
        return this;
    }

    public JerseyDockerCmdExecFactory withClientRequestFilters(ClientRequestFilter... clientRequestFilters) {
        this.clientRequestFilters = clientRequestFilters;
        return this;
    }


    /**
     * release connections from the pool
     *
     * @param idleSeconds
     *            idle seconds, longer than the configured value will be evicted
     */
    public void releaseConnection(long idleSeconds) {
        this.connManager.closeExpiredConnections();
        this.connManager.closeIdleConnections(idleSeconds, TimeUnit.SECONDS);
    }

    @Override
    public Exec createInitializeSwarmCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createInitializeSwarmCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.InspectSwarmCmd.Exec createInspectSwarmCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createInspectSwarmCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.JoinSwarmCmd.Exec createJoinSwarmCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createJoinSwarmCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.LeaveSwarmCmd.Exec createLeaveSwarmCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createLeaveSwarmCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.UpdateSwarmCmd.Exec createUpdateSwarmCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createUpdateSwarmCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.ListServicesCmd.Exec createListServicesCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createListServicesCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.CreateServiceCmd.Exec createCreateServiceCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createCreateServiceCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.InspectServiceCmd.Exec createInspectServiceCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createInspectServiceCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.UpdateServiceCmd.Exec createUpdateServiceCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createUpdateServiceCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.RemoveServiceCmd.Exec createRemoveServiceCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "createRemoveServiceCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.LogSwarmObjectCmd.Exec logSwarmObjectExec(String endpoint) {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "logSwarmObjectExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.ListSwarmNodesCmd.Exec listSwarmNodeCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "listSwarmNodeCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.InspectSwarmNodeCmd.Exec inspectSwarmNodeCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "inspectSwarmNodeCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.RemoveSwarmNodeCmd.Exec removeSwarmNodeCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "removeSwarmNodeCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.UpdateSwarmNodeCmd.Exec updateSwarmNodeCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "updateSwarmNodeCmdExec");
        return null;
    }

    @Override
    public com.github.dockerjava.api.command.ListTasksCmd.Exec listTasksCmdExec() {
        log.info(NOT_SUPPORTING_DOCKER_SWARM_MESSAGE + "listTasksCmdExec");
        return null;
    }
}
