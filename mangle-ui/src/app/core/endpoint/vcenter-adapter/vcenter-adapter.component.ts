import {Component, OnInit} from "@angular/core";
import {ClrLoadingState} from "@clr/angular";
import {EndpointService} from "../endpoint.service";
import {MessageConstants} from "../../../common/message.constants";

@Component({
  selector: "app-vcenter-adapter",
  templateUrl: "./vcenter-adapter.component.html",
  styleUrls: ["./vcenter-adapter.component.css"]
})
export class VcenterAdapterComponent implements OnInit {

  public vcenterAdapterDetailsList: any = [];
  public isLoading = true;
  public selected: any = [];

  public alertMessage: string;
  public isErrorMessage: boolean;
  public editModal: boolean;
  public tagsModal: boolean;
  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public testConnectionAlertMessage: string;
  public isTestConnectionErrorMessage: boolean;
  public isTestConnectionSuccessful: boolean;

  public addEdit: string;

  public vCenterAdapterDetailsOrig = {
    name: "",
    adapterUrl: "",
    username: "",
    password: ""
  };

  public vCenterAdapterDetails: any;

  constructor(private endpointService: EndpointService) {
  }

  ngOnInit() {
    this.getVCenterAdapterDetails();
  }


  public getVCenterAdapterDetails() {
    this.isLoading = true;
    this.endpointService.getVCenterAdapterDetails().subscribe(
      res => {
        if (res.code) {
          this.vcenterAdapterDetailsList = [];
          this.isLoading = false;
        } else {
          this.vcenterAdapterDetailsList = res.content === undefined ? [] : res.content;
          this.isLoading = false;
        }
      },
      error => {
        this.vcenterAdapterDetailsList = [];
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = error.error.description;
      }
    );
  }

  public onDeleteTrigger() {
    const adapterNames = this.selected.map(adapterDetails => adapterDetails.name);
    this.endpointService.deleteVCenterAdapterDetails(adapterNames).subscribe(res => {
      this.getVCenterAdapterDetails();
    }, error => {
      this.isErrorMessage = true;
      this.alertMessage = error.error.description;
    });
  }

  public onCreateTrigger() {
    this.editModal = true;
    this.addEdit = "Add";
    this.isTestConnectionSuccessful = false;
    this.isTestConnectionErrorMessage = false;
    this.testConnectionAlertMessage = null;
    this.vCenterAdapterDetails = {...this.vCenterAdapterDetailsOrig};
  }

  public onEditTrigger() {
    this.editModal = true;
    this.addEdit = "Edit";
    this.vCenterAdapterDetails = {...this.selected[0]};
    this.vCenterAdapterDetails.password = "";
    this.isTestConnectionSuccessful = false;
    this.isTestConnectionErrorMessage = false;
    this.testConnectionAlertMessage = null;
  }

  public addOrUpdateVCenterAdapterDetails(form) {
    if (form.valid && form.dirty) {
      this.submitBtnState = ClrLoadingState.LOADING;
      if (this.addEdit === "Add") {
        this.addVCenterAdapterEntry();
      } else {
        this.editVCenterAdapterEntry();
      }
    }
  }

  public addVCenterAdapterEntry() {
    this.isLoading = true;
    this.endpointService.addVCenterAdapterDetails(this.vCenterAdapterDetails).subscribe(
      res => {
        this.isErrorMessage = false;
        this.isLoading = false;
        this.vcenterAdapterDetailsList.push(res);
        this.editModal = false;
        this.submitBtnState = ClrLoadingState.DEFAULT;
      },
      error => {
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = error.error.description;
        this.submitBtnState = ClrLoadingState.DEFAULT;
        this.editModal = false;
      });

  }

  public editVCenterAdapterEntry() {
    this.endpointService.updateVCenterAdapterDetails(this.vCenterAdapterDetails).subscribe(
      res => {
        this.getVCenterAdapterDetails();
        this.isErrorMessage = false;
        this.isLoading = false;
        this.editModal = false;
        this.submitBtnState = ClrLoadingState.DEFAULT;
      },
      error => {
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = error.error.description;
        this.submitBtnState = ClrLoadingState.DEFAULT;
        this.editModal = false;
      });
  }

  public testConnection(isFormValid) {
    this.isLoading = true;
    if (isFormValid) {
      this.endpointService.testVCenterAdapterConnection(this.vCenterAdapterDetails).subscribe(
        res => {
          this.isTestConnectionErrorMessage = false;
          this.isTestConnectionSuccessful = true;
          this.isLoading = false;
          this.testConnectionAlertMessage = MessageConstants.TEST_CONNECTION;
        },
        error => {
          this.testConnectionAlertMessage = error.error.description;
          this.isTestConnectionErrorMessage = true;
          this.isTestConnectionSuccessful = false;
          this.isLoading = false;
        }
      );
     }
  }
}
