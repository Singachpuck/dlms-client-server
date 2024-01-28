import {inject} from "@angular/core";
import {TokenStorageService} from "../services/token-storage.service";
import {CanActivateFn, Router} from "@angular/router";

export function authGuard(): CanActivateFn  {
  return () => {
    let router: Router = inject(Router);
    let tokenValid: boolean = inject(TokenStorageService).isValid();
    return tokenValid ? tokenValid : router.createUrlTree(['/login']);
  }
}
