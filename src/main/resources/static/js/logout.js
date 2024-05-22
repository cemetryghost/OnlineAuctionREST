$(document).ready(function() {
    $('#logoutButton').click(function(event) {
        event.preventDefault();
        $.ajax({
            url: '/auth/logout',
            type: 'POST',
            success: function(response) {
                window.location.href = '/auth/login';
            },
            error: function(xhr, status, error) {
                console.log('Logout failed:', xhr, status, error);
            }
        });
    });
});