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

    public executeDockerStateChangeFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_DOCKER, faultData);
    }

    public executeK8SDeleteResourceFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_K8S_DELETE_RESOURCE, faultData);
    }

    public executeK8SResourceNotReadyFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_K8S_RESOURCE_NOT_READY, faultData);
    }

    public executeK8SServiceUnavailable(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_K8S_SERVICE_UNAVAILABLE, faultData);
    }

    public executeVcenterDiskFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_DISK, faultData);
    }

    public executeVcenterNicFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_NIC, faultData);
    }

    public executeVcenterStateFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_VCENTER_STATE, faultData);
    }
    public executeNetworkFault(faultData): Observable<any> {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_NETWORK, faultData);
    }

    public executeAwsEC2StateFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AWS_EC2_STATE, faultData);
    }

    public executeAwsEC2NetworkFault(faultData) {
        this.removeExtraArgs(faultData);
        return this.http.post(ServiceConstants.FAULTS_AWS_EC2_NETWORK, faultData);
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
            if ((faultData.schedule.cronExpression == "" || faultData.schedule.cronExpression == null) && (faultData.schedule.timeInMilliseconds == "" || faultData.schedule.timeInMilliseconds == null)) {
                delete faultData["schedule"];
            } else {
                if (faultData.schedule.cronExpression == "" || faultData.schedule.cronExpression == null) {
                    delete faultData.schedule["cronExpression"];
                }
                if (faultData.schedule.timeInMilliseconds == "" || faultData.schedule.timeInMilliseconds == null) {
                    delete faultData.schedule["timeInMilliseconds"];
                }
                if (faultData.schedule.description == "" || faultData.schedule.description == null) {
                    delete faultData.schedule["description"];
                }
            }
        }
        if (typeof faultData.injectionHomeDir !== "undefined") {
            if (faultData.injectionHomeDir == "" || faultData.injectionHomeDir == null) {
                delete faultData["injectionHomeDir"];
            }
        }
        if (typeof faultData.jvmProperties !== "undefined") {
            if (faultData.jvmProperties.javaHomePath == "" || faultData.jvmProperties.javaHomePath == null) {
                delete faultData.jvmProperties["javaHomePath"];
            }
            if (faultData.jvmProperties.user == "" || faultData.jvmProperties.user == null) {
                delete faultData.jvmProperties["user"];
            }
        }
        if (typeof faultData.dockerArguments !== "undefined") {
            if (faultData.dockerArguments.containerName == "" || faultData.dockerArguments.containerName == null) {
                delete faultData["dockerArguments"];
            }
        }
        if (typeof faultData.k8sArguments !== "undefined") {
            if (faultData.k8sArguments.containerName == "" || faultData.k8sArguments.containerName == null) {
                delete faultData["k8sArguments"];
            }
        }
        if (typeof faultData.remediationCommand !== "undefined") {
            if (faultData.remediationCommand == "" || faultData.remediationCommand == null) {
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
}
