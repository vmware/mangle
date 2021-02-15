import { Component, OnDestroy, OnInit } from "@angular/core";
import { RequestsService } from "../requests.service";
import { MessageConstants } from "src/app/common/message.constants";
import { Router } from "@angular/router";
import { DataService } from "src/app/shared/data.service";
import { CommonConstants } from "src/app/common/common.constants";
import { interval, Subscription } from "rxjs";

@Component({
  selector: "app-processed",
  templateUrl: "./processed.component.html"
})
export class ProcessedComponent implements OnInit, OnDestroy {

  constructor(private requestsService: RequestsService, private router: Router, private dataService: DataService) {

  }


  public alertMessage: string;
  public isErrorMessage: boolean;
  public viewReportFlag: boolean;
  public notifierModal: boolean;
  public viewExtraData: boolean;

  public extraData: string;

  public selectedTasks: any = [];
  public selectedTaskIds: any = [];
  public selectedTaskNames: any = [];

  public processedRequests: any = [];
  public processedRequestView: any;
  public totalRequests: number;
  public pageSize = 10;

  public isLoading = false;
  public enableAutoRefresh = false;
  public autoRefreshButton: string = MessageConstants.ENABLE_AUTO_REFRESH;
  private updateSubscription: Subscription;

  public lastFilterEventData: any;

  ngOnInit() {
  }

  ngOnDestroy() {
    this.disableAutoRefresh();
  }

  public getPageProcessedTasks(eventData) {
    this.lastFilterEventData = eventData;
    const filterOn: any = {
      "taskType": "",
      "taskDescription": "",
      "taskStatus": "",
      "fromIndex": 0,
      "endpointName": "",
      "toIndex": this.pageSize - 1
    };
    if (typeof eventData.page !== "undefined") {
      filterOn.fromIndex = eventData.page.from;
      if(eventData.page.to != 0){
        filterOn.toIndex = eventData.page.to;
      }
    }
    if (typeof eventData.filters !== "undefined") {
      for (let i = 0; i < eventData.filters.length; i++) {
        if (eventData.filters[i].property === "triggerui.taskStatus") {
          filterOn.taskStatus = eventData.filters[i].value;
        }
        if (eventData.filters[i].property === "taskType") {
          filterOn.taskType = eventData.filters[i].value;
        }
        if (eventData.filters[i].property === "taskDescription") {
          filterOn.taskDescription = eventData.filters[i].value;
        }
        if (eventData.filters[i].property === "endpointName") {
          filterOn.endpointName = eventData.filters[i].value;
        }
      }
    }
    this.isLoading = true;
    this.requestsService.getAllTasksBasedOnIndex(filterOn).subscribe(
      response => {
        if (response.code) {
          this.processedRequests = [];
          this.isLoading = false;
        } else {
          const res = response.content.taskList;
          this.totalRequests = response.content.taskSize;
          for (let i = 0; typeof res !== "undefined" && i < res.length; i++) {
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
          this.selectedTasks = [];
        }

      }, err => {
        this.processedRequests = [];
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public enableDisableAutoRefresh() {
    if (this.enableAutoRefresh) {
      this.updateSubscription.unsubscribe();
      this.enableAutoRefresh = false;
      this.autoRefreshButton = MessageConstants.ENABLE_AUTO_REFRESH;
    } else {
      this.updateSubscription = interval(5000).subscribe(
        (val) => {
          this.getPageProcessedTasks(this.lastFilterEventData);
        });
      this.enableAutoRefresh = true;
      this.autoRefreshButton = MessageConstants.DISABLE_AUTO_REFRESH;
    }
  }

  public disableAutoRefresh() {
    if (this.updateSubscription != null) {
      this.updateSubscription.unsubscribe();
      this.enableAutoRefresh = false;
      this.autoRefreshButton = MessageConstants.ENABLE_AUTO_REFRESH;
    }
  }

  public deleteTask(selectedTask) {
    this.selectedTaskIds = [];
    this.selectedTaskNames = [];
    if (selectedTask !== undefined) {
      for (let i = 0; i < selectedTask.length; i++) {
        this.selectedTaskIds.push(selectedTask[i].id);
        this.selectedTaskNames.push(selectedTask[i].taskName);
      }
    } else {
      this.alertMessage = MessageConstants.NO_TASK_SELECTED;
    }
    if (confirm(MessageConstants.DELETE_CONFIRM + this.selectedTaskNames + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.requestsService.deleteTask(this.selectedTaskIds).subscribe(
        res => {
          this.getPageProcessedTasks(this.lastFilterEventData);
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.TASK_DELETE;
          this.isLoading = false;
        }, err => {
          this.getPageProcessedTasks(this.lastFilterEventData);
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
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
          this.getPageProcessedTasks(this.lastFilterEventData);
          this.isErrorMessage = false;
          this.alertMessage = processedRequest.taskName + MessageConstants.REMEDIATION_TASK_TRIGGERED;
          this.isLoading = false;
        }, err => {
          this.getPageProcessedTasks(this.lastFilterEventData);
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
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
        this.getPageProcessedTasks(this.lastFilterEventData);
        if (processedRequest.taskType === "REMEDIATION") {
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.REMEDIATION_RE_TRIGGERED;
        } else {
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.FAULT_TRIGGERED;
        }
        this.isLoading = false;
      }, err => {
        this.getPageProcessedTasks(this.lastFilterEventData);
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public enableEditRetrigger(selectedTask): boolean {
    if (selectedTask.scheduledTask === false && selectedTask.taskType === "INJECTION"
      && (selectedTask.triggerui.taskStatus === "COMPLETED" || selectedTask.triggerui.taskStatus === "FAILED")) {
      if ((selectedTask.triggerui.childTaskIDs.length === 0 && selectedTask.taskName.includes("VCenterSpecificFaultTaskHelper") || selectedTask.taskData.k8sArguments != null) || selectedTask.taskData.specType === CommonConstants.COMMAND_EXECUTION_FAULT_SPEC) {
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  public enableRemediate(selectedTask): boolean {
    if (selectedTask !== undefined) {
      if (selectedTask.remediated === false && selectedTask.scheduledTask === false && selectedTask.taskType === "INJECTION"
        && (selectedTask.triggerui.taskStatus === "COMPLETED" || selectedTask.triggerui.taskStatus === "INJECTED")) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public disableActions(selectedTask): boolean {
    if (selectedTask !== undefined) {
      if (selectedTask.taskType === "REMEDIATION"
        || selectedTask.taskType === "RESILIENCY_SCORE") {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public enableReTrigger(selectedTask): boolean {
    if (selectedTask !== undefined) {
      if (selectedTask.scheduledTask === false && ((selectedTask.taskType === "INJECTION"
        && (selectedTask.triggerui.taskStatus === "COMPLETED" || selectedTask.triggerui.taskStatus === "FAILED"))
        || (selectedTask.taskType === "REMEDIATION" && selectedTask.triggerui.taskStatus === "FAILED"))) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public reRun(processedRequest) {
    this.dataService.sharedData = processedRequest.taskData;
    if (typeof this.dataService.sharedData !== undefined || this.dataService.sharedData != null) {
      this.dataService.faultType = CommonConstants.INFRA_FAULTS;
      let faultSpecType: string = this.dataService.sharedData.specType;
      if (faultSpecType === CommonConstants.K8S_FAULT_SPEC || faultSpecType === CommonConstants.ENDPOINT_GROUP_FAULT_SPEC || faultSpecType === CommonConstants.VCENTER_FAULT_SPEC) {
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
        case CommonConstants.STOP_SERVICE_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.STOP_SERVICE_FAULT_URL);
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
        case CommonConstants.VCENTER_HOST_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_VCENTER;
          this.router.navigateByUrl(CommonConstants.VCENTER_HOST_FAULT_URL);
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
        case CommonConstants.AWS_EC2_STORAGE_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AWS;
          this.router.navigateByUrl(CommonConstants.AWS_EC2_STORAGE_FAULT_URL);
          break;
        }
        case CommonConstants.AWS_RDS_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AWS;
          this.router.navigateByUrl(CommonConstants.AWS_RDS_FAULT_URL);
          break;
        }
        case CommonConstants.AZURE_VM_STATE_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AZURE;
          this.router.navigateByUrl(CommonConstants.AZURE_VM_STATE_FAULT_URL);
          break;
        }
        case CommonConstants.AZURE_VM_NETWORK_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AZURE;
          this.router.navigateByUrl(CommonConstants.AZURE_VM_NETWORK_FAULT_URL);
          break;
        }
        case CommonConstants.AZURE_VM_STORAGE_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_AZURE;
          this.router.navigateByUrl(CommonConstants.AZURE_VM_STORAGE_FAULT_URL);
          break;
        }
        case CommonConstants.NETWORK_FAULT_SPEC: {
          this.dataService.infraSubType = CommonConstants.INFRA_FAULTS_NETWORK;
          if (this.dataService.sharedData.faultOperation === CommonConstants.NETWORK_DELAY_MILLISECONDS) {
            this.router.navigateByUrl(CommonConstants.PACKET_DELAY_FAULT_URL);
          } else if (this.dataService.sharedData.faultOperation === CommonConstants.PACKET_DUPLICATE_PERCENTAGE) {
            this.router.navigateByUrl(CommonConstants.PACKET_DUPLICATE_FAULT_URL);
          } else if (this.dataService.sharedData.faultOperation === CommonConstants.PACKET_CORRUPTION_PERCENTAGE) {
            this.router.navigateByUrl(CommonConstants.PACKET_CORRUPTION_FAULT_URL);
          } else {
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
        case CommonConstants.DB_CONNECTION_LEAK_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.router.navigateByUrl(CommonConstants.DB_CONNECTION_LEAK_FAULT_URL);
          break;
        }
        case CommonConstants.DB_TRANSACTION_ERROR_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.router.navigateByUrl(CommonConstants.DB_TRANSACTION_ERROR_FAULT_URL);
          break;
        }
        case CommonConstants.CLOCK_SKEW_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.CLOCK_SKEW_FAULT_URL);
          break;
        }
        case CommonConstants.DB_TRANSACTION_LATENCY_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.router.navigateByUrl(CommonConstants.DB_TRANSACTION_LATENCY_FAULT_URL);
          break;
        }
        case CommonConstants.REDIS_DB_DELAY_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.dataService.infraSubType = CommonConstants.DB_FAULTS_REDIS;
          this.router.navigateByUrl(CommonConstants.REDIS_DB_DELAY_FAULT_URL);
          break;
        }
        case CommonConstants.REDIS_DB_RETURN_ERROR_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.dataService.infraSubType = CommonConstants.DB_FAULTS_REDIS;
          this.router.navigateByUrl(CommonConstants.REDIS_DB_RETURN_ERROR_FAULT_URL);
          break;
        }
        case CommonConstants.REDIS_DB_RETURN_EMPTY_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.dataService.infraSubType = CommonConstants.DB_FAULTS_REDIS;
          this.router.navigateByUrl(CommonConstants.REDIS_DB_RETURN_EMPTY_FAULT_URL);
          break;
        }
        case CommonConstants.REDIS_DB_DROP_CONN_FAULT_SPEC: {
          this.dataService.faultType = CommonConstants.DB_FAULTS;
          this.dataService.infraSubType = CommonConstants.DB_FAULTS_REDIS;
          this.router.navigateByUrl(CommonConstants.REDIS_DB_DROP_CONN_FAULT_URL);
          break;
        }
        case CommonConstants.NETWORK_PARTITION_FAULT_SPEC: {
          this.router.navigateByUrl(CommonConstants.NETWORK_PARTITION_FAULT_URL);
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
          if (req[0].pluginVersion !== this.dataService.sharedData.pluginMetaInfo.pluginVersion) {
            this.isErrorMessage = true;
            this.alertMessage = MessageConstants.PLUGIN_UPGRADED_ERR;
          } else {
            this.router.navigateByUrl(CommonConstants.CUSTOM_FAULT_URL);
          }
        } else {
          this.isErrorMessage = true;
          this.alertMessage = MessageConstants.PLUGIN_UNAVAILABLE;
        }
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.error;
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
