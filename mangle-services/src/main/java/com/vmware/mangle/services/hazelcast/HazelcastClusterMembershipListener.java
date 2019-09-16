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

package com.vmware.mangle.services.hazelcast;


import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE;
import static com.vmware.mangle.utils.constants.HazelcastConstants.HAZELCAST_NODE_TASKS_MAP;
import static com.vmware.mangle.utils.constants.URLConstants.TASKS_WAIT_TIME_SECONDS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.HazelcastInstanceAware;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;
import com.hazelcast.core.PartitionService;
import com.hazelcast.core.ReplicatedMap;
import com.hazelcast.nio.Address;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.vmware.mangle.cassandra.model.faults.specs.TaskSpec;
import com.vmware.mangle.cassandra.model.hazelcast.HazelcastClusterConfig;
import com.vmware.mangle.cassandra.model.tasks.Task;
import com.vmware.mangle.services.ClusterConfigService;
import com.vmware.mangle.services.enums.MangleNodeStatus;
import com.vmware.mangle.services.enums.MangleQuorumStatus;
import com.vmware.mangle.services.tasks.executor.TaskExecutor;
import com.vmware.mangle.utils.CommonUtils;
import com.vmware.mangle.utils.constants.Constants;
import com.vmware.mangle.utils.constants.HazelcastConstants;
import com.vmware.mangle.utils.constants.URLConstants;
import com.vmware.mangle.utils.exceptions.MangleException;

/**
 *
 * i) Fault when injected will follow the following steps of execution 1. Construct the task object
 * 2. Store in the DB 3. Trigger TaskCreatedEvent 4. TaskCreatedListener will add the task to the
 * hazelcast cluster cache 5. Hazelcast will put this task into its distributed-map 6. Depending on
 * which partition this task was added into, it will trigger an entryAdded event in its respective
 * node 7. Listener to this event will trigger the execution of the task on that particular node
 *
 * ii) When new member joins the cluster 1. The partition ownership of some of the partitions are
 * moved to new cluster member 2. No re-triggering of the tasks will be carried out 3. Task that
 * were being executed by the old owner of the partition will remain to be executed on the old owner
 * Summary: only partition ownership will be transferred
 *
 * iii) When an existing cluster member leaves the cluster 1. Membership event is generated - Tasks
 * that were still running on the old owner, but whose ownership were assigned to the new owner will
 * be re-triggered 2. Migration event is generated - Partitions owned by the dead node will be
 * distributed across the existing cluster nodes - Tasks that are part of dead cluster member will
 * be re-triggered, on the new owner of the respective partitions
 *
 *
 * @author chetanc
 * @author bkaranam (bhanukiran karanam)
 *
 */
@Log4j2
@Component
public class HazelcastClusterMembershipListener implements MembershipListener, HazelcastInstanceAware {

    @Autowired
    TaskExecutor<Task<? extends TaskSpec>> taskRunner;
    private HazelcastInstance hz;
    @Autowired
    private HazelcastTaskService hazelcastTaskService;
    @Autowired
    private ClusterConfigService clusterConfigService;
    private List<String> taskQueue = new ArrayList<>();
    private PartitionService partitionService;

    private ThreadPoolTaskScheduler taskScheduler;

    private static void updateMangleNodeCurrentStatus(MangleNodeStatus nodeStatus) {
        URLConstants.setMangleNodeStatus(nodeStatus);
    }

    @Autowired
    public void setTaskScheduler(ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        this.taskScheduler = threadPoolTaskScheduler;
    }

    @Override
    public void memberAdded(MembershipEvent membershipEvent) {
        Address addedMember = membershipEvent.getMember().getAddress();
        clusterConfigService.handleQuorumForNewNodeAddition();
        hz.getConfig().getNetworkConfig().getJoin().getTcpIpConfig().addMember(addedMember.getHost());
        log.debug("Member {} joined the cluster", addedMember);
    }

    /**
     *
     * Method is triggered when member leaves the cluster
     *
     * Upon the member leaving the cluster, the tasks that were being executed on the dead node are to
     * be re-triggered on the each of the other node, depending on which node owns the partition, in
     * which key(task id in our case) is stored
     *
     * The task that are triggered in this method are only those, which sits in the partition that are
     * already migrated to another node(new owner) when it joined the cluster, but those tasks were
     * continued to be executed on the same node(old owner), on which they had first started executing
     *
     *
     *
     * @param membershipEvent
     *            Event object that is generated by the hazelcast when a member leaves the hazelcast
     *            cluster
     */
    @Override
    public void memberRemoved(MembershipEvent membershipEvent) {
        log.debug("Member removed event triggered by hazelcast instance {} leaving the cluster",
                membershipEvent.getMember().getAddress());
        updateClusterConfiguration(membershipEvent);

        String removedNodeUUID = membershipEvent.getMember().getUuid();
        String currentNodeUUID = hz.getCluster().getLocalMember().getUuid();

        IMap<String, Set<String>> nodeToTaskMapping = hz.getMap(HAZELCAST_NODE_TASKS_MAP);

        Set<String> executingTasks = nodeToTaskMapping.get(removedNodeUUID);
        Set<String> currentNodeTasks = nodeToTaskMapping.get(currentNodeUUID);

        // Tasks on the current node because of the member removed event
        // will only be triggered if the current node and hence the cluster has a valid quorum
        if (HazelcastConstants.mangleQourumStatus == MangleQuorumStatus.PRESENT && executingTasks != null
                && !executingTasks.isEmpty()) {
            log.info("Tasks {} are to be re-triggered, as the node {} left the cluster", executingTasks.toString(),
                    membershipEvent.getMember().getAddress());
            synchronized (hazelcastTaskService) {
                for (String taskId : executingTasks) {
                    if (partitionService.getPartition(taskId).getOwner() != null
                            && partitionService.getPartition(taskId).getOwner().getUuid().equals(currentNodeUUID)
                            && (CollectionUtils.isEmpty(currentNodeTasks) || !currentNodeTasks.contains(taskId))) {
                        try {
                            triggerTask(taskId);
                        } catch (MangleException e) {
                            log.error("Failed to re-trigger the task {} because  of the exception {}", taskId,
                                    e.getMessage());
                        }
                    }
                }
            }
        }
        this.taskScheduler.schedule(this::triggerTasks,
                new Date(System.currentTimeMillis() + Constants.ONE_MINUTE_IN_MILLIS * 5));
    }

    private void triggerTask(String taskId) throws MangleException {
        log.debug("Task {} will be triggered on the current node", taskId);
        log.info("Triggering the task {}", taskId);
        if (hazelcastTaskService.isScheduledTask(taskId)) {
            hazelcastTaskService.triggerTask(taskId);
        } else {
            taskQueue.add(taskId);
        }
    }

    @Override
    public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
        log.info("Member attribute {} has been modified to {} on the node {}", memberAttributeEvent.getKey(),
                memberAttributeEvent.getValue(), memberAttributeEvent.getMember().getAddress());
        if (memberAttributeEvent.getKey() == HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE) {
            handleMangleNodeCurrentStatusChange(memberAttributeEvent);
        }
    }

    private void handleMangleNodeCurrentStatusChange(MemberAttributeEvent memberAttributeEvent) {
        if (memberAttributeEvent.getValue().equals(MangleNodeStatus.MAINTENANCE_MODE.name())
                && !URLConstants.getMangleNodeCurrentStatus().equals(MangleNodeStatus.MAINTENANCE_MODE)) {
            updateMangleNodeCurrentStatus(MangleNodeStatus.PAUSE);
            //Wait for tasks to complete
            int i = 0;
            while (i++ < TASKS_WAIT_TIME_SECONDS / 10) {
                if (taskRunner.getRunningTasks().size() <= 1) {
                    break;
                }
                CommonUtils.delayInSeconds(10);
            }
            hz.getCluster().getLocalMember().setStringAttribute(HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE,
                    MangleNodeStatus.MAINTENANCE_MODE.name());
            updateMangleNodeCurrentStatus(MangleNodeStatus.MAINTENANCE_MODE);

        } else {
            String value = (String) memberAttributeEvent.getValue();
            updateMangleNodeCurrentStatus(MangleNodeStatus.valueOf(value));
            hz.getCluster().getLocalMember().setStringAttribute(HAZELCAST_MANGLE_NODE_CURRENT_STATUS_ATTRIBUTE, value);
        }
    }

    /**
     * Used for the lazy initialization of the cluster membership listener and its depended objects;
     *
     * @param hazelcastInstance
     */
    @Override
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
        hz = hazelcastInstance;
        hazelcastTaskService.setHazelcastInstance(hz);
        partitionService = hz.getPartitionService();
        taskRunner.setNode(hz.getCluster().getLocalMember().getAddress().getHost());
    }

    /**
     * Method will remove the member entry of the removed node present in the cluster config This will
     * only take place on the nodes that have the quorum
     *
     * also removes the ip of the removed node from the node status replicated map
     *
     * @param membershipEvent
     */
    private void updateClusterConfiguration(MembershipEvent membershipEvent) {
        log.debug("Received request to update cluster config as part of member removed event");
        synchronized (clusterConfigService) {
            HazelcastClusterConfig config = clusterConfigService.getClusterConfiguration();
            Address removedMemberAddress = membershipEvent.getMember().getAddress();
            if (isValidNodeToUpdateClusterConfig(config, membershipEvent)
                    && !CollectionUtils.isEmpty(config.getMembers())) {
                config.getMembers().remove(removedMemberAddress.getHost());
                if (isPersistedMasterRemoved(config, membershipEvent) && isQuorumPresent(membershipEvent)) {
                    log.info("Updating master reference as the node removed is the master node");
                    config.setMaster(getOldestMemberIPInCluster(membershipEvent));
                }
                clusterConfigService.updateClusterConfiguration(config);
            }
            ReplicatedMap<String, Boolean> nodeStatus =
                    hz.getReplicatedMap(HazelcastConstants.MANGLE_APPLICATION_STATUS_MAP);
            nodeStatus.remove(removedMemberAddress.getHost());
        }
    }

    private void triggerTasks() {
        for (String taskId : taskQueue) {
            try {
                hazelcastTaskService.triggerTask(taskId);
            } catch (MangleException e) {
                log.error("Failed to re-trigger the task {} because  of the exception {}", taskId, e.getMessage());
            }
        }
    }

    /**
     * At any particular point in time, when a node leaves a cluster, then the entry of that node can be
     * removed from the db by a node which meets the following terms,
     *
     * 1. If the node is the last identified master(as persisted in the DB)
     *
     * 2. If the removed node is the last persisted master, and if the current node is the oldest node
     * in the cluster with the quorum
     *
     * @param config
     * @param event
     * @return
     */
    private boolean isValidNodeToUpdateClusterConfig(HazelcastClusterConfig config, MembershipEvent event) {
        return (isPersistedMasterClusterOldestMember(config, event)
                || (isQuorumPresent(event) && isPersistedMasterRemoved(config, event)) || null == config.getMaster())
                && isCurrentNodeClusterOldestMember(event);
    }

    private boolean isCurrentNodeClusterOldestMember(MembershipEvent event) {
        String clusterOldestNode = getOldestMemberIPInCluster(event);
        String currentNode = hz.getCluster().getLocalMember().getAddress().getHost();
        return null != currentNode && currentNode.equals(clusterOldestNode);
    }

    private boolean isPersistedMasterRemoved(HazelcastClusterConfig config, MembershipEvent event) {
        String removedMemberAddress = event.getMember().getAddress().getHost();
        return null != removedMemberAddress && null != config.getMaster()
                && removedMemberAddress.equals(config.getMaster());
    }

    private boolean isPersistedMasterClusterOldestMember(HazelcastClusterConfig config, MembershipEvent event) {
        String clusterOldestNode = getOldestMemberIPInCluster(event);
        String persistedMaster = config.getMaster();
        return null != persistedMaster && persistedMaster.equals(clusterOldestNode);
    }

    private boolean isQuorumPresent(MembershipEvent event) {
        return HazelcastConstants.mangleQourum <= event.getMembers().size();
    }

    private String getOldestMemberIPInCluster(MembershipEvent event) {
        return event.getMembers().iterator().next().getAddress().getHost();
    }


}
