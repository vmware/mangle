import { Component, Input, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-cron',
  templateUrl: './cron.component.html'
})
export class CronComponent {

  public clockRanges: any = [];
  public hourRanges: any = [];
  public dateRages: any = [];
  public dayRanges: any = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

  public cronType: string = "Minutes";
  public cronExp: string;

  @Input() cronModal: boolean;
  @Input() cronValOrig;
  @Output() cronMessageEvent = new EventEmitter<string>();

  public constructor() {
    for (var i = 0; i < 59; i++) {
      this.clockRanges[i] = i;
      if (i < 23) {
        this.hourRanges[i] = i;
      }
      if (i < 31) {
        this.dateRages[i] = i + 1;
      }
    }
  }

  public composeCron(cronFormVal) {
    if (this.cronType == "Minutes") {
      this.cronExp = cronFormVal.second + " " + cronFormVal.startat + "/" + cronFormVal.minute + " * * * ?";
    }
    if (this.cronType == "Hourly") {
      this.cronExp = "0 " + cronFormVal.minute + " " + cronFormVal.startat + "/" + cronFormVal.hour + " * * ?";
    }
    if (this.cronType == "Daily") {
      this.cronExp = "0 " + cronFormVal.minute + " " + cronFormVal.hour + " * * ?";
    }
    if (this.cronType == "Weekly") {
      this.cronExp = "0 0 " + cronFormVal.hour + " ? * " + cronFormVal.day;
    }
    if (this.cronType == "Monthly") {
      this.cronExp = "0 0 " + cronFormVal.hour + " " + cronFormVal.date + " * ?";
    }
    this.cronMessageEvent.emit(this.cronExp);
  }

  public cancelCronModal() {
    this.cronMessageEvent.emit(this.cronValOrig);
  }

}
