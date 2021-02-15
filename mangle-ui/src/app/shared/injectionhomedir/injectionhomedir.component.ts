import { Component, Input } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
    selector: 'fault-injectionhomedir',
    templateUrl: './injectionhomedir.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class InjectionHomeDirComponent {

    constructor() { }

    @Input() faultFormData: any;

}

