$(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const blocked = urlParams.get('blocked');

    const errorMessage = $('#error-message');
    const loginForm = $('#loginForm');
    const togglePassword = $('#togglePassword');
    const password = $('#password');

    if (error) {
        errorMessage.text("Неверный логин/email или пароль. Попробуйте снова.").show();
    }
    if (blocked) {
        errorMessage.text("Ваш аккаунт заблокирован, свяжитесь с администратором по эл. почте: admin123@gmail.com").show();
    }

    togglePassword.click(function() {
        const type = password.attr('type') === 'password' ? 'text' : 'password';
        password.attr('type', type);
        togglePassword.toggleClass('bi-eye bi-eye-slash');
    });
});
