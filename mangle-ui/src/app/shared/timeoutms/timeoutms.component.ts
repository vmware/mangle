import { Component, Input } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
    selector: 'fault-timeoutms',
    templateUrl: './timeoutms.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class TimeoutmsComponent {

    constructor() { }

    @Input() faultFormData: any;

}

