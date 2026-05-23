"use strict";

/**
 * Muestra un mensaje de error o estado en el login.
 */
function mostrarMensajeLogin(mensaje, esError = true) {
    const status = document.getElementById("loginStatus");
    const error = document.getElementById("loginError");

    const target = status || error;
    if (!target) {
        return;
    }

    target.textContent = mensaje;
    target.style.display = mensaje ? "block" : "none";
    target.classList.toggle("alert-error", esError);
    target.classList.toggle("alert-success", !esError);
}

/**
 * Ejecuta el login JWT contra el backend y gestiona validación, persistencia y navegación.
 */
async function iniciarSesionJwt(event) {
    event.preventDefault();

    const form = document.getElementById("loginForm");
    const username = document.getElementById("username");
    const password = document.getElementById("password");
    const button = form?.querySelector('button[type="submit"]');

    if (!form || !username || !password) {
        return;
    }

    const userValue = username.value.trim();
    const passValue = password.value.trim();

    if (!userValue || !passValue) {
        mostrarMensajeLogin("Debes completar usuario y contraseña.");
        return;
    }

    const endpoint = form.dataset.endpoint || "/api/auth/login";
    const body = {
        username: userValue,
        password: passValue
    };

    if (button) {
        button.disabled = true;
        button.textContent = "Ingresando...";
    }

    mostrarMensajeLogin("");

    try {
        const response = await fetch(endpoint, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            mostrarMensajeLogin("Usuario o contraseña incorrectos. Intenta nuevamente.");
            return;
        }

        const data = await response.json();
        guardarSesionJwt(data);
        mostrarMensajeLogin("Autenticación correcta. Redirigiendo...", false);
        redirigirPorRol(data.rol);
    } catch (error) {
        console.error("Error en login JWT:", error);
        mostrarMensajeLogin("No se pudo iniciar sesión. Revisa la conexión e intenta nuevamente.");
    } finally {
        if (button) {
            button.disabled = false;
            button.textContent = "Ingresar";
        }
    }
}

(function initLoginJwt() {
    const form = document.getElementById("loginForm");

    if (!form) {
        return;
    }

    form.addEventListener("submit", iniciarSesionJwt);
})();
