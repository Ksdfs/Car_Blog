// Add scroll event listener to change navbar background on scroll
document.addEventListener("DOMContentLoaded", () => {
  const mainNav = document.getElementById("mainNav")

  if (mainNav) {
    window.addEventListener("scroll", () => {
      if (window.scrollY > 20) {
        mainNav.classList.remove("navbar-transparent")
        mainNav.classList.add("navbar-dark", "shadow-sm")
      } else {
        mainNav.classList.remove("navbar-dark", "shadow-sm")
        mainNav.classList.add("navbar-transparent")
      }
    })
  }
})
