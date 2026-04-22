(function initLoginValidation() {
    const form = document.getElementById("loginForm");
    const username = document.getElementById("username");
    const password = document.getElementById("password");
    const error = document.getElementById("loginError");

    if (!form || !username || !password || !error) {
        return;
    }

    form.addEventListener("submit", function (event) {
        const userValue = username.value.trim();
        const passValue = password.value.trim();

        if (!userValue || !passValue) {
            event.preventDefault();
            error.textContent = "Debes completar usuario y contraseña.";
            return;
        }

        error.textContent = "";
    });
})();
