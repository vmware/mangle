import { Component, OnInit } from "@angular/core";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { MessageConstants } from "src/app/common/message.constants";
import { CommonConstants } from "src/app/common/common.constants";

@Component({
  selector: "app-endpoint-credentials",
  templateUrl: "./endpoint-credentials.component.html"
})
export class EndpointCredentialsComponent implements OnInit {

  constructor(private endpointService: EndpointService) {
  }

  public credentials: any;
  public credentialFormData: any;

  public alertMessage: string;
  public isErrorMessage: boolean;

  public addEdit: string;

  public machineCredentialModal: boolean = false;
  public k8sCredentialModal: boolean = false;
  public vcenterCredentialModal: boolean = false;
  public awsCredentialModal: boolean = false;
  public azureCredentialModal: boolean = false;
  public databaseCredentialModal: boolean = false;

  public selectedCreds: any = [];
  public selectedCredNames: any = [];

  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public isLoading: boolean = true;

  ngOnInit() {
    this.getCredentials();
  }

  public populateCredentialForm(credentialData: any) {
    this.credentialFormData = credentialData;
  }

  public getCredentials() {
    this.isLoading = true;
    this.endpointService.getCredentials().subscribe(
      res => {
        if (res.code) {
          this.credentials = [];
          this.isLoading = false;
        } else {
          this.credentials = res.content;
          this.isLoading = false;
        }
      }, err => {
        this.credentials = [];
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public setEndpointTypeCredential(credentialVal: any) {
    if (credentialVal != undefined) {
      if (credentialVal.type == CommonConstants.MACHINE) {
        this.machineCredentialModal = true;
      }
      if (credentialVal.type == CommonConstants.K8S_CLUSTER) {
        this.k8sCredentialModal = true;
      }
      if (credentialVal.type == CommonConstants.VCENTER) {
        this.vcenterCredentialModal = true;
      }
      if (credentialVal.type == CommonConstants.AWS) {
        this.awsCredentialModal = true;
      }
      if (credentialVal.type == CommonConstants.AZURE) {
        this.azureCredentialModal = true;
      }
      if (credentialVal.type == CommonConstants.DATABASE) {
        this.databaseCredentialModal = true;
      }
    }
  }

  public closeCredentialForm(credentialFormMessage) {
    this.isErrorMessage = credentialFormMessage.isErrorMessage;
    this.alertMessage = credentialFormMessage.alertMessage;
    this.awsCredentialModal = false;
    this.azureCredentialModal = false;
    this.vcenterCredentialModal = false;
    this.k8sCredentialModal = false;
    this.machineCredentialModal = false;
    this.databaseCredentialModal = false;
    this.getCredentials();
  }

  public deleteCredential(credential) {
    this.selectedCredNames = [];
    if (credential != undefined) {
      for (var i = 0; i < credential.length; i++) {
        this.selectedCredNames.push(credential[i].name);
      }
    } else {
      this.alertMessage = MessageConstants.NO_CREDENTIAL_SELECTED;
    }
    if (confirm(MessageConstants.DELETE_CONFIRM + this.selectedCredNames + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.endpointService.deleteCredential(this.selectedCredNames).subscribe(
        res => {
          this.getCredentials();
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.CREDENTIAL_DELETE;
          this.isLoading = false;
        }, err => {
          this.getCredentials();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      // Do nothing!
    }
  }

}
