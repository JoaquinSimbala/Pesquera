"use strict";

document.addEventListener("DOMContentLoaded", () => {
    const TARIFAS = {};
    const rateItems = document.querySelectorAll(".rate-item[data-rol][data-tarifa]");
    for (const item of rateItems) {
        const rol = item.dataset.rol;
        const tarifaValue = (item.dataset.tarifa || "0").replace(",", ".");
        TARIFAS[rol] = parseFloat(tarifaValue) || 0;
    }

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

    function configurarLogout() {
        actualizarEtiquetaUsuarioSesion();
        conectarBotonCerrarSesion();
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
                credentials: "same-origin"
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
                const mensaje = await response.text();
                alert(mensaje || "No se pudo aprobar el pago.");
            }
        } catch (error) {
            console.error("Error en AJAX:", error);
            alert("No se pudo conectar con el servidor.");
        }
    }

    document.addEventListener("click", (event) => {
        const btnCalcular = event.target.closest(".btn-main-action");
        if (btnCalcular) {
            event.preventDefault();
            procesarCalculo(btnCalcular);
            return;
        }

        const btnModificar = event.target.closest(".btn-modify");
        if (btnModificar) {
            event.preventDefault();
            habilitarEdicion(btnModificar);
            return;
        }

        const btnAprobar = event.target.closest(".btn-aprobar-ajax");
        if (btnAprobar) {
            event.preventDefault();
            const id = btnAprobar.dataset.liquidacionId;
            if (id) {
                ejecutarAprobacionAjax(id, btnAprobar);
            }
        }
    });

    verificarFormularioCompleto();
    configurarLogout();
});
