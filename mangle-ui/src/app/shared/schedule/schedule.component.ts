import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
  selector: 'fault-schedule',
  templateUrl: './schedule.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class ScheduleComponent {

  constructor() { }

  @Input() faultFormData: any;
  @Output() submitButtonChange = new EventEmitter<string>();
  @Output() setCronModal = new EventEmitter<boolean>()

  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;
  public selectedSchedulePrev: string = "";

  public setScheduleVal(selectedSchedule) {
    if (this.selectedSchedulePrev == selectedSchedule.value) {
      selectedSchedule.checked = false;
      this.timeInMillisecondsHidden = true;
      this.cronExpressionHidden = true;
      this.descriptionHidden = true;
    } else {
      this.timeInMillisecondsHidden = true;
      this.cronExpressionHidden = true;
      this.descriptionHidden = true;
      if (selectedSchedule.value == "timeInMilliseconds") {
        this.timeInMillisecondsHidden = false;
        this.descriptionHidden = false;
      }
      if (selectedSchedule.value == "cronExpression") {
        this.cronExpressionHidden = false;
        this.descriptionHidden = false;
      }
      this.selectedSchedulePrev = selectedSchedule.value;
    }
  }

  public callSubmitButtonChange() {
    this.submitButtonChange.emit("");
  }

  public showCronModal() {
    this.setCronModal.emit(true);
  }

}

