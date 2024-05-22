$(document).ready(function() {
    $('#loginForm').submit(function(event) {
        event.preventDefault();

        const formData = {
            username: $('#username').val(),
            password: $('#password').val()
        };

        $.ajax({
            url: '/auth/login',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(formData),
            success: function(response) {
                window.location.href = getRedirectURLBasedOnRole(response.role);
            },
            error: function(xhr) {
                const errorMessage = $('#error-message');
                if (xhr.responseJSON && xhr.responseJSON.error) {
                    errorMessage.text(xhr.responseJSON.error).show();
                } else {
                    errorMessage.text("Неизвестная ошбика. Попробуйте позже").show();
                }
            }
        });
    });

    const togglePassword = $('#togglePassword');
    const password = $('#password');

    togglePassword.click(function() {
        const type = password.attr('type') === 'password' ? 'text' : 'password';
        password.attr('type', type);
        togglePassword.toggleClass('bi-eye bi-eye-slash');
    });
});

function getRedirectURLBasedOnRole(role) {
    switch(role) {
        case 'ROLE_ADMIN':
            return '/admin_dashboard';
        case 'ROLE_SELLER':
            return '/seller_dashboard';
        case 'ROLE_BUYER':
            return '/buyer_dashboard';
        default:
            return '/welcome';
    }
}
