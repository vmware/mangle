import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ServiceConstants } from 'src/app/common/service.constants';
import { Observable } from 'rxjs';

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

}
