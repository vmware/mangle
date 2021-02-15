import { Component, Input, Output, EventEmitter } from '@angular/core';
import { ControlContainer, NgForm } from '@angular/forms';
import { CommonConstants } from 'src/app/common/common.constants';
@Component({
    selector: 'fault-endpoint',
    templateUrl: './endpoint.component.html',
    viewProviders: [{ provide: ControlContainer, useExisting: NgForm }]
})
export class EndpointComponent {

    constructor() { }

    @Input() faultFormData: any;
    @Input() endpoints: any;
    @Input() supportedEpTypes: any;
    @Input() isKernelPanicFault: boolean = false;
    showSelectRandomEndpoint: boolean = false;
    @Output() displayEndpointFieldsEvent = new EventEmitter<string>();
    @Output() dockerContainersEvent = new EventEmitter<string>();
    public searchedEndpoints: any = [];

    ngOnInit() {
        if (this.faultFormData.randomEndpoint != null) {
            this.showSelectRandomEndpoint = true;
        }
    }

    public searchEndpoint(searchKeyWord) {
        this.searchedEndpoints = [];
        for (var i = 0; i < this.endpoints.length; i++) {
            if (this.endpoints[i].name.indexOf(searchKeyWord) > -1) {
                this.searchedEndpoints.push(this.endpoints[i]);
            }
        }
    }

    public setEndpointVal(endpointVal) {
        this.faultFormData.endpointName = endpointVal;
    }

    public setRandomEndpointVal(randomEndpointVal) {
        this.faultFormData.randomEndpoint = randomEndpointVal;
    }

    public displayEndpointFields(endpointVal) {
        this.displayEndpointFieldsEvent.emit(endpointVal);
    }

    public getDockerContainers(endpointVal) {
        this.dockerContainersEvent.emit(endpointVal);
    }

    public setShowSelectRandomEndpoint(endpointType) {
        console.log(endpointType + ":" + CommonConstants.ENDPOINT_GROUP);
        if (endpointType == CommonConstants.ENDPOINT_GROUP) {
            this.showSelectRandomEndpoint = true;
            this.faultFormData.randomEndpoint = true;
        } else {
            this.showSelectRandomEndpoint = false;
        }
    }

}

