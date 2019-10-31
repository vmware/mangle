import { Component, EventEmitter, Input, Output } from '@angular/core';
@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html'
})
export class AlertComponent {
    @Input() alertMessage: string;
    @Input() isErrorMessage: boolean;
    @Output() alertMessageChange = new EventEmitter<string>();

    public constructor() {
    }

    public closeAlertMessage() {
        this.alertMessage = null;
        this.alertMessageChange.emit(this.alertMessage);
    }

}

