import { Component, Input } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
    selector: 'fault-k8sargs',
    templateUrl: './k8sargs.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class K8sArgsComponent {

    constructor() { }

    @Input() faultFormData: any;
    @Input() k8sHidden: boolean;

}

