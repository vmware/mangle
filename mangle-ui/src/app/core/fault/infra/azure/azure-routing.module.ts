import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AzureVMStateComponent } from './virtualmachine/state/azure-vm-state.component';
import { AzureVMNetworkComponent } from './virtualmachine/network/azure-vm-network.component';
import { AzureVMComponent } from './virtualmachine/azure-vm.component';
import { AzureVMStorageComponent } from './virtualmachine/storage/azure-vm-storage.component';


const routes: Routes = [
  {
    path: '', component: AzureVMComponent, children: [
      { path: '', redirectTo: 'vm-state', pathMatch: 'full'},
      { path: 'vm-state', component: AzureVMStateComponent },
      { path: 'vm-network', component: AzureVMNetworkComponent },
      { path: 'vm-storage', component: AzureVMStorageComponent }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class AzureRoutingModule {
}