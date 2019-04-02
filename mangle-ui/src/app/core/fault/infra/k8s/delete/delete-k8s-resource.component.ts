import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { CommonFault } from '../../../common.fault';
import { FaultService } from '../../../fault.service';

@Component({
  selector: 'app-delete-k8s-resource',
  templateUrl: './delete-k8s-resource.component.html',
  styleUrls: ['./delete-k8s-resource.component.css']
})
export class DeleteK8SResourceComponent implements OnInit {

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
  public k8sResourceTypes: any = ["POD", "NODE"]

  public faultFormData: any = {
    "endpointName": null,
    "resourceType": null,
    "resource": "",
    "randomInjection": false,
    "injectionHomeDir": null,
    "schedule": {
      "cronExpression": null
    }
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

  public executeK8SDeleteResourceFault(faultData) {
    this.errorFlag = false;
    this.successFlag = false;
    if (faultData.resource.startsWith("{")) {
      faultData.resourceLabels = JSON.parse(faultData.resource);
    } else {
      faultData.resourceName = faultData.resource;
    }
    delete faultData["resource"];
    this.faultService.executeK8SDeleteResourceFault(faultData).subscribe(
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
