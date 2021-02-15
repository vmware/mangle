import { Component, Input } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';

@Component({
    selector: 'fault-dockerargs',
    templateUrl: './dockerargs.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class DockerArgsComponent {

    constructor() { }

    @Input() faultFormData: any;
    @Input() dockerHidden: boolean;
    @Input() searchedContainers: any;
    @Input() dockerContainers: any;

    public setContainerVal(containerVal) {
        this.faultFormData.dockerArguments.containerName = containerVal;
    }

    public searchContainer(searchKeyWord) {
        this.searchedContainers = [];
        for (var i = 0; i < this.dockerContainers.length; i++) {
          if (this.dockerContainers[i].indexOf(searchKeyWord) > -1) {
            this.searchedContainers.push(this.dockerContainers[i]);
          }
        }
      }

}

