import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { EndpointComponent } from './endpoint.component';
import { MachineComponent } from './machine/machine.component';
import { KubernetesComponent } from './kubernetes/kubernetes.component';
import { DockerComponent } from './docker/docker.component';
import { VcenterComponent } from './vcenter/vcenter.component';
import { AWSComponent } from './aws/aws.component';
import { AzureComponent } from './azure/azure.component';
import { EndpointCredentialsComponent } from './credentials/endpoint-credentials.component';
import { EndpointCertificatesComponent } from './certificates/endpoint-certificates.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { VcenterAdapterComponent } from './vcenter-adapter/vcenter-adapter.component';
import { RedisProxyComponent } from './redis-proxy/redis-proxy.component';
import { DatabaseComponent } from './database/database.component';
import { RouterModule } from '@angular/router';
import { EndpointRoutingModule } from './endpoint-routing.module';
import { EndpointGroupsComponent } from './groups/endpoint-groups.component';

@NgModule({
  declarations: [EndpointComponent, EndpointGroupsComponent, MachineComponent, KubernetesComponent, DockerComponent, VcenterComponent,
    AWSComponent, EndpointCredentialsComponent, EndpointCertificatesComponent, AzureComponent, VcenterAdapterComponent, RedisProxyComponent, DatabaseComponent],
  imports: [
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    SharedModule,
    RouterModule,
    EndpointRoutingModule
  ]
})
export class EndpointModule {
}
