import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class DataService {

    constructor() { }

    public sharedData: any = null;

    public faultType: string = null;

    public infraSubType: string = null;

}
