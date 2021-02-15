import {NgModule} from "@angular/core";
import {PreloadAllModules, RouterModule, Routes} from "@angular/router";
import {ConfigGuardService} from "./shared/guards/config-guard.service";
import {ConfigComponent} from "./config/config.component";
import {UnavailableComponent} from "./pages/unavailable.component";
import {AuthGuardService} from "./shared/guards/auth-guard.service";
import {LoginGuardService} from "./shared/guards/login-guard.service";
import {UnavailableGuardService} from "./shared/guards/unavailable-guard.service";

const routes: Routes = [
  {
    path: "config",
    canActivate: [ConfigGuardService],
    component: ConfigComponent
  },
  {
    path: "unavailable",
    canActivate: [UnavailableGuardService],
    component: UnavailableComponent
  },
  {
    path: "login",
    canActivate: [LoginGuardService],
    loadChildren: "./auth/auth.module#AuthModule"
  },
  {
    path: "core",
    canActivate: [AuthGuardService],
    loadChildren: "./core/core.module#CoreModule"
  },
  {path: "", redirectTo: "core", pathMatch: "prefix"},
  {path: "**", redirectTo: "core"}
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    preloadingStrategy: PreloadAllModules
  })],
  exports: [RouterModule],
  providers: []
})
export class AppRoutingModule {
}
