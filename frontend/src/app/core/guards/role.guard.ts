import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth';

export const roleGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  
  if (!authService.currentUser().isAuthenticated) {
    return router.parseUrl('/login');
  }

  const allowedRoles = route.data['roles'] as Array<string>;
  const userRole = authService.getRole();

  
  if (allowedRoles && allowedRoles.includes(userRole)) {
    return true;
  }

  
  if (userRole === 'GERENTE') {
    return router.parseUrl('/panel/carga');
  } else if (userRole === 'SUPERVISOR') {
    return router.parseUrl('/panel/gestion');
  }

  return router.parseUrl('/login');
};
