function togglePassword(id) {
    var input = document.getElementById(id);
    var icon = input.nextElementSibling;
    if (input.type === "password") {
        input.type = "text";
        icon.classList.add("bi-eye");
        icon.classList.remove("bi-eye-slash");
    } else {
        input.type = "password";
        icon.classList.remove("bi-eye");
        icon.classList.add("bi-eye-slash");
    }
}

$(document).ready(function() {
    flatpickr("#birth_date", {
        dateFormat: "Y-m-d",
        "locale": "ru",
        maxDate: "today",
        altInput: true,
        altFormat: "F j, Y",
        dateFormat: "Y-m-d",
        appendTo: document.getElementById('birth_date').parentNode
    });

    $('#registrationForm').on('submit', function(e) {
        e.preventDefault();

        var password = $('#password').val();
        var confirmPassword = $('#confirm_password').val();

        if (password !== confirmPassword) {
            $('#error-message').text('Пароли не совпадают!').show();
            return false;
        }

        var formData = {
            name: $('#name').val(),
            surname: $('#surname').val(),
            login: $('#login').val(),
            email: $('#email').val(),
            password: $('#password').val(),
            birth_date: $('#birth_date').val(),
            role: $('input[name="role"]:checked').val()
        };

        if (!formData.birth_date) {
            $('#error-message').text('Пожалуйста, выберите дату рождения').show();
            return;
        }
        if (!formData.email) {
            $('#error-message').text('Пожалуйста, введите ваш email').show();
            return;
        }
        $.ajax({
            type: 'POST',
            url: '/auth/register',
            data: JSON.stringify(formData),
            contentType: 'application/json',
            success: function(response) {
                $('#error-message').hide();
                $('#success-message').text('Регистрация прошла успешно! Подождите...').show();
                setTimeout(function() {
                    window.location.href = "registration.html";
                }, 3000);
            },
            error: function(xhr) {
                var errorMessage = xhr.responseJSON.error;
                $('#error-message').text(errorMessage).show();
            }
        });
    });
});