import { Component, OnInit } from '@angular/core';
import { RequestsService } from '../requests.service';
import { ClrDatagridSortOrder } from '@clr/angular';
import { MessageConstants } from 'src/app/common/message.constants';
import { Router } from '@angular/router';
import { DataService } from 'src/app/shared/data.service';
import { CommonConstants } from 'src/app/common/common.constants';

@Component({
  selector: 'app-processed',
  templateUrl: './processed.component.html'
})
export class ProcessedComponent implements OnInit {

  constructor(private requestsService: RequestsService, private router: Router, private dataService: DataService) {

  }

  public errorAlertMessage: string;
  public successAlertMessage: string;

  public extraData: string;

  public processedRequests: any = [];
  public processedRequestView: any;

  public isLoading: boolean = false;
  public startTimeDesc = ClrDatagridSortOrder.DESC;

  ngOnInit() {
    this.getAllProcessedTasks();
  }

  public getAllProcessedTasks() {
    this.isLoading = true;
    this.requestsService.getAllTasks().subscribe(
      res => {
        if (res.code) {
          this.processedRequests = [];
          this.isLoading = false;
        } else {
          for (var i = 0; i < res.length; i++) {
            if (res[i].triggers != null) {
              res[i].triggerui = res[i].triggers[res[i].triggers.length - 1];
              res[i].triggerui.startTime = new Date(res[i].triggerui.startTime);
              if (res[i].triggerui.endTime != null) {
                res[i].triggerui.endTime = new Date(res[i].triggerui.endTime);
              }
            } else {
              res[i].triggerui = {
                "taskStatus": "NOT STARTED"
              };
            }
            if (res[i].taskData.endpointName === undefined && res[i].taskData.faultSpec !== undefined) {
              res[i].taskData.endpointName = res[i].taskData.faultSpec.endpointName;
            }
          }
          this.processedRequests = res;
          this.isLoading = false;
        }
      }, err => {
        this.processedRequests = [];
        this.isLoading = false;
        this.errorAlertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public deleteTask(processedRequest) {
    if (confirm(MessageConstants.DELETE_CONFIRM + processedRequest.taskName + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.requestsService.deleteTask(processedRequest.id).subscribe(
        res => {
          this.getAllProcessedTasks();
          this.successAlertMessage = processedRequest.taskName + MessageConstants.TASK_DELETE;
          this.isLoading = false;
        }, err => {
          this.getAllProcessedTasks();
          this.errorAlertMessage = err.error.description;
          this.isLoading = false;
          if (this.errorAlertMessage === undefined) {
            this.errorAlertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public remediateFault(processedRequest) {
    if (confirm(MessageConstants.REMEDIATE_CONFIRM + processedRequest.taskName + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.requestsService.remediateFault(processedRequest.id).subscribe(
        res => {
          this.getAllProcessedTasks();
          this.successAlertMessage = processedRequest.taskName + MessageConstants.REMEDIATION_TASK_TRIGGERED;
          this.isLoading = false;
        }, err => {
          this.getAllProcessedTasks();
          this.errorAlertMessage = err.error.description;
          this.isLoading = false;
          if (this.errorAlertMessage === undefined) {
            this.errorAlertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public reTrigger(processedRequest) {
    this.isLoading = true;
    this.requestsService.rerunFault(processedRequest.id).subscribe(
      res => {
        this.getAllProcessedTasks();
        if (processedRequest.taskType == "REMEDIATION") {
          this.successAlertMessage = MessageConstants.REMEDIATION_RE_TRIGGERED;
        } else {
          this.successAlertMessage = MessageConstants.FAULT_TRIGGERED;
        }
        this.isLoading = false;
      }, err => {
        this.getAllProcessedTasks();
        this.errorAlertMessage = err.error.description;
        this.isLoading = false;
        if (this.errorAlertMessage === undefined) {
          this.errorAlertMessage = err.error.error;
        }
      });
  }

  public reRun(processedRequest) {
    this.dataService.sharedData = processedRequest.taskData;
    if (typeof this.dataService.sharedData != undefined || this.dataService.sharedData != null) {
      this.dataService.faultType = CommonConstants.INFRA_FAULTS;
      var faultSpecType: string = this.dataService.sharedData.specType;
      if (faultSpecType == CommonConstants.K8S_FAULT_SPEC) {
        faultSpecType = this.dataService.sharedData.childSpecType;
        this.dataService.sharedData = processedRequest.taskData.faultSpec;
      }
      switch (faultSpecType) {
        case CommonConstants.CPU_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.CPU_APP_FAULT_URL);
          } else {
            this.router.navigateByUrl(CommonConstants.CPU_INFRA_FAULT_URL);
          }
          break;
        }
        case CommonConstants.MEMORY_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.MEMORY_APP_FAULT_URL);
          } else {
            this.router.navigateByUrl(CommonConstants.MEMORY_INFRA_FAULT_URL);
          }
          break;
        }
        case CommonConstants.DISKIO_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.DISKIO_FAULT_URL);
          break;
        }
        case CommonConstants.KILL_PROCESS_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.KILL_PROCESS_FAULT_URL);
          break;
        }
        case CommonConstants.DOCKER_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_DOCKER;
          this.router.navigateByUrl(CommonConstants.DOCKER_FAULT_URL);
          break;
        }
        case CommonConstants.K8S_DELETE_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_K8S;
          this.router.navigateByUrl(CommonConstants.K8S_DELETE_FAULT_URL);
          break;
        }
        case CommonConstants.K8S_NOT_READY_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_K8S;
          this.router.navigateByUrl(CommonConstants.K8S_NOT_READY_FAULT_URL);
          break;
        }
        case CommonConstants.K8S_SERVICE_UNAVAILABLE_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_K8S;
          this.router.navigateByUrl(CommonConstants.K8S_SERVICE_UNAVAILABLE_URL);
          break;
        }
        case CommonConstants.VCENTER_DISK_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_VCENTER;
          this.router.navigateByUrl(CommonConstants.VCENTER_DISK_FAULT_URL);
          break;
        }
        case CommonConstants.VCENTER_NIC_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_VCENTER;
          this.router.navigateByUrl(CommonConstants.VCENTER_NIC_FAULT_URL);
          break;
        }
        case CommonConstants.VCENTER_STATE_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_VCENTER;
          this.router.navigateByUrl(CommonConstants.VCENTER_STATE_FAULT_URL);
          break;
        }

        case CommonConstants.AWS_EC2_STATE_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AWS;
          this.router.navigateByUrl(CommonConstants.AWS_EC2_STATE_FAULT_URL);
          break;
        }

        case CommonConstants.AWS_EC2_NETWORK_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AWS;
          this.router.navigateByUrl(CommonConstants.AWS_EC2_NETWORK_FAULT_URL);
          break;
        }
        case CommonConstants.NETWORK_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_NETWORK;
          if (this.dataService.sharedData.faultOperation == CommonConstants.NETWORK_DELAY_MILLISECONDS) {
            this.router.navigateByUrl(CommonConstants.PACKET_DELAY_FAULT_URL);
          }
          else if (this.dataService.sharedData.faultOperation == CommonConstants.PACKET_DUPLICATE_PERCENTAGE) {
            this.router.navigateByUrl(CommonConstants.PACKET_DUPLICATE_FAULT_URL);
          }
          else if (this.dataService.sharedData.faultOperation == CommonConstants.PACKET_CORRUPTION_PERCENTAGE) {
            this.router.navigateByUrl(CommonConstants.PACKET_CORRUPTION_FAULT_URL);
          }
          else {
            this.router.navigateByUrl(CommonConstants.PACKET_DROP_FAULT_URL);
          }
          break;
        }
        case CommonConstants.FILE_HANDLER_LEAK_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.FILE_HANDLER_LEAK_FAULT_URL);
          } else {
            this.router.navigateByUrl(CommonConstants.FILE_HANDLER_LEAK_INFRA_FAULT_URL);
          }
          break;
        }
        case CommonConstants.JAVA_METHOD_LATENCY_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.JAVA_METHOD_LATENCY_FAULT_URL);
          }
          break;
        }
        case CommonConstants.SPRING_SERVICE_LATENCY_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.SPRING_SERVICE_LATENCY_FAULT_URL);
          }
          break;
        }
        case CommonConstants.SPRING_SERVICE_EXCEPTION_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.SPRING_SERVICE_EXCEPTION_FAULT_URL);
          }
          break;
        }
        case CommonConstants.KILL_JVM_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.KILL_JVM_FAULT_URL);
          }
          break;
        }
        case CommonConstants.SIMULATE_JAVA_EXCEPTION_FAULT_SPEC: {
          if (this.dataService.sharedData.jvmProperties != null) {
            this.dataService.faultType = CommonConstants.APP_FAULTS;
            this.router.navigateByUrl(CommonConstants.SIMULATE_JAVA_EXCEPTION_FAULT_URL);
          }
          break;
        }
        case CommonConstants.DISK_SPACE_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.DISK_SPACE_FAULT_URL);
          break;
        }
        case CommonConstants.KERNEL_PANIC_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.KERNEL_PANIC_FAULT_URL);
          break;
        }
        default: {
          this.dataService.faultType = null;
          this.dataService.infraSubType = null;
          if (this.dataService.sharedData.pluginMetaInfo != null) {
            this.comparePluginEditAndReTrigger();
          } else {
            this.dataService.sharedData = null;
            this.router.navigateByUrl(CommonConstants.FAULT_URL);
          }
          break;
        }
      }
    }
  }

  public comparePluginEditAndReTrigger() {
    this.requestsService.getPluginDetails(this.dataService.sharedData.pluginMetaInfo.pluginId).subscribe(
      req => {
        if (req.length > 0) {
          if (req[0].pluginVersion != this.dataService.sharedData.pluginMetaInfo.pluginVersion) {
            this.errorAlertMessage = MessageConstants.PLUGIN_UPGRADED_ERR;
          } else {
            this.router.navigateByUrl(CommonConstants.CUSTOM_FAULT_URL);
          }
        } else {
          this.errorAlertMessage = MessageConstants.PLUGIN_UNAVAILABLE;
        }
      }, err => {
        this.errorAlertMessage = err.error.error;
      }
    );
  }

  public viewReport(processedRequest) {
    this.processedRequestView = JSON.parse(JSON.stringify(processedRequest));
    delete this.processedRequestView["triggerui"];
  }

  public showExtraData(extraDataVal) {
    this.extraData = JSON.parse(JSON.stringify(extraDataVal));
  }

}
