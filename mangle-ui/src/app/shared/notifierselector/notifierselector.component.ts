import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { NotifierService } from '../../core/setting/integration/notifier/notifier.service';

@Component({
  selector: 'app-notifiers',
  templateUrl: './notifierselector.component.html'
})
export class NotifierSelectorComponent implements OnInit {

  constructor(private notifierService: NotifierService) { }

  public notifications: any;
  @Input() notifierModal: boolean;
  @Input() notifiersData: any;
  @Output() updateNotifiersModal = new EventEmitter<boolean>();

  ngOnInit() {
    this.getNotifierInfo();
  }

  public updateNotifiers(notifiersVal: any) {
    if (!(this.notifiersData.indexOf(notifiersVal.name, 0) > -1)) {
      this.notifiersData.push(notifiersVal.name);
    }
  }

  public closeNotifierModal() {
    this.notifierModal = false;
    this.updateNotifiersModal.emit(false);
  }

  public getNotifierInfo() {
    this.notifierService.getNotificationInfo().subscribe(
      res => {
        if (res.code) {
          this.notifications = [];
        } else {
          this.notifications = res;
        }
      }, err => {
        this.notifications = [];
      });
  }
}

