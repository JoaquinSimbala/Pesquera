(function () {
    var rol = sessionStorage.getItem('pesquera_rol') || '';
    var usuario = sessionStorage.getItem('pesquera_usuario') || '';

    function showFor(selector, allowedRoles) {
        document.querySelectorAll(selector).forEach(function (el) {
            el.style.display = allowedRoles.indexOf(rol) !== -1 ? '' : 'none';
        });
    }

    showFor('.role-gerente', ['GERENTE']);
    showFor('.role-supervisor', ['SUPERVISOR']);

    var logoutBtn = document.getElementById('btnLogout');
    if (logoutBtn) {
        logoutBtn.style.display = (['GERENTE', 'SUPERVISOR'].indexOf(rol) !== -1) ? '' : 'none';
        if (typeof conectarBotonCerrarSesion === 'function') {
            conectarBotonCerrarSesion();
        } else {
            logoutBtn.addEventListener('click', function () {
                if (typeof cerrarSesionJwt === 'function') {
                    cerrarSesionJwt();
                    return;
                }
                sessionStorage.removeItem('pesquera_rol');
                sessionStorage.removeItem('pesquera_usuario');
                window.location.assign('/login');
            });
        }
    }

    var navUserLabel = document.getElementById('navUserLabel');
    if (navUserLabel) {
        if (rol === 'GERENTE') {
            navUserLabel.textContent = usuario ? (usuario + ' (' + rol + ')') : navUserLabel.textContent;
        } else {
            navUserLabel.textContent = usuario || navUserLabel.textContent;
        }
    }
})();
