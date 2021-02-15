import { Component, Input } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
    selector: 'fault-jvmargs',
    templateUrl: './jvmargs.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class JvmArgsComponent {

    constructor() { }

    @Input() faultFormData: any;
    @Input() k8sHidden: boolean;
    @Input() dockerHidden: boolean;

}

