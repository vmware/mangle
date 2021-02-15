import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {HttpClientModule} from '@angular/common/http';
import {ClarityModule} from '@clr/angular';
import { AwsEC2Component } from './ec2/aws-ec2.component';
import { AwsEC2StateComponent } from "./ec2/state/aws-ec2-state.component";
import { AwsEC2NetworkComponent } from "./ec2/network/aws-ec2-network.component";
import { AwsEC2StorageComponent } from "./ec2/storage/aws-ec2-storage.component";
import { AwsRoutingModule } from './aws-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { AwsRDSComponent } from './rds/aws-rds.component';
@NgModule({
  declarations: [AwsEC2Component,AwsEC2NetworkComponent,AwsEC2StateComponent,AwsEC2StorageComponent,AwsRDSComponent],
  imports: [
    FormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    AwsRoutingModule,
    SharedModule
  ],
  providers: []
})

export class AwsModule {
}
