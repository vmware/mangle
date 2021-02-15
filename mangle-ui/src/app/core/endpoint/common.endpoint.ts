import { OnInit } from "@angular/core";
import { EndpointService } from "./endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { NgForm } from "@angular/forms";
import { MessageConstants } from "src/app/common/message.constants";
import { CommonConstants } from "src/app/common/common.constants";

export class CommonEndpoint implements OnInit {

  public epDatagrid = false;
  public epForm = true;
  public epType: string;
  public epFormData: any;
  public k8sFileToUpload: any;
  public keyFileToUpload: any;
  public dockerCaCertToUpload: any;
  public dockerServerCertToUpload: any;
  public dockerPrivateKeyToUpload: any;
  public tagsModal: boolean;

  public endpoints: any;
  public allEndpoints: any;
  public endpointGroups: any;
  public credentials: any;
  public certificates: any;
  public vCenterAdapters: any;
  public tagsData: any = {};
  public disabledResourceLabelsData: any = {};
  public searchedCredentials: any = [];
  public selectedEP: any = [];
  public selectedEPNames: any = [];
  public groupTypeEPNames: any = [];
  public supportedEndpointGroupTypes = [CommonConstants.MACHINE];

  public addDisabled = false;
  public editDisabled = true;
  public deleteDisabled = true;
  public enableDisabled = true;
  public disabledDisabled = true;

  public passwordHidden = true;
  public privateKeyHidden = true;

  public addEdit: string;

  public epGroupValidateMessage = MessageConstants.NOT_SELECTED_MINIMUM_ENDPOINTS;
  public alertMessage: string;
  public isErrorMessage: boolean;

  public authErrorFlag = false;
  public authAlertMessage: string;

  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public machineCredential = false;

  public isLoading = true;

  public testEndpointBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public disableSubmit = true;

  public awsCredentialModal = false;
  public vcenterCredentialModal = false;
  public k8sCredentialModal = false;
  public machineCredentialModal = false;
  public azureCredentialModal = false;
  public databaseCredentialModal = false;

  constructor(private endpointService: EndpointService, endpointType: string) {
    this.epType = endpointType;
  }

  ngOnInit() {
  }

  public populateEndpointForm(endpoint) {
    this.disableSubmit = true;
    if (endpoint.tags != null) {
      this.tagsData = endpoint.tags;
    } else {
      this.tagsData = {};
    }
    if (this.epType === CommonConstants.K8S_CLUSTER && endpoint.k8sConnectionProperties != null
      && endpoint.k8sConnectionProperties.disabledResourceLabels != null) {
      this.disabledResourceLabelsData = endpoint.k8sConnectionProperties.disabledResourceLabels;
    } else {
      this.disabledResourceLabelsData = {};
    }
    this.epFormData = endpoint;
    if (this.epType === CommonConstants.ENDPOINT_GROUP) {
      delete this.epFormData.credentialsName;
    }
  }

  public getCertificates() {
    this.endpointService.getCertificates().subscribe(
      res => {
        if (res.code) {
          this.certificates = [];
        } else {
          this.certificates = res.content;
        }
      }, err => {
        this.certificates = [];
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public getVCenterAdapters() {
    this.endpointService.getVCenterAdapterDetails().subscribe(res => {
      if (res && res.content) {
        this.vCenterAdapters = res.content;
      } else {
        this.vCenterAdapters = [];
      }
    }, err => {
      this.vCenterAdapters = [];
      this.isErrorMessage = true;
      this.alertMessage = err.error.description;
    });
  }

  public getEndpoints() {
    this.isLoading = true;
    this.endpointService.getEndpoints(this.epType).subscribe(
      res => {
        if (res.code) {
          this.endpoints = [];
          this.isLoading = false;
        } else {
          this.endpoints = res.content;
          this.isLoading = false;
        }
      }, err => {
        this.endpoints = [];
        this.isLoading = false;
        if (err.error.code === "FI506") {
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
        }
      }
    );
  }

  public getAllEndpoints() {
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        if (res.code) {
          this.allEndpoints = [];
        } else {
          this.allEndpoints = res.content;
        }
      }, err => {
        this.allEndpoints = [];
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public populateEndpointsOfGroupType(epGroupType) {
    this.groupTypeEPNames = [];
    this.isLoading = true;
    let selectedEndpoints = [];
    this.endpointService.getEndpoints(epGroupType).subscribe(
      res => {
        if (res.code) {
          selectedEndpoints = [];
          this.isLoading = false;
        } else {
          selectedEndpoints = res.content;
          for (let i = 0; i < selectedEndpoints.length; i++) {
            this.groupTypeEPNames.push(selectedEndpoints[i].name);
          }
          this.isLoading = false;
        }
      }, err => {
        selectedEndpoints = [];
        this.isLoading = false;
        if (err.error.code === "FI506") {
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
        }
      }
    );
  }

  public addOrUpdateEndpoint(endpoint) {
    if (this.tagsData !== {}) {
      endpoint.tags = this.tagsData;
      this.tagsData = {};
    }

    if (this.epType === CommonConstants.K8S_CLUSTER && endpoint.k8sConnectionProperties != null && this.disabledResourceLabelsData !== {}) {
      endpoint.k8sConnectionProperties.disabledResourceLabels = this.disabledResourceLabelsData;
      this.disabledResourceLabelsData = {};
    }

    this.testEndpointBtnState = ClrLoadingState.DEFAULT;
    if (endpoint.id == null) {
      this.addEndpoint(endpoint);
    } else {
      this.updateEndpoint(endpoint);
    }
  }

  public addEndpoint(endpoint) {
    delete endpoint["id"];
    this.isLoading = true;
    this.endpointService.addEndpoint(endpoint).subscribe(
      res => {
        this.getEndpoints();
        this.isErrorMessage = false;
        this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_ADD;
        this.isLoading = false;
      }, err => {
        this.getEndpoints();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public updateEndpoint(endpoint) {
    this.isLoading = true;
    this.endpointService.updateEndpoint(endpoint).subscribe(
      res => {
        this.getEndpoints();
        this.isErrorMessage = false;
        this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_UPDATE;
        this.isLoading = false;
      }, err => {
        this.getEndpoints();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public deleteEndpoint(endpoint) {
    this.selectedEPNames = [];
    if (endpoint !== undefined) {
      for (let i = 0; i < endpoint.length; i++) {
        this.selectedEPNames.push(endpoint[i].name);
      }
    } else {
      this.alertMessage = MessageConstants.NO_ENDPOINT_SELECTED;
    }
    if (confirm(MessageConstants.DELETE_CONFIRM + this.selectedEPNames + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.endpointService.deleteEndpoint(this.selectedEPNames).subscribe(
        res => {
          this.getEndpoints();
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.ENDPOINT_DELETE;
          this.isLoading = false;
        }, err => {
          this.getEndpoints();
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

  public getCredentials() {
    this.endpointService.getCredentials().subscribe(
      res => {
        if (res.code) {
          this.credentials = [];
        } else {
          this.credentials = res.content;
        }
      }, err => {
        this.credentials = [];
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public searchCredentials(searchKeyWord) {
    this.searchedCredentials = [];
    for (let i = 0; i < this.credentials.length; i++) {
      if (this.credentials[i].name.indexOf(searchKeyWord) > -1) {
        this.searchedCredentials.push(this.credentials[i]);
      }
    }
  }

  public setCredentialVal(credentialVal) {
    this.epFormData.credentialsName = credentialVal;
  }

  public setEndpointGroupType(endpointGroupType) {
    this.epFormData.endpointGroupType = endpointGroupType;
  }

  public setVCenterAdapterDetailsName(vcenterAdapterDetailsName) {
    this.epFormData.vcenterConnectionProperties.vcenterAdapterDetailsName = vcenterAdapterDetailsName;
  }

  public getPvtKeyFiles(fileToUploadEvent) {
    this.keyFileToUpload = fileToUploadEvent.target.files[0];
  }

  public addMachineCredential(credentialForm: NgForm) {
    const credential = credentialForm.value;
    this.authErrorFlag = false;
    this.submitBtnState = ClrLoadingState.LOADING;
    delete credential["id"];
    if (typeof this.keyFileToUpload !== undefined || (typeof credential.password !== undefined
      && credential.password !== "" && credential.password != null)) {
      this.endpointService.addRemoteMachineCredential(credential, this.keyFileToUpload).subscribe(
        res => {
          this.getCredentials();
          this.isErrorMessage = false;
          this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
          this.submitBtnState = ClrLoadingState.DEFAULT;
          this.machineCredential = false;
          this.epFormData.credentialsName = credential.name;
          credentialForm.reset();
        }, err => {
          this.getCredentials();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.submitBtnState = ClrLoadingState.DEFAULT;
          this.machineCredential = false;
          credentialForm.reset();
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    } else {
      this.authAlertMessage = MessageConstants.PASSWORD_OR_KEY_REQUIRED;
      this.authErrorFlag = true;
      this.submitBtnState = ClrLoadingState.DEFAULT;
      return;
    }
  }

  public getK8SFiles(fileToUploadEvent) {
    this.k8sFileToUpload = fileToUploadEvent.target.files[0];
  }

  public addKubernetesCredential(credential) {
    delete credential["id"];
    this.endpointService.addk8sCredential(credential, this.k8sFileToUpload).subscribe(
      res => {
        this.getCredentials();
        this.isErrorMessage = false;
        this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
        this.epFormData.credentialsName = credential.name;
      }, err => {
        this.getCredentials();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public addVcenterCredential(credential) {
    delete credential["id"];
    this.endpointService.addVcenterCredential(credential).subscribe(
      res => {
        this.getCredentials();
        this.isErrorMessage = false;
        this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
        this.epFormData.credentialsName = credential.name;
      }, err => {
        this.getCredentials();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public addAwsCredential(credential) {
    delete credential["id"];
    this.endpointService.addAwsCredential(credential).subscribe(
      res => {
        this.getCredentials();
        this.isErrorMessage = false;
        this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
        this.epFormData.credentialsName = credential.name;
      }, err => {
        this.getCredentials();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
      });
  }

  public getDockerCaCertFile(fileToUploadEvent) {
    this.dockerCaCertToUpload = fileToUploadEvent.target.files[0];
  }

  public getDockerServerCertFile(fileToUploadEvent) {
    this.dockerServerCertToUpload = fileToUploadEvent.target.files[0];
  }

  public getDockerPrivateKeyFile(fileToUploadEvent) {
    this.dockerPrivateKeyToUpload = fileToUploadEvent.target.files[0];
  }

  public addDockerCertificates(certificates) {
    delete certificates["id"];
    this.endpointService.addDockerCertificates(certificates, this.dockerCaCertToUpload, this.dockerServerCertToUpload,
      this.dockerPrivateKeyToUpload).subscribe(
        res => {
          this.getCertificates();
          this.isErrorMessage = false;
          this.alertMessage = certificates.name + MessageConstants.CERTIFICATES_ADD;
        }, err => {
          this.getCertificates();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
  }

  public closeCredentialForm(credentialFormMessage) {
    this.isErrorMessage = credentialFormMessage.isErrorMessage;
    this.alertMessage = credentialFormMessage.alertMessage;
    this.awsCredentialModal = false;
    this.vcenterCredentialModal = false;
    this.k8sCredentialModal = false;
    this.machineCredentialModal = false;
    this.azureCredentialModal = false;
    this.databaseCredentialModal = false;
    if (this.isErrorMessage != null && !this.isErrorMessage) {
      this.epFormData.credentialsName = credentialFormMessage.credentialData.name;
    }
  }

  public testEndpointConnection(isFormValid, endpoint) {
    this.alertMessage = null;
    if (isFormValid) {
      this.testEndpointBtnState = ClrLoadingState.LOADING;
      this.endpointService.testEndpointConnection(endpoint).subscribe(
        res => {
          if (res.code) {
            this.testEndpointBtnState = ClrLoadingState.ERROR;
            this.isErrorMessage = true;
            this.alertMessage = res.description;
          } else {
            this.testEndpointBtnState = ClrLoadingState.SUCCESS;
            this.disableSubmit = false;
            this.isErrorMessage = false;
            this.alertMessage = MessageConstants.TEST_CONNECTION;
          }
        }, err => {
          this.testEndpointBtnState = ClrLoadingState.ERROR;
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    }
  }

  public updateDisabledResourceLabels(disabledResourceLabelsVal) {
    this.disabledResourceLabelsData[disabledResourceLabelsVal.resourceLabelsKey] = disabledResourceLabelsVal.resourceLabelsValue;
  }

  public removeDisabledResourceLabels(resourceLabelsKeyToRemove) {
    delete this.disabledResourceLabelsData[resourceLabelsKeyToRemove];
  }

  public updateActionButtons() {
    if (this.selectedEP !== undefined) {
      this.addDisabled = this.selectedEP.length >= 1;
      this.editDisabled = this.selectedEP.length === 0 || this.selectedEP.length > 1;
      this.deleteDisabled = this.selectedEP.length === 0;
      let enabledCount = 0;
      let disabledCount = 0;
      for (let i = 0; i < this.selectedEP.length; i++) {
        if (this.selectedEP[i].enable == null || this.selectedEP[i].enable) {
          enabledCount++;
        } else {
          disabledCount++;
        }
      }
      if (enabledCount > 0 && disabledCount === 0) {
        this.disabledDisabled = false;
      } else if (disabledCount > 0 && enabledCount === 0) {
        this.enableDisabled = false;
      }
      if ((enabledCount === 0 && disabledCount === 0) || (enabledCount > 0 && disabledCount > 0)) {
        this.enableDisabled = true;
        this.disabledDisabled = true;
      }
    }
  }

  public enableEndpoints(selectedEndpoints, enableFlag: boolean) {
    const endPointNames = [];
    if (selectedEndpoints !== undefined) {
      for (let i = 0; i < selectedEndpoints.length; i++) {
        endPointNames.push(selectedEndpoints[i].name);
      }
    } else {
      this.alertMessage = MessageConstants.NO_ENDPOINT_SELECTED;
    }
    let confirmationMessage = MessageConstants.DISABLE_CONFIRM;
    if (enableFlag) {
      confirmationMessage = MessageConstants.ENABLE_CONFIRM;
    }
    if (confirm(confirmationMessage + endPointNames + MessageConstants.QUESTION_MARK)) {
      this.isLoading = true;
      this.endpointService.enableEndpoints(endPointNames, enableFlag).subscribe(
        res => {
          this.getEndpoints();
          this.isErrorMessage = false;
          this.alertMessage = MessageConstants.ENDPOINT_DISABLE;
          if (enableFlag) {
            this.alertMessage = MessageConstants.ENDPOINT_ENABLE;
          }
          this.isLoading = false;
        }, err => {
          this.getEndpoints();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
          if (this.alertMessage === undefined) {
            this.alertMessage = err.error.error;
          }
        });
    }
  }

}
