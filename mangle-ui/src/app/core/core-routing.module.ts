import {NgModule} from "@angular/core";
import {RouterModule, Routes} from "@angular/router";
import {HomeComponent} from "./home/home.component";
import {PasswordComponent} from "./password/password.component";
import {PrivilegeGuardService} from "../privilege-guard.service";
import {CoreComponent} from "./core.component";


const routes: Routes = [
  {
    path: "", component: CoreComponent, children: [
      {
        path: "home", component: HomeComponent
      },
      {
        path: "password", component: PasswordComponent
      },
      {
        path: "requests",
        loadChildren: "./requests/requests.module#RequestsModule"
      },
      {
        path: "endpoint",
        loadChildren: "./endpoint/endpoint.module#EndpointModule"
      },
      {
        path: "fault",
        loadChildren: "./fault/fault.module#FaultModule"
      },
      {
        path: "resiliencyscore",
        loadChildren: "./resiliencyscore/resiliencyscore.module#ResiliencyscoreModule"
      },
      {
        path: "setting",
        canActivate: [PrivilegeGuardService],
        loadChildren: "./setting/setting.module#SettingModule"
      },
      {
        path: "", redirectTo: "home", pathMatch: "prefix"
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})

export class CoreRoutingModule {
}
