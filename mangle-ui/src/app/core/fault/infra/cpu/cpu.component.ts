import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { CommonFault } from '../../common.fault';

@Component({
  selector: 'app-cpu-infra',
  templateUrl: './cpu.component.html',
  styleUrls: ['./cpu.component.css']
})
export class CpuInfraComponent implements OnInit {

  public commonFault: CommonFault = new CommonFault();

  public errorFlag = false;
  public successFlag = false;
  public alertMessage: string;

  public clockRanges: any;
  public hourRanges: any;
  public dateRages: any;
  public dayRanges: any;
  public cronType: string = "Minutes";

  public disableSchedule: boolean = true;
  public disableRun: boolean = false;

  public endpoints: any = [];
  public dockerHidden: boolean = true;
  public k8sHidden: boolean = true;

  public faultFormData: any = {
    "cpuLoad": 0,
    "dockerArguments": {
      "containerName": null
    },
    "endpointName": null,
    "k8sArguments": {
      "containerName": null,
      "enableRandomInjection": true,
      "podLabels": null
    },
    "schedule": {
      "cronExpression": null
    },
    "timeoutInMilliseconds": null
  };

  constructor(private faultService: FaultService, private endpointService: EndpointService, private router: Router) {

  }

  ngOnInit() {
    this.clockRanges = this.commonFault.getClockRanges();
    this.hourRanges = this.commonFault.getHourRanges();
    this.dateRages = this.commonFault.getDateRages();
    this.dayRanges = this.commonFault.getDayRanges();
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        if (res.code) {
          this.endpoints = [];
        } else {
          this.endpoints = res;
        }
      });
  }

  public displayEndpointFields(endpointNameVal) {
    this.dockerHidden = true;
    this.k8sHidden = true;
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        if (this.endpoints[i].endPointType == 'DOCKER') {
          this.dockerHidden = false;
        }
        if (this.endpoints[i].endPointType == 'K8S_CLUSTER') {
          this.k8sHidden = false;
        }
      }
    }
  }

  public composeSchedule(scheduleFormVal) {
    scheduleFormVal.cronType = this.cronType;
    this.faultFormData.schedule.cronExpression = this.commonFault.getCronExpression(scheduleFormVal);
    this.setSubmitButton();
  }

  setSubmitButton() {
    if (this.faultFormData.schedule.cronExpression == "" || this.faultFormData.schedule.cronExpression == null) {
      this.disableSchedule = true;
      this.disableRun = false;
    } else {
      this.disableSchedule = false;
      this.disableRun = true;
    }
  }

  public executeCpuFault(faultData) {
    this.errorFlag = false;
    this.successFlag = false;
    this.faultService.executeCpuFault(faultData).subscribe(
      res => {
        this.alertMessage = 'Fault triggred successfully!';
        this.successFlag = true;
        this.router.navigateByUrl('core/requests');
      }, err => {
        this.alertMessage = err.error.description;
        this.errorFlag = true;
      });
  }

}
