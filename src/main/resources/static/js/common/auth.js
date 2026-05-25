"use strict";

const JWT_ROLE_KEY = "pesquera_rol";
const JWT_USER_KEY = "pesquera_usuario";

function obtenerRolJwt() {
    return sessionStorage.getItem(JWT_ROLE_KEY) || "";
}

function obtenerUsuarioJwt() {
    return sessionStorage.getItem(JWT_USER_KEY) || "";
}

function guardarSesionJwt(respuesta) {
    sessionStorage.setItem(JWT_ROLE_KEY, respuesta?.rol || "");
    sessionStorage.setItem(JWT_USER_KEY, respuesta?.username || "");
}

function limpiarSesionJwt() {
    sessionStorage.removeItem(JWT_ROLE_KEY);
    sessionStorage.removeItem(JWT_USER_KEY);
}

function actualizarEtiquetaUsuarioSesion() {
    const navUserLabel = document.getElementById("navUserLabel");

    if (!navUserLabel) {
        return;
    }

    const usuario = obtenerUsuarioJwt();
    const rol = obtenerRolJwt();

    if (usuario) {
        navUserLabel.textContent = rol ? `${usuario} (${rol})` : usuario;
    }
}

function conectarBotonCerrarSesion() {
    const logoutButton = document.getElementById("btnLogout");

    if (!logoutButton) {
        return;
    }

    logoutButton.addEventListener("click", function () {
        cerrarSesionJwt();
    });
}

function redirigirPorRol(rol) {
    if (rol === "GERENTE") {
        window.location.assign("/gerente");
        return;
    }

    if (rol === "SUPERVISOR") {
        window.location.assign("/supervisor");
        return;
    }

    window.location.assign("/login");
}

async function cerrarSesionJwt() {
    try {
        await fetch("/api/auth/logout", {
            method: "POST",
            credentials: "same-origin"
        });
    } catch (error) {
        console.debug("No se pudo invalidar la cookie HttpOnly en el backend.", error);
    } finally {
        limpiarSesionJwt();
        window.location.assign("/login");
    }
}