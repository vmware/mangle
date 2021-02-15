import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { ClarityModule } from '@clr/angular';
import { AzureRoutingModule } from './azure-routing.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { AzureVMComponent } from './virtualmachine/azure-vm.component';
import { AzureVMStateComponent } from './virtualmachine/state/azure-vm-state.component';
import { AzureVMNetworkComponent } from './virtualmachine/network/azure-vm-network.component';
import { AzureVMStorageComponent } from './virtualmachine/storage/azure-vm-storage.component';
@NgModule({
  declarations: [AzureVMComponent, AzureVMStateComponent, AzureVMNetworkComponent, AzureVMStorageComponent],
  imports: [
    FormsModule,
    HttpClientModule,
    ClarityModule,
    CommonModule,
    AzureRoutingModule,
    SharedModule
  ],
  providers: []
})

export class AzureModule {
}
