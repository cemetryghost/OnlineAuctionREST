var lots = [];

statusTranslations = {
    'AWAITING_CONFIRMATION_LOT': 'Ожидает подтверждения',
    'ACTIVE_LOT': 'Активный',
    'COMPLETED_LOT': 'Завершен'
};

$(document).ready(function () {
    initializeApp();
});

function initializeApp() {
    setupEventListeners();
    loadCategories();
    loadLots();
    loadMyCompletedLots();
    if (userRole === 'ROLE_ADMIN') {
        $('#statusSelectContainer').show(); // Выпадающий список для администратора
    }
    if (userRole === 'ROLE_SELLER') {
        $('#addLotButton').show(); // Кнопка добавления лота только для продавцов
    }
}

function setupEventListeners() {
    $('#statusSelect').off('change').change(() => loadLots($('#statusSelect').val()));
    $('#addLotButton').off('click').click(() => $('#lotModal').modal('show'));
    $(document).off('submit', '#lotModal form').on('submit', '#lotModal form', handleLotFormSubmit);
}

async function loadCategories() {
    try {
        const response = await $.getJSON('/category');
        const categorySelect = $('#category');
        categorySelect.empty().append(new Option("Выберите категорию", "", true, true));
        response.forEach(cat => categorySelect.append(new Option(cat.nameCategory, cat.id)));
    } catch (error) {
        console.error("Error loading categories:", error);
    }
}

async function loadLots(statusLot = '', role = userRole) {
    let url;

    if (role === 'ROLE_ADMIN') {
        url = '/lots';
    } else if (role === 'ROLE_SELLER') {
        url = '/lots/my';
    }
    try {
        let data = await $.getJSON(url + (statusLot ? `?statusLot=${statusLot}` : ''));

        if (role === 'ROLE_SELLER') {
            data = data.filter(lot => lot.statusLots !== 'COMPLETED_LOT');
        }
        lots = data;
        const container = $('#lotsContainer').empty();

        if (data.length === 0 && role === 'ROLE_SELLER') {
            $('#noLotsMessage').text('У вас еще нет лотов (Активных/Ожидающих подтверждения)').show();
        } else if (data.length === 0 && role === 'ROLE_ADMIN') {
            $('#noLotsMessage').text('Лоты отсутствуют на платформе, либо в выбранной категории').show();
        } else {
            $('#noLotsMessage').hide();
            lots.forEach(lot => container.append(createLotCard(lot)));
        }
    } catch (error) {
        console.error("Error loading lots:", error);
    }
}

function createLotCard(lot) {
    var card = $(`
        <div class="col-md-4">
            <div class="card" data-lot-id="${lot.id}">
                <img src="${lot.hasImage ? `/lots/${lot.id}/image` : 'placeholder.jpg'}" class="card-img-top" alt="Изображение лота">
                <div class="card-body">
                    <h5 class="card-title">${lot.nameLots}</h5>
                    <p class="card-text">${lot.descriptionLots}</p>
                    <p class="card-text">Статус: ${statusTranslations[lot.statusLots]}</p>
                    <p class="card-text">Текущая цена: ${lot.currentPrice || 'Ставки еще не сделаны'}</p>
                    <button class="btn btn-primary">Подробнее</button>
                </div>
            </div>
        </div>
    `);
    card.find('.btn-primary').on('click', function() {
        showLotDetails(lot.id);
    });
    return card;
}

function showLotDetails(lotId) {
    const lot = lots.find(l => l.id === lotId);
    if (!lot) return;
    $.getJSON(`/category/${lot.categoryId}`, function(category) {
        $('#lotDetailsBody').html(generateLotDetailsHtml(lot, category.nameCategory));
        if (userRole === 'ROLE_ADMIN') {
            const $footer = $('#lotDetailsFooter');
            $footer.html(generateAdminFooterButtons(lot));
            const $newStatusSelect = $('#newStatusSelect');
            const $changeStatusButton = $('#changeStatusButton');
            $newStatusSelect.change(function() {
                if ($(this).val() === '') {
                    $changeStatusButton.prop('disabled', true);
                } else {
                    $changeStatusButton.prop('disabled', false);
                }
            });
            $newStatusSelect.val('');
            $changeStatusButton.prop('disabled', true);
        } else {
            $('#lotDetailsFooter').html(generateFooterButtons(lot));
        }
        $('#lotDetailsModal').modal('show');

        $('#lotDetailsModal').on('hidden.bs.modal', function () {
            if (!$('#confirmCompleteLotModal').hasClass('show')) {
                $('#lotDetailsModal').modal('hide');
            }
        });
    });
}

function generateAdminFooterButtons(lot) {
    return `
        <select class="custom-select" id="newStatusSelect" style="margin-right: 10px;">
            <option value="">Выберите статус</option>
            <option value="AWAITING_CONFIRMATION_LOT">Ожидает подтверждения</option>
            <option value="ACTIVE_LOT">Активный</option>
            <option value="COMPLETED_LOT">Завершен</option>
        </select>
        <button id="changeStatusButton" class="btn btn-success" onclick="changeLotStatus(${lot.id}, $('#newStatusSelect').val())">Изменить статус</button>
        ${generateFooterButtons(lot)}
    `;
}

function generateLotDetailsHtml(lot, categoryName, role = userRole) {
    console.log(userRole);
    let buyerInfo = "";
    let buyerType = "";

    if ((lot.statusLots === 'ACTIVE_LOT' || lot.statusLots === 'AWAITING_CONFIRMATION_LOT') && lot.buyerDetails) {
        buyerType = "Текущий покупатель:";
        buyerInfo = `
            <p><strong>${buyerType}</strong></p>
            <p class="buyer-margin"><strong>Имя и фамилия:</strong> ${lot.buyerDetails.name} ${lot.buyerDetails.surname}</p>
            <p class="buyer-margin"><strong>Email:</strong> ${lot.buyerDetails.email}</p>
        `;
    } else if (lot.statusLots === 'COMPLETED_LOT' && lot.buyerDetails) {
        buyerType = "Победитель";
        buyerInfo = `
            <p><strong>${buyerType}</strong></p>
            <p class="buyer-margin"><strong>Имя и фамилия:</strong> ${lot.buyerDetails.name} ${lot.buyerDetails.surname}</p>
            <p class="buyer-margin"><strong>Email:</strong> ${lot.buyerDetails.email}</p>
        `;
    } else {
        buyerType = "Текущий покупатель: ";
        buyerInfo = `
            <p><strong>${buyerType}</strong></p>
            <p class="buyer-margin">Отсутствует</p>
        `;
    }

    const sellerInfo = `
        <div class="seller-info">
            <p><strong>Продавец:</strong></p>
            <p class="seller-margin"><strong>Имя и фамилия:</strong> ${lot.sellerDetails.name} ${lot.sellerDetails.surname}</p>
            <p class="seller-margin"><strong>Email:</strong> ${lot.sellerDetails.email}</p>
        </div>
    `;

    const guaranteeText = (role !== 'ROLE_ADMIN') ? `
    <div class="guarantee-text">
        <p>Для покупки/продажи, гарантом является Администратор платформы. Свяжитесь с ним по Email - admin_auction@mail.ru. В случае, если сделка минует Администратора, платформа не несет ответственности!</p>
    </div>
` : '';


    return `
        <img src="${lot.hasImage ? `/lots/${lot.id}/image` : '/placeholder.jpg'}" class="img-fluid mb-3" alt="Изображение лота">
        <p><strong>Наименование:</strong> ${lot.nameLots}</p>
        <p><strong>Категория:</strong> ${categoryName}</p>
        <p><strong>Описание:</strong> ${lot.descriptionLots}</p>
        <p><strong>Стартовая цена:</strong> ${lot.startPrice}</p>
        <p><strong>Текущая цена:</strong> ${lot.currentPrice || 'Ставки еще не сделаны'}</p>
        <p><strong>Шаг цены:</strong> ${lot.stepPrice}</p>
        <p><strong>Дата публикации:</strong> ${lot.publicationDate}</p>
        <p><strong>Дата закрытия:</strong> ${lot.closingDate}</p>
        <p><strong>Состояние:</strong> ${lot.conditionLots}</p>
        <p><strong>Статус:</strong> ${statusTranslations[lot.statusLots]}</p>
        ${sellerInfo}
        ${buyerInfo}
        ${guaranteeText}
    `;
}


function generateFooterButtons(lot) {
    let buttons = '<button class="btn btn-info" onclick="editLot(' + lot.id + ')">Редактировать</button>' +
        '<button class="btn btn-danger" onclick="deleteLot(' + lot.id + ')">Удалить лот</button>';
    if (userRole === 'ROLE_SELLER' && lot.statusLots !== 'AWAITING_CONFIRMATION_LOT' && lot.statusLots !== 'COMPLETED_LOT') {
        buttons += '<button class="btn btn-primary" onclick="completeLot(' + lot.id + ')">Завершить лот</button>';
    }
    return buttons;
}

function handleLotFormSubmit(event) {
    event.preventDefault();

    const lotData = {
        nameLots: $('#nameLots').val(),
        descriptionLots: $('#descriptionLots').val(),
        startPrice: $('#startPrice').val(),
        stepPrice: $('#stepPrice').val(),
        closingDate: $('#closingDate').val(),
        conditionLots: $('#conditionLots').val(),
        categoryId: $('#category').val()
    };

    const formData = new FormData();
    formData.append('lot', JSON.stringify(lotData));

    const imageFile = $('#lotImage')[0].files[0];
    if (imageFile) {
        formData.append('image', imageFile);
    }

    $.ajax({
        url: '/lots',
        type: 'POST',
        processData: false,
        contentType: false,
        data: formData,
        success: function() {
            $('#lotModal').modal('hide');
            loadLots();
        },
        error: handleError
    });
}

function editLot(lotId) {
    const lot = lots.find(l => l.id === lotId);
    if (!lot) return;

    currentLotId = lotId;

    $('#lotDetailsModal').modal('hide');

    // Заполнение полей формы данными из лота
    $('#editLotName').val(lot.nameLots);
    $('#editLotDescription').val(lot.descriptionLots);
    $('#editLotStartPrice').val(lot.startPrice);
    $('#editLotStepPrice').val(lot.stepPrice);
    $('#editLotClosingDate').val(lot.closingDate);
    $('#editLotCondition').val(lot.conditionLots);

    $('#editLotModal').modal('show');

    $('#editLotModal').on('hidden.bs.modal', function () {
        if (!$('#updateLotSuccessModal').hasClass('show')) {
            showLotDetails(currentLotId);
        }
    });
    $('#saveEditedLot').off('click').on('click', function() {
        if ($('#editLotName').val() === '' || $('#editLotDescription').val() === '' || $('#editLotStartPrice').val() === '' || $('#editLotStepPrice').val() === '' || $('#editLotClosingDate').val() === '' || $('#editLotCondition').val() === '') {
            $('#editLotValidationError').text('Все поля должны быть заполнены').show(); // Отображаем сообщение об ошибке
            return;
        } else {
            $('#editLotValidationError').hide(); // Скрываем сообщение об ошибке, если оно было отображено ранее
        }

        const updatedLotData = {
            nameLots: $('#editLotName').val(),
            descriptionLots: $('#editLotDescription').val(),
            startPrice: $('#editLotStartPrice').val(),
            stepPrice: $('#editLotStepPrice').val(),
            closingDate: $('#editLotClosingDate').val(),
            conditionLots: $('#editLotCondition').val(),
        };
        updateLot(currentLotId, updatedLotData); // Используем сохраненный ID для обновления лота
    });
}


function updateLot(lotId, updatedLotData) {
    const formData = new FormData();
    formData.append('lot', JSON.stringify(updatedLotData)); // Добавляем параметр lot
    const imageFile = $('#editLotImage')[0].files[0];
    if (imageFile) {
        formData.append('image', imageFile);
    }
    console.log("FormData:", formData); // Добавляем эту строку для отладки
    $.ajax({
        url: `/lots/${lotId}`,
        method: 'PUT',
        processData: false,
        contentType: false,
        data: formData, // Передаем объект FormData
        success: function() {
            $('#editLotModal').modal('hide');
            loadLots();
            $('#updateLotSuccessModal').modal('show').on('hidden.bs.modal', function () {
                showLotDetails(currentLotId);
            });
        },
        error: function(jqxhr, textStatus, error) {
            $('#editLotModal').modal('hide');
            $('#lotEditErrorModal').modal('show')
        }
    });
}

// TODO: Модальные окна для редактрования и удаления, если есть покупатель

function deleteLot(lotId) {
    $('#lotDetailsModal').modal('hide');
    $('#deleteLotsModal').modal('show');

    $('#confirmDeleteButton').off('click').on('click', function() {
        $.ajax({
            url: `/lots/${lotId}`,
            method: 'DELETE',
            success: function() {
                // Закрываем все модальные окна после успешного удаления лота
                $('#deleteLotsModal').modal('hide');
                $('#deleteLotSuccessModal').modal('show');
                loadLots();
            },
            error: function() {
                $('#deleteCategoryError').text('Невозможно удалить лот, так как на него уже есть текущий покупатель').show();
            }
        });
    });
}

function completeLot(lotId) {
    $('#lotDetailsModal').modal('hide');
    $('#confirmCompleteLotModal').modal('show');
    $('#confirmCompleteLot').off('click').on('click', function() {
        $.ajax({
            url: `/lots/${lotId}/status`,
            method: 'PUT',
            data: { newStatus: 'COMPLETED_LOT' },
            success: function() {
                $('#confirmCompleteLotModal').modal('hide');
                $('#lotSuccessCompleted').modal('show');
                loadLots();
            },
            error: handleError
        });
    });
}

function changeLotStatus(lotId, newStatus) {
    $.ajax({
        url: `/lots/${lotId}/status`,
        method: 'PUT',
        data: { newStatus: newStatus },
        success: function() {
            $('#lotDetailsModal').modal('hide');
            loadLots();
            $('#statusSuccessModal').modal('show'); // Показываем модальное окно об успешном изменении статуса лота
        },
        error: handleError
    });
}

function handleError(jqxhr, textStatus, error) {
    console.error("Request Failed:", textStatus, error);
    $('#errorMessage').text('Произошла ошибка: ' + textStatus + ' ' + error);
    $('#errorModal').modal('show');
}

function loadMyCompletedLots() {
    $.getJSON('/lots/my/completed', function (data) {
        lots = data;
        const lotsContainer = $('#lotsContainer');
        lotsContainer.empty();
        if (data.length === 0) {
            $('#noLotsMessageCompleted').text('У вас еще нет завершенных лотов').show();
        } else {
            data.forEach(function(lot) {
                lotsContainer.append(createLotCard(lot));
            });
        }
    }).fail(function(jqxhr, textStatus, error) {
        console.error("Failed to load completed lots:", textStatus, error);
    });
}

