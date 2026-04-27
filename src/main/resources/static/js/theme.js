function setToggleIcon() {
    const btn = document.getElementById("darkToggle");
    if (!btn) return;

    if (document.body.classList.contains("dark-mode")) {
        btn.innerText = "☀️";
    } else {
        btn.innerText = "🌙";
    }
}

function toggleDarkMode() {
    document.body.classList.toggle("dark-mode");

    if (document.body.classList.contains("dark-mode")) {
        localStorage.setItem("theme", "dark");
    } else {
        localStorage.setItem("theme", "light");
    }

    setToggleIcon();
}

window.onload = function () {
    const theme = localStorage.getItem("theme");
    if (theme === "dark") {
        document.body.classList.add("dark-mode");
    }
    setToggleIcon();
};
