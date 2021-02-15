import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AwsEC2StateComponent } from './ec2/state/aws-ec2-state.component';
import { AwsEC2NetworkComponent } from './ec2/network/aws-ec2-network.component';
import { AwsEC2StorageComponent } from './ec2/storage/aws-ec2-storage.component';
import { AwsEC2Component } from './ec2/aws-ec2.component';
import { AwsRDSComponent } from './rds/aws-rds.component';


const routes: Routes = [
  {
    path: '', component: AwsEC2Component, children: [
      { path: '', redirectTo: 'ec2-state', pathMatch: 'full'},
      { path: 'ec2-state', component: AwsEC2StateComponent },
      { path: 'ec2-network', component: AwsEC2NetworkComponent },
      { path: 'ec2-storage', component: AwsEC2StorageComponent }
    ]
  },
  { path: 'rds', component: AwsRDSComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class AwsRoutingModule {
}