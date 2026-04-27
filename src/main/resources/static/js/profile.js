function toggleProfileMenu() {
    const menu = document.getElementById("profileDropdown");
    menu.style.display = menu.style.display === "block" ? "none" : "block";
}

document.addEventListener("click", function (e) {
    const profileMenu = document.querySelector(".profile-menu");
    if (profileMenu && !profileMenu.contains(e.target)) {
        document.getElementById("profileDropdown").style.display = "none";
    }
});
