"use strict";

const TARIFAS = {};
const rateItems = document.querySelectorAll(".rate-item[data-rol][data-tarifa]");
for (const item of rateItems) {
    const rol = item.dataset.rol;
    const tarifaValue = (item.dataset.tarifa || "0").replace(",", ".");
    TARIFAS[rol] = parseFloat(tarifaValue) || 0;
}

const CSRF_TOKEN = document.querySelector('meta[name="_csrf"]')?.content || "";
const CSRF_HEADER = document.querySelector('meta[name="_csrf_header"]')?.content || "X-CSRF-TOKEN";

function verificarFormularioCompleto() {
    const totalTarjetas = document.querySelectorAll(".worker-card").length;
    const calculadas = document.querySelectorAll(".worker-card.calculated").length;
    const btnReg = document.getElementById("btnRegistrarLote");
    const msg = document.getElementById("msgValidacion");

    if (!btnReg || !msg) {
        return;
    }

    if (totalTarjetas > 0 && calculadas === totalTarjetas) {
        btnReg.disabled = false;
        msg.style.display = "none";
    } else {
        btnReg.disabled = true;
        msg.style.display = "block";
    }
}

function procesarCalculo(btn) {
    const card = btn.closest(".worker-card");
    if (!card) {
        return;
    }

    const input = card.querySelector(".input-kilos");
    const pagoFinal = card.querySelector(".pago-final");
    const btnModificar = card.querySelector(".btn-modify");

    if (!input || !pagoFinal || !btnModificar) {
        return;
    }

    const kilos = parseFloat(input.value) || 0;
    const rol = input.getAttribute("data-rol");
    const tarifa = TARIFAS[rol] || 0;

    pagoFinal.textContent = `Total: S/ ${(kilos * tarifa).toFixed(2)}`;
    card.classList.add("calculated");
    btn.style.display = "none";
    btnModificar.style.display = "inline-block";

    verificarFormularioCompleto();
}

function habilitarEdicion(btn) {
    const card = btn.closest(".worker-card");
    if (!card) {
        return;
    }

    const btnCalcular = card.querySelector(".btn-main-action");
    if (!btnCalcular) {
        return;
    }

    card.classList.remove("calculated");
    btn.style.display = "none";
    btnCalcular.style.display = "inline-block";

    verificarFormularioCompleto();
}

async function ejecutarAprobacionAjax(id, btn) {
    try {
        const response = await fetch(`/gerente/liquidaciones/${id}/aprobar`, {
            method: "POST",
            headers: { [CSRF_HEADER]: CSRF_TOKEN }
        });

        if (response.ok) {
            const row = document.getElementById(`row-${id}`);
            const statusCell = row?.querySelector(".status-cell");
            if (statusCell) {
                statusCell.textContent = "Aprobado";
                statusCell.classList.remove("status-warn");
                statusCell.classList.add("status-ok");
            }
            btn.remove();
        } else {
            alert("No se pudo aprobar el pago.");
        }
    } catch (error) {
        console.error("Error en AJAX:", error);
    }
}

for (const btn of document.querySelectorAll(".btn-main-action")) {
    btn.addEventListener("click", () => procesarCalculo(btn));
}

for (const btn of document.querySelectorAll(".btn-modify")) {
    btn.addEventListener("click", () => habilitarEdicion(btn));
}

for (const btn of document.querySelectorAll(".btn-aprobar-ajax")) {
    btn.addEventListener("click", () => {
        const id = btn.dataset.liquidacionId;
        if (id) {
            ejecutarAprobacionAjax(id, btn);
        }
    });
}

verificarFormularioCompleto();
