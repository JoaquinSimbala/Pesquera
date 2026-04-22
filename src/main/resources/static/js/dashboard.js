function validarRango(input) {
    if (!input) {
        return;
    }

    const rawValue = input.value;
    const value = Number(rawValue);
    const min = Number(input.min);
    const max = Number(input.max);

    if (!rawValue) {
        input.setCustomValidity("Este campo es obligatorio.");
        input.reportValidity();
        return;
    }

    if (Number.isNaN(value)) {
        input.setCustomValidity("Ingresa un valor numérico válido.");
        input.reportValidity();
        return;
    }

    if (value < min || value > max) {
        input.setCustomValidity("Valor fuera del rango permitido.");
        input.reportValidity();
        return;
    }

    input.setCustomValidity("");
    calcularTiempoEfectivo();
}

function calcularTiempoEfectivo() {
    const tiempoInput = document.getElementById("tiempoObjetivo");
    const preview = document.getElementById("tiempoEfectivo");
    const alerta = document.getElementById("alertaTiempo");

    if (!tiempoInput || !preview || !alerta) {
        return;
    }

    const tiempo = Number(tiempoInput.value);
    if (!tiempoInput.value || Number.isNaN(tiempo)) {
        preview.textContent = "Tiempo efectivo estimado: no calculado";
        alerta.textContent = "";
        return;
    }

    const tiempoEfectivo = tiempo - 1;
    preview.textContent = "Tiempo efectivo estimado: " + tiempoEfectivo.toFixed(1) + " h";

    if (tiempoEfectivo <= 0) {
        alerta.textContent = "El tiempo efectivo es menor o igual a 0. Ajusta el tiempo objetivo.";
    } else {
        alerta.textContent = "";
    }
}

function resaltarFilasEstado() {
    const rows = document.querySelectorAll("#tablaResultados tbody tr");
    rows.forEach(function (row) {
        const estado = row.querySelector("[data-estado]");
        if (!estado) {
            return;
        }

        if (estado.getAttribute("data-estado") === "insuficiente") {
            row.classList.add("alert-row");
        } else {
            row.classList.remove("alert-row");
        }
    });
}

(function initDashboard() {
    const form = document.getElementById("calculoForm");
    const kilosInput = document.getElementById("kilos");
    const tiempoInput = document.getElementById("tiempoObjetivo");

    if (kilosInput) {
        kilosInput.addEventListener("change", function () {
            validarRango(kilosInput);
        });
    }

    if (tiempoInput) {
        tiempoInput.addEventListener("change", function () {
            validarRango(tiempoInput);
        });
    }

    if (form) {
        form.addEventListener("submit", function () {
            calcularTiempoEfectivo();
        });
    }

    calcularTiempoEfectivo();
    resaltarFilasEstado();
})();
