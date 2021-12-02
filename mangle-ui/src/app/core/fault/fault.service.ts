import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ServiceConstants } from 'src/app/common/service.constants';
import { Observable } from 'rxjs';
import { CommonConstants } from 'src/app/common/common.constants';

@Injectable({
    providedIn: 'root'
})
export class FaultService {

    constructor(private http: HttpClient) { }

    public executeCpuFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_CPU, faultData);
    }

    public executeMemoryFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_MEMORY, faultData);
    }

    public executeDiskIOFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_DISKIO, faultData);
    }

    public executeKillProcessFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_KILL_PROCESS, faultData);
    }

    public executeDockerStateChangeFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_DOCKER, faultData);
    }

    public executeK8SDeleteResourceFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_K8S_DELETE_RESOURCE, faultData);
    }

    public executeK8SDrainNodeFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_DRAIN_K8S_NODE, faultData);
    }

    public executeK8SResourceNotReadyFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_K8S_RESOURCE_NOT_READY, faultData);
    }

    public executeK8SServiceUnavailable(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_K8S_SERVICE_UNAVAILABLE, faultData);
    }

    public executeVCenterVMDiskFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_VM_DISK, faultData);
    }

    public executeVCenterVMNicFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_VM_NIC, faultData);
    }

    public executeVCenterVMStateFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_VM_STATE, faultData);
    }

    public executeVCenterHostStateFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_HOST_STATE, faultData);
    }

    public executeNetworkFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_NETWORK, faultData);
    }

    public executeAwsEC2StateFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AWS_EC2_STATE, faultData);
    }

    public executeAwsRDSFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AWS_RDS, faultData);
    }

    public executeAzureVMStateFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AZURE_VM_STATE, faultData);
    }

    public executeAzureVMStorageFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AZURE_VM_STORAGE, faultData);
    }

    public executeAwsEC2NetworkFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AWS_EC2_NETWORK, faultData);
    }

    public executeAwsEC2StorageFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AWS_EC2_STORAGE, faultData);
    }

    public executeAzureVMNetworkFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AZURE_VM_NETWORK, faultData);
    }

    public executeFilehandlerLeakFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_FILE_HANDLER_LEAK, faultData);
    }
    public executeThreadLeakFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_THREAD_LEAK, faultData);
    }
    public executeJavaMethodLatencyFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_JAVA_METHOD_LATENCY, faultData);
    }
    public executeSpringServiceLatencyFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_SPRING_SERVICE_LATENCY, faultData);
    }
    public executeSpringServiceExceptionFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_SPRING_SERVICE_EXCEPTION, faultData);
    }
    public executeKillJVMFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_KILL_JVM, faultData);
    }
    public executeSimulateJavaExceptionFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_SIMULATE_JAVA_EXCEPTION, faultData);
    }


    public removeExtraArgs(faultData) {
        if (typeof faultData.schedule !== "undefined") {
            if ((faultData.schedule.cronExpression == null || faultData.schedule.cronExpression == "") && (faultData.schedule.timeInMilliseconds == null || faultData.schedule.timeInMilliseconds == "")) {
                delete faultData["schedule"];
            } else {
                if (faultData.schedule.cronExpression == null || faultData.schedule.cronExpression == "") {
                    delete faultData.schedule["cronExpression"];
                }
                if (faultData.schedule.timeInMilliseconds == null || faultData.schedule.timeInMilliseconds == "") {
                    delete faultData.schedule["timeInMilliseconds"];
                }
                if (faultData.schedule.description == null || faultData.schedule.description == "") {
                    delete faultData.schedule["description"];
                }
            }
        }
        if (typeof faultData.injectionHomeDir !== "undefined") {
            if (faultData.injectionHomeDir == null || faultData.injectionHomeDir == "") {
                delete faultData["injectionHomeDir"];
            }
        }
        if (typeof faultData.jvmProperties !== "undefined") {
            if (faultData.jvmProperties.javaHomePath == null || faultData.jvmProperties.javaHomePath == "") {
                delete faultData.jvmProperties["javaHomePath"];
            }
            if (faultData.jvmProperties.user == null || faultData.jvmProperties.user == "") {
                delete faultData.jvmProperties["user"];
            }
        }
        if (typeof faultData.dockerArguments !== "undefined") {
            if (faultData.dockerArguments.containerName == null || faultData.dockerArguments.containerName == "") {
                delete faultData["dockerArguments"];
            }
        }
        if (typeof faultData.k8sArguments !== "undefined") {
            if (faultData.k8sArguments.containerName == null || faultData.k8sArguments.containerName == "") {
                delete faultData["k8sArguments"];
            }
        }
        if (typeof faultData.remediationCommand !== "undefined") {
            if (faultData.remediationCommand == null || faultData.remediationCommand == "") {
                delete faultData["remediationCommand"];
            }
        }
        if (typeof faultData.tags !== "undefined") {
            if (JSON.stringify(faultData.tags) === JSON.stringify({})) {
                delete faultData["tags"];
            }
        }
    }

    public getPluginDetails(): Observable<any> {
        return this.http.get(ServiceConstants.PLUGIN_DETAILS);
    }

    public getCustomFaultJson(pluginId, faultName): Observable<any> {
        return this.http.get(ServiceConstants.PLUGIN_REQ_JSON + CommonConstants.QUESTION_MARK + CommonConstants.pluginId + CommonConstants.EQUALS_TO + pluginId + CommonConstants.AND_OP + CommonConstants.faultName + CommonConstants.EQUALS_TO + faultName);
    }

    public executeCustomFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.PLUGINS_CUSTOM_FAULT, faultData);
    }

    public executeDiskSpaceFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_DISK_SPACE, faultData);
    }

    public executeKernelPanicFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.KERNEL_PANIC_FAULT_URL, faultData);
    }

    public executeConnectionLeakFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.DB_CONNECTION_LEAK_URL, faultData);
    }

    public executeTransactionErrorFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.DB_TRANSACTION_ERROR_URL, faultData);
    }

    public getTransactionErrorCode(dbType: string): Observable<any> {
        return this.http.get(ServiceConstants.DB_TRANSACTION_ERROR_CODES_URL + dbType);
    }

    public executeClockSkewFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.CLOCK_SKEW_FAULT_URL, faultData);
    }

    public executeStopServiceFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_STOP_SERVICE, faultData);
    }

    public executeTransactionLatencyFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.DB_TRANSACTION_LATENCY_URL, faultData);
    }

    public executeRedisDelayFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.REDIS_DELAY_URL, faultData);
    }

    public executeRedisReturnErrorFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.REDIS_RETURN_ERROR_URL, faultData);
    }

    public executeRedisReturnEmptyFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.REDIS_RETURN_EMPTY_URL, faultData);
    }

    public executeRedisDropConnectionFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.REDIS_DROP_CONNECTION_URL, faultData);
    }

    public executeNetworkPartitionFault(faultData: any): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.NETWORK_PARTITION_URL, faultData);
    }
}
