import {Component, OnInit} from '@angular/core';
import {SettingService} from '../setting.service';
import {ClrLoadingState} from '@clr/angular';

@Component({
  selector: 'app-cluster',
  templateUrl: './cluster.component.html',
  styleUrls: ['./cluster.component.css']
})
export class ClusterComponent implements OnInit {

  constructor(private settingService: SettingService) {
  }

  clusterEditModal = false;
  isEditQuorum = false;
  isEditDeployment = false;
  clusterEditModalTitle: string;


  public clusterConfig: any = {
    'id': null,
    'clusterName': null,
    'validationToken': null,
    'members': [],
    'quorum': null,
    'deploymentMode': null,
  };

  deploymentModes = ['CLUSTER', 'STANDALONE'];

  public clusterConfigEdit: any;

  public spinnerActive = false;

  public alertMessage: string;
  public isErrorMessage: boolean;

  public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  ngOnInit() {
    this.getCluster();
  }

  public getCluster() {
    this.spinnerActive = true;
    this.settingService.getCluster().subscribe(
      res => {
        delete res['_links'];
        this.clusterConfig = res;
        this.spinnerActive = false;
      }, err => {
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
        this.spinnerActive = false;
      });
  }

  onQuorumEditTrigger() {
    this.clusterConfigEdit = {...this.clusterConfig};
    this.isEditQuorum = true;
    this.isEditDeployment = false;
    this.clusterEditModal = true;
    this.clusterEditModalTitle = 'Edit Quorum';
  }

  onDeploymentEditTrigger() {
    this.clusterConfigEdit = {...this.clusterConfig};
    this.isEditQuorum = false;
    this.isEditDeployment = true;
    this.clusterEditModal = true;
    this.clusterEditModalTitle = 'Edit Deployment Mode';
  }


  updateClusterInfo(form) {
    if (form.dirty && form.valid) {
      this.spinnerActive = true;
      this.isEditQuorum ? this.updateQuorum() : this.updateDeploymentMode();
      form.reset();
      this.clusterEditModal = false;
    }
  }

  updateQuorum() {
    this.settingService.updateClusterQuorumValue(this.clusterConfigEdit.quorum).subscribe(
      res => {
        this.getCluster();
      },
      error => {
        this.isErrorMessage= true;
        this.alertMessage = error.error.description;
        this.spinnerActive = false;
      }
    );
  }

  updateDeploymentMode() {
    this.settingService.updateClusterDeploymentMode(this.clusterConfigEdit.deploymentMode).subscribe(
      res => {
        this.getCluster();
      },
      error => {
        this.isErrorMessage= true;
        this.alertMessage = error.error.description;
        this.spinnerActive = false;
      }
    );
  }

  cancelClusterEdition(form) {
    this.clusterEditModal = false;
    form.reset();
  }

}
