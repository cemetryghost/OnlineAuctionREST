$(document).ready(function() {
    const urlParams = new URLSearchParams(window.location.search);
    const error = urlParams.get('error');
    const blocked = urlParams.get('blocked');
    const unconfirmed = urlParams.get('unconfirmed');
    const errorMessage = $('#error-message');
    const togglePassword = $('#togglePassword');
    const password = $('#password');

    $('#loginForm');

    if (error) {
        errorMessage.text("Неверный логин/email или пароль. Попробуйте снова.").show();
    }
    if (blocked) {
        errorMessage.text("Ваш аккаунт заблокирован, свяжитесь с администратором по эл. почте: admin_auction@gmail.com").show();
    }
    if (unconfirmed) {
        errorMessage.text("При регистрации вы не ввели код подтверждения!").show();
    }

    togglePassword.click(function() {
        const type = password.attr('type') === 'password' ? 'text' : 'password';
        password.attr('type', type);
        togglePassword.toggleClass('bi-eye bi-eye-slash');
    });
});
