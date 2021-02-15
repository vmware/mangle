import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
  selector: 'fault-notifiers',
  templateUrl: './faultnotifiers.component.html',
  viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class FaultNotifiersComponent {

  constructor() { }

  @Input() notifiersData: any;
  @Output() notifiersModalEvent = new EventEmitter<boolean>();

  public removeNotifiers(notifierName: string) {
    const index: number = this.notifiersData.indexOf(notifierName, 0);
    if (index > -1) {
      this.notifiersData.splice(index, 1);
    }
  }

  public openNotifiersModal() {
    this.notifiersModalEvent.emit(true);
  }

}

