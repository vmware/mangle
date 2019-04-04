import { Component, OnInit } from '@angular/core';
import { RequestsService } from '../requests.service';
import { ClrDatagridSortOrder } from '@clr/angular';

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
            res[i].triggerui = res[i].triggers[0];
            if (res[i].taskData.endpointName === undefined) {
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
    if (confirm('Do you want to delete: ' + processedRequest.taskName + ' task?')) {
      this.isLoading = true;
      this.requestsService.deleteTask(processedRequest.id).subscribe(
        res => {
          this.getAllProcessedTasks();
          this.alertMessage = processedRequest.taskName + ' task deleted successfully!';
          this.successFlag = true;
          this.isLoading = false;
        }, err => {
          this.getAllProcessedTasks();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }

  public remediateFault(processedRequest) {
    this.errorFlag = false;
    this.successFlag = false;
    if (confirm('Do you want to remediate: ' + processedRequest.taskName + ' task?')) {
      this.isLoading = true;
      this.requestsService.remediateFault(processedRequest.id).subscribe(
        res => {
          this.getAllProcessedTasks();
          this.alertMessage = processedRequest.taskName + ' remediation task triggered!';
          this.successFlag = true;
          this.isLoading = false;
        }, err => {
          this.getAllProcessedTasks();
          this.alertMessage = err.error.description;
          this.errorFlag = true;
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }

  public viewReport(processedRequest) {
    this.processedRequestView = processedRequest;
  }

}
