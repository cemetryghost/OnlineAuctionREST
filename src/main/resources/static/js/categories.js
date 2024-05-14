$(document).ready(function() {

    function resetModal() {
        hideErrorMessages();
        $('#addNameCategory').val(''); // Сбрасываем значение поля ввода
        $('#editNameCategory').val(''); // Сбрасываем значение поля ввода
    }

    function hideErrorMessages() {
        $('#addCategoryError').hide();
        $('#editCategoryError').hide();
        $('#deleteCategoryError').hide();
    }

    $('.modal').on('hidden.bs.modal', function () {
        resetModal();
        $('body').removeClass('modal-open');
    });

    var selectedCategory = null;

    function refreshCategoryList() {
        $.ajax({
            url: '/category',
            method: 'GET',
            success: function(categories) {
                var tableBody = $('#categoriesTable tbody');
                tableBody.empty();
                categories.forEach(function(category) {
                    var row = $('<tr>').append($('<td>').text(category.nameCategory));
                    row.on('click', function() {
                        $('#categoriesTable tr').removeClass('table-primary');
                        $(this).addClass('table-primary');
                        selectedCategory = category;
                        $('#editButton').prop('disabled', false);
                        $('#deleteButton').prop('disabled', false);
                    });
                    tableBody.append(row);
                });
            }
        });
    }

    refreshCategoryList();

    $('#addCategoryForm').submit(function(e) {
        e.preventDefault();
        var categoryName = $('#addNameCategory').val();
        $.ajax({
            url: '/category',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ nameCategory: categoryName }),
            success: function() {
                $('#addCategoryModal').modal('hide');

                successMessage = "Категория успешно доавблена";
                $('#successMessage').text(successMessage);
                $('#successModal').modal('show');

                $('#addNameCategory').val('');
                hideErrorMessages();
                refreshCategoryList();
            },
            error: function(xhr) {
                if (xhr.status === 400) {
                    var errorResponse = JSON.parse(xhr.responseText);
                    var errorMessage = errorResponse.error;
                    $('#addCategoryError').text(errorMessage).show();
                } else {
                    alert("Произошла ошибка: " + xhr.statusText);
                }
            }
        });
    });

    $('#editButton').click(function() {
        hideErrorMessages(); // Скрываем сообщения об ошибках
        if(selectedCategory) {
            $('#editCategoryId').val(selectedCategory.id);
            $('#editNameCategory').val(selectedCategory.nameCategory);
            $('#editCategoryModal').modal('show');
        }
    });

    $('#deleteButton').click(function() {
        hideErrorMessages(); // Скрываем сообщения об ошибках
        if(selectedCategory) {
            $('#deleteCategoryModal').modal('show');
        }
    });

    $('#editCategoryForm').submit(function(e) {
        e.preventDefault();
        var categoryId = $('#editCategoryId').val();
        var categoryName = $('#editNameCategory').val();
        $.ajax({
            url: '/category/' + categoryId,
            method: 'PUT',
            contentType: 'application/json',
            data: JSON.stringify({ id: categoryId, nameCategory: categoryName }),
            success: function() {
                $('#editCategoryModal').modal('hide');
                successMessage = "Категория успешно изменена";
                $('#successMessage').text(successMessage);
                $('#successModal').modal('show');
                selectedCategory = null;
                $('#editButton').prop('disabled', true);
                $('#deleteButton').prop('disabled', true);
                $('#categoriesTable tr').removeClass('table-primary');
                refreshCategoryList();
            },
            error: function(xhr) {
                if (xhr.status === 400) {
                    var errorResponse = JSON.parse(xhr.responseText);
                    var errorMessage = errorResponse.error;
                    $('#editCategoryError').text(errorMessage).show();
                } else {
                    alert("Произошла ошибка: " + xhr.statusText);
                }
            }
        });
    });

    $('#confirmDeleteButton').click(function() {
        if(selectedCategory) {
            $.ajax({
                url: '/category/' + selectedCategory.id,
                method: 'DELETE',
                success: function() {
                    selectedCategory = null;
                    $('#editButton').prop('disabled', true);
                    $('#deleteButton').prop('disabled', true);
                    $('#categoriesTable tr').removeClass('table-primary');
                    refreshCategoryList();
                    $('#deleteCategoryModal').modal('hide');
                    successMessage = "Категория успешно удалена";
                    $('#successMessage').text(successMessage);
                    $('#successModal').modal('show');
                },
                error: function(xhr) {
                    var errorMessage = xhr.responseText || "Произошла неизвестная ошибка";
                    try {
                        var errorObj = JSON.parse(errorMessage);
                        if (errorObj && errorObj.error) {
                            errorMessage = errorObj.error;
                        }
                    } catch (e) {
                    }
                    $('#deleteCategoryError').text(errorMessage).show();
                }
            });
        }
    });
});
