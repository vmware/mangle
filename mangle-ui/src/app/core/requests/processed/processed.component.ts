import { Component, OnInit } from '@angular/core';
import { RequestsService } from '../requests.service';
import { ClrDatagridSortOrder } from '@clr/angular';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
  selector: 'app-processed',
  templateUrl: './processed.component.html',
  styleUrls: ['./processed.component.css']
})
export class ProcessedComponent implements OnInit {

  constructor(private requestsService: RequestsService) {

  }

  public errorFlag = false;
  public successFlag = false;
  public alertMessage: string;

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
        this.alertMessage = err.error.description;
        this.errorFlag = true;
        this.isLoading = false;
      });
  }

  public deleteTask(processedRequest) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.DELETE_CONFIRM + processedRequest.taskName + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.requestsService.deleteTask(processedRequest.id).subscribe(
        res => {
          this.getAllProcessedTasks();
          this.alertMessage = processedRequest.taskName + MessageConstants.TASK_DELETE;
          this.successFlag = true;
          this.isLoading = false;
        }, err => {
          this.getAllProcessedTasks();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
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
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm(MessageConstants.REMEDIATE_CONFIRM + processedRequest.taskName + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.requestsService.remediateFault(processedRequest.id).subscribe(
        res => {
          this.getAllProcessedTasks();
          this.alertMessage = processedRequest.taskName + MessageConstants.REMEDIATION_TASK_TRIGGERED;
          this.successFlag = true;
          this.isLoading = false;
        }, err => {
          this.getAllProcessedTasks();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

  public viewReport(processedRequest) {
    this.processedRequestView = JSON.parse(JSON.stringify(processedRequest));
    delete this.processedRequestView["triggerui"];
  }

}
