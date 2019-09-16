import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
    selector: 'app-alert',
    templateUrl: './alert.component.html'
})
export class AlertComponent {

    @Input() errorAlertMessage: string;
    @Input() successAlertMessage: string;
    @Output() errorAlertMessageChange = new EventEmitter<string>();
    @Output() successAlertMessageChange = new EventEmitter<string>();

    public constructor() { }

    public closeErrorAlertMessage() {
        this.errorAlertMessage = null;
        this.errorAlertMessageChange.emit(this.errorAlertMessage);
    }

    public closeSuccessAlertMessage() {
        this.successAlertMessage = null;
        this.successAlertMessageChange.emit(this.successAlertMessage);
    }

}