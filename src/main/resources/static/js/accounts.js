$(document).ready(function() {
    var selectedUser = null;
    var selectedRow = null;

    function loadUsers() {
        $.ajax({
            url: '/user',
            method: 'GET',
            success: function(users) {
                var tableBody = $('#usersTableBody');
                tableBody.empty();
                users.forEach(function(user) {
                    var status = user.status === 'BLOCKED' ? 'Заблокирован' : 'Активный';
                    var role = '';
                    switch (user.role) {
                        case 'ADMIN':
                            role = 'Админ';
                            break;
                        case 'SELLER':
                            role = 'Продавец';
                            break;
                        case 'BUYER':
                            role = 'Покупатель';
                            break;
                        default:
                            role = user.role;
                            break;
                    }
                    var row = '<tr data-user-id="' + user.id + '">' +
                        '<td>' + user.name + '</td>' +
                        '<td>' + user.surname + '</td>' +
                        '<td>' + user.login + '</td>' +
                        '<td>' + user.email + '</td>' +
                        '<td>' + role + '</td>' +
                        '<td>' + status + '</td>' +
                        '</tr>';
                    tableBody.append(row);
                });
            }
        });
    }

    loadUsers();

    $(document).on('click', '#usersTableBody tr', function() {
        $('#usersTableBody tr').removeClass('table-primary');
        $(this).addClass('table-primary');
        selectedUser = $(this).data('user-id');
        selectedRow = $(this);
        $('#userActions button').prop('disabled', false);
    });

    function resetSelection() {
        selectedRow.removeClass('table-primary');
        selectedRow = null;
        $('#userActions button').prop('disabled', true);
    }

    window.deleteUser = function() {
        if (!selectedUser) return;
        $('#deleteUserModal').modal('show');
    };

    $('#confirmDeleteUser').click(function() {
        $.ajax({
            url: '/user/' + selectedUser,
            method: 'DELETE',
            success: function() {
                loadUsers();
                $('#deleteUserModal').modal('hide');
                resetSelection();
                successMessage = "Аккаунт успешно удален";
                $('#successMessage').text(successMessage);
                $('#successModalAccounts').modal('show');
            }
        });
    });

    window.blockUser = function() {
        if (!selectedUser) return;
        $('#blockActionText').text('Вы уверены, что хотите заблокировать этого пользователя?');
        $('#blockUserModal').modal('show');
        $('#confirmBlockUser').removeClass().addClass('btn btn-danger').text('Заблокировать');
    };

    window.unblockUser = function() {
        if (!selectedUser) return;
        $('#blockActionText').text('Вы уверены, что хотите разблокировать этого пользователя?');
        $('#blockUserModal').modal('show');
        $('#confirmBlockUser').removeClass().addClass('btn btn-success').text('Разблокировать');
    };

    $('#confirmBlockUser').click(function() {
        var action = $(this).text() === 'Заблокировать' ? 'block' : 'unblock';
        $.ajax({
            url: '/user/' + selectedUser + '/' + action,
            method: 'POST',
            success: function() {
                var statusCell = selectedRow.find('td:eq(5)');
                statusCell.text(action === 'block' ? 'Заблокирован' : 'Активный');
                $('#blockUserModal').modal('hide');
                resetSelection();
                var successMessage = "Аккаунт успешно " + (action === 'block' ? 'заблокирован' : 'разблокирован');
                $('#successMessage').text(successMessage);
                $('#successModalAccounts').modal('show');
            }
        });
    });
});