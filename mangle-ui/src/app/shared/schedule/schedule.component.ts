import { Component, Input, Output, EventEmitter, Inject } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';
import {DOCUMENT} from "@angular/common";

@Component({
  selector: 'fault-schedule',
  templateUrl: './schedule.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class ScheduleComponent {

  constructor(@Inject(DOCUMENT) document) { }

  @Input() faultFormData: any;
  @Output() submitButtonChange = new EventEmitter<string>();
  @Output() setCronModal = new EventEmitter<boolean>()

  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;

  public clearSelected(){
    this.timeInMillisecondsHidden = true;
    this.cronExpressionHidden = true;
    this.descriptionHidden = true;
    this.faultFormData.schedule.timeInMilliseconds = undefined;
    this.faultFormData.schedule.cronExpression = undefined;
    this.faultFormData.schedule.description = undefined;
    var radioElements = <NodeListOf<HTMLInputElement>>document.getElementsByName("scheduleBody");
    radioElements.forEach(e => {
      e.checked = false;
    });
    this.callSubmitButtonChange();
  }

  public setScheduleVal(selectedSchedule){
      var timeInMillisecondsElement = <HTMLInputElement> document.getElementById("timeInMilliseconds");
      var cronExpressionElement = <HTMLInputElement> document.getElementById("cronExpression");
      if(timeInMillisecondsElement.checked || cronExpressionElement.checked){
        if(timeInMillisecondsElement.checked) {
          selectedSchedule.value = timeInMillisecondsElement.value;
          this.timeInMillisecondsHidden = false;
          this.cronExpressionHidden = true;
        } else {
          selectedSchedule.value = cronExpressionElement.value;
          this.timeInMillisecondsHidden = true;
          this.cronExpressionHidden = false;
        }
        this.descriptionHidden = false;
      } else {
          this.timeInMillisecondsHidden = true;
          this.cronExpressionHidden = true;
          this.descriptionHidden = true;
      }
  }

  public callSubmitButtonChange() {
    this.submitButtonChange.emit("");
  }

  public showCronModal() {
    this.setCronModal.emit(true);
  }

}

