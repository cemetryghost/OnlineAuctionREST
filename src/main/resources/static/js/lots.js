statusTranslations = {
    'AWAITING_CONFIRMATION_LOT': 'Ожидает подтверждения',
    'ACTIVE_LOT': 'Активный',
    'COMPLETED_LOT': 'Завершен'
};

var lots = [];
var pageType;
var userRole;


$(document).ready(function () {
    pageType = $('body').data('page-type');
    userRole = window.userRole || 'ROLE_NOT_SPECIFIED';

    initializeApp();

    $('#lotModal').on('hidden.bs.modal', clearLotForm);

    if (pageType === 'activeLots') {
        $('#categoryFilter').change(() => loadActiveLots($('#categoryFilter').val(), $('#searchKeyword').val().trim()));
        $('#searchButton').click(() => loadActiveLots($('#categoryFilter').val(), $('#searchKeyword').val().trim()));
    } else if (pageType === 'userBids') {
        loadUserBids();
    }
});

function clearLotForm() {
    $('#nameLots, #descriptionLots, #startPrice, #stepPrice, #closingDate, #conditionLots, #category, #lotImage').val('');
}

function initializeApp() {
    setupEventListeners();
    loadCategories();
    if (pageType === 'activeLots' && userRole === 'ROLE_BUYER') {
        loadActiveLots();
    } else if (userRole !== 'ROLE_BUYER') {
        loadLots();
    }
    setMinimumDate('#closingDate');
    setMinimumDate('#editLotClosingDate');
    if (userRole === 'ROLE_ADMIN') {
        $('#statusSelectContainer').show();
    }
    if (userRole === 'ROLE_SELLER') {
        $('#addLotButton').show();
    }
    loadMyCompletedLots();
}

function setMinimumDate(selector) {
    const date = new Date();
    date.setDate(date.getDate() + 3);
    const minDate = date.toISOString().split('T')[0];
    $(selector).attr('min', minDate).on('keydown paste', e => e.preventDefault());
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
        if (userRole === 'ROLE_BUYER') {
            const filterSelect = $('#categoryFilter');
            filterSelect.empty().append(new Option("Все категории", "", true, true));
            response.forEach(cat => filterSelect.append(new Option(cat.nameCategory, cat.id)));
        }
    } catch (error) {
        console.error("Ошибка загрузки категорий:", error);
    }
}

async function loadLots(statusLot = '', role = userRole) {
    let url;

    if (role === 'ROLE_ADMIN') {
        url = '/lots';
    } else if (role === 'ROLE_SELLER') {
        url = '/lots/my';
    } else if (role === 'ROLE_BUYER') {
        url = '/lots/active';
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
            lots.forEach(lot => container.append(createLotCard(lot, lots)));
        }
    } catch (error) {
        console.error("Ошибка загрузки лотов", error);
    }
}


function createLotCard(lot) {
    const imageUrl = lot.hasImage ? `/lots/${lot.id}/image?timestamp=${new Date().getTime()}` : 'https://localhost:8443/placeholder.jpg';
    const card = $(`
        <div class="col-md-4">
            <div class="card" data-lot-id="${lot.id}">
                <img src="${imageUrl}" class="card-img-top" alt="Изображение лота" onerror="this.onerror=null;this.src='https://localhost:8443/placeholder.jpg';">
                <div class="card-body">
                    <h5 class="card-title">${lot.nameLots}</h5>
                    <p class="card-text">${lot.descriptionLots}</p>
                    <p class="card-text">Статус: ${statusTranslations[lot.statusLots]}</p>
                    <p class="card-text">Текущая цена: ${lot.currentPrice !== undefined && lot.currentPrice !== null ? `${lot.currentPrice} руб.` : 'Ставки еще не сделаны'}</p>
                    <button class="btn btn-primary lot-details-button" data-lot-id="${lot.id}">Подробнее</button>
                </div>
            </div>
        </div>
    `);
    card.find('.btn-primary').on('click', () => showLotDetails(lot.id, lots));
    return card;
}

function showLotDetails(lotId, allLots, userBid = null) {
    const lot = Array.isArray(allLots) ? allLots.find(l => l.id === lotId) : null;
    if (!lot) {
        return;
    }

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
        } else if (userRole === 'ROLE_BUYER') {
            const $footer = $('#lotDetailsFooter');
            $footer.html(generateBuyerFooterButtons(lot, userBid));
            $('#placeBidButton').off('click').on('click', function() {
                placeBid(lot.id);
            });
            $('#increaseBidButton').off('click').on('click', function() {
                const bidId = $(this).data('bid-id');
                increaseBid(lot.id, bidId);
            });
        } else {
            $('#lotDetailsFooter').html(generateFooterButtons(lot));
        }

        $('#lotDetailsModal').modal('show');

        $('#lotDetailsModal').on('hidden.bs.modal', function () {
            if (!$('#confirmCompleteLotModal').hasClass('show')) {
                $('#lotDetailsModal').modal('hide');
            }
        });
    }).fail(function(jqXHR, textStatus, errorThrown) {
        console.error('Ошибка загрузки категории: ', textStatus, errorThrown);
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

function generateBuyerFooterButtons(lot, userBid) {
    if (lot.statusLots === 'COMPLETED_LOT') {
        return '';
    }
    if (userBid && userBid.bidAmount) {
        return `
            <hr>
            <p>Введите новую сумму ставки:</p>
            <input type="number" id="newBidAmount" class="form-control" placeholder="Введите новую сумму ставки">
            <button type="button" class="btn btn-secondary" id="increaseBidButton" data-bid-id="${userBid.id}">Повысить ставку</button>
        `;
    } else {
        return `
            <hr>
            <p>Введите сумму ставки:</p>
            <input type="number" id="bidAmount" class="form-control" placeholder="Введите сумму ставки">
            <button type="button" class="btn btn-primary" id="placeBidButton">Сделать ставку</button>
        `;
    }
}

function generateLotDetailsHtml(lot, categoryName) {
    let buyerInfo = "";
    if (lot.buyerDetails) {
        const buyerType = lot.statusLots === 'COMPLETED_LOT' ? "Победитель:" : "Текущий покупатель:";
        buyerInfo = `
            <p><strong>${buyerType}</strong></p>
            <p class="buyer-margin"><strong>Имя и фамилия:</strong> ${lot.buyerDetails.name} ${lot.buyerDetails.surname}</p>
            <p class="buyer-margin"><strong>Email:</strong> ${lot.buyerDetails.email}</p>
        `;
    } else {
        buyerInfo = `
            <p><strong>Текущий покупатель:</strong></p>
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

    const guaranteeText = userRole !== 'ROLE_ADMIN' ? `
        <div class="guarantee-text">
            <p>Для покупки/продажи, гарантом является Администратор платформы. Свяжитесь с ним по Email - admin_auction@mail.ru. В случае, если сделка минует Администратора, платформа не несет ответственности!</p>
        </div>
    ` : '';

    return `
        <img src="${lot.hasImage ? `/lots/${lot.id}/image` : '/placeholder.jpg'}" class="img-fluid mb-3" alt="Изображение лота">
        <p><strong>Наименование:</strong> ${lot.nameLots}</p>
        <p><strong>Категория:</strong> ${categoryName}</p>
        <p><strong>Описание:</strong> ${lot.descriptionLots}</p>
        <p><strong>Стартовая цена:</strong> ${lot.startPrice} руб.</p>
        <p><strong>Текущая цена:</strong> ${lot.currentPrice !== undefined && lot.currentPrice !== null ? `${lot.currentPrice} руб.` : 'Ставки еще не сделаны'}</p>
        <p><strong>Шаг цены:</strong> ${lot.stepPrice} руб. </p>
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
    let buttons = '';
    if (userRole === 'ROLE_SELLER' && lot.statusLots !== 'AWAITING_CONFIRMATION_LOT' && lot.statusLots !== 'COMPLETED_LOT') {
        buttons += '<button class="btn btn-primary" onclick="completeLot(' + lot.id + ')">Завершить лот</button>';
    }
    if (!lot.buyerDetails && (userRole === 'ROLE_SELLER' || userRole === 'ROLE_ADMIN')) {
        buttons += '<button class="btn btn-info" onclick="editLot(' + lot.id + ')">Редактировать</button>';
        buttons += '<button class="btn btn-danger" onclick="deleteLot(' + lot.id + ')">Удалить лот</button>';
    }
    return buttons;
}

function handleLotFormSubmit(event) {
    event.preventDefault();
    const startPrice = parseFloat($('#startPrice').val());
    const stepPrice = parseFloat($('#stepPrice').val());

    if (startPrice < 1 || stepPrice < 1) {
        alert('Стартовая и шаг цены должны быть больше 1');
        return;
    }

    const lotData = {
        nameLots: $('#nameLots').val(),
        descriptionLots: $('#descriptionLots').val(),
        startPrice: startPrice,
        stepPrice: stepPrice,
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
            $('#lotCreationSuccessModal').modal('show');
        },
        error: handleError
    });
}

function editLot(lotId) {
    const lot = lots.find(l => l.id === lotId);
    if (!lot) return;

    currentLotId = lotId;
    $('#lotDetailsModal').modal('hide');

    if (lot.buyerDetails) {
        $('#lotEditErrorModal').modal('show');
        $('#lotEditErrorModal').off('hidden.bs.modal').on('hidden.bs.modal', function () {
            showLotDetails(currentLotId, lots);
        });
        return;
    }

    $('#editLotName').val(lot.nameLots);
    $('#editLotDescription').val(lot.descriptionLots);
    $('#editLotStartPrice').val(lot.startPrice);
    $('#editLotStepPrice').val(lot.stepPrice);
    $('#editLotClosingDate').val(lot.closingDate);
    $('#editLotCondition').val(lot.conditionLots);
    $('#editLotModal').modal('show');

    $('#saveEditedLot').off('click').on('click', function() {
        const updatedLotData = {
            nameLots: $('#editLotName').val(),
            descriptionLots: $('#editLotDescription').val(),
            startPrice: $('#editLotStartPrice').val(),
            stepPrice: $('#editLotStepPrice').val(),
            closingDate: $('#editLotClosingDate').val(),
            conditionLots: $('#editLotCondition').val(),
        };
        updateLot(currentLotId, updatedLotData);
    });
}

function updateLot(lotId, updatedLotData) {
    const formData = new FormData();
    formData.append('lot', JSON.stringify(updatedLotData));
    const imageFile = $('#editLotImage')[0].files[0];
    if (imageFile) {
        formData.append('image', imageFile);
    }

    $.ajax({
        url: `/lots/${lotId}`,
        method: 'PUT',
        processData: false,
        contentType: false,
        data: formData,
        success: function() {
            $('#editLotModal').modal('hide');
            loadLots();
            $('#updateLotSuccessModal').modal('show');
        },
        error: function() {
            $('#editLotModal').modal('hide');
            $('#lotEditErrorModal').modal('show');
        }
    });
}

function deleteLot(lotId) {
    $('#lotDetailsModal').modal('hide');
    $('#deleteLotsModal').modal('show');

    $('#confirmDeleteButton').off('click').on('click', function() {
        $.ajax({
            url: `/lots/${lotId}`,
            method: 'DELETE',
            success: function() {
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
            $('#statusSuccessModal').modal('show');
        },
        error: handleError
    });
}

function handleError(jqxhr, textStatus, error) {
    console.error("Ошибка запроса:", textStatus, error);
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
            data.forEach(lot => lotsContainer.append(createLotCard(lot)));
        }
    }).fail(function(jqxhr, textStatus, error) {
        console.error("Ошибка загрузки лотов продавца:", textStatus, error);
    });
}

function placeBid(lotId) {
    const bidAmount = parseFloat($('#bidAmount').val());
    const lot = lots.find(l => l.id === lotId);

    if (!bidAmount || bidAmount < (lot.currentPrice || lot.startPrice) + lot.stepPrice) {
        showBidErrorModal(lot);
        return;
    }

    $.ajax({
        url: '/bids',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({ lotId: lotId, bidAmount: bidAmount }),
        success: function(response) {
            $('#lotDetailsModal').modal('hide');
            $('#bidConfirmationModal').modal('show');
            updateLotCard(response.lotDTO, response);
        },
        error: function(xhr, status, error) {
            console.error("Ошибка размещения ставки:", error);
            alert("Произошла ошибка при размещении ставки. Пожалуйста, попробуйте еще раз.");
        }
    });
}

function increaseBid(lotId, bidId) {
    const newBidAmount = parseFloat($('#newBidAmount').val());
    const lot = lots.find(l => (l.lotDTO ? l.lotDTO.id : l.id) === lotId);

    if (!lot) {
        console.error("Лот с таким id не найден:", lotId);
        return;
    }

    const actualLot = lot.lotDTO ? lot.lotDTO : lot;
    const currentPrice = actualLot.currentPrice !== undefined && actualLot.currentPrice !== null ? actualLot.currentPrice : actualLot.startPrice;

    if (!newBidAmount || newBidAmount < currentPrice + actualLot.stepPrice) {
        $('#lotDetailsModal').modal('hide');
        showBidErrorModal(actualLot);
        return;
    }

    $.ajax({
        url: `/bids/${bidId}/increase`,
        type: 'PUT',
        data: { newBidAmount: newBidAmount },
        success: function(response) {
            $('#lotDetailsModal').modal('hide');
            $('#bidConfirmationModal').modal('show');
            updateLotCard(response.lotDTO, response, 'userBids');
            if (pageType === 'userBids') {
                loadUserBids();
            }
        },
        error: function(xhr, status, error) {
            console.error("Ошибка повышения ставки:", error);
            $('#lotDetailsModal').modal('hide');
            showBidErrorModal(actualLot);
        }
    });
}


function showBidErrorModal(lot) {
    const minBidAmount = (lot.currentPrice || lot.startPrice) + lot.stepPrice;
    const currentPriceText = lot.currentPrice !== undefined && lot.currentPrice !== null ? `${lot.currentPrice} руб.` : 'Ставки еще не сделаны';

    $('#bidErrorModal .modal-body').html(`
        <p>Минимальная сумма ставки должна быть больше или равна ${minBidAmount} руб.</p>
        <p>Начальная цена: ${lot.startPrice} руб.</p>
        <p>Текущая цена: ${currentPriceText}</p>
        <p>Шаг цены: ${lot.stepPrice} руб.</p>
    `);

    $('#lotDetailsModal').modal('hide');

    $('#bidErrorModal').off('hidden.bs.modal').on('hidden.bs.modal', function () {
        $('#bidErrorModal').off('hidden.bs.modal');
        $('#lotDetailsModal').modal('show');
    });

    $('#bidErrorModal').modal('show');
}


function loadActiveLots(categoryId = '', keyword = '') {
    const url = '/lots/active/search';
    const params = { categoryId, keyword };

    $.getJSON(url, params)
        .done(function(allLots) {
            const lotsContainer = $('#lotsContainer').empty();
            if (allLots.length === 0) {
                $('#noLotsMessageBuyer').text('Лоты не найдены').show();
            } else {
                $('#noLotsMessageBuyer').hide();
                lots = allLots;
                allLots.forEach(lot => lotsContainer.append(createLotCard(lot)));
                $('.lot-details-button').click(function() {
                    const lotId = $(this).data('lot-id');
                    const lot = allLots.find(l => l.id === lotId);
                    $.getJSON('/bids/my', function(userBids) {
                        const userBid = userBids.find(bid => bid.lotId === lotId);
                        showLotDetails(lotId, allLots, userBid);
                    }).fail(function(jqXHR, textStatus, errorThrown) {
                        console.error('Ошибка загрузки ставок пользователя: ', textStatus, errorThrown);
                        showLotDetails(lotId, allLots, null);
                    });
                });
            }
        })
        .fail(function(jqXHR, textStatus, errorThrown) {
            console.error('Ошибка загрузки лотов: ', textStatus, errorThrown);
            $('#lotsContainer').empty().append('<p>Произошла ошибка при загрузке лотов!</p>');
        });
}

function loadUserBids() {
    $.getJSON('/bids/my', function(bids) {
        const userBidsContainer = $('#userBidsContainer').empty();
        lots = bids.map(bid => bid.lotDTO);
        if (bids.length === 0) {
            $('#noLotsMessageBuyerBids').text('Вы еще не принимали участие в торгах').show();
        } else {
            bids.forEach(bid => {
                userBidsContainer.append(createUserBidCard(bid));
            });
        }
    }).fail(function(jqXHR, textStatus, errorThrown) {
        console.error('Ошибка загрузки ставок: ', textStatus, errorThrown);
        $('#userBidsContainer').empty().append('<p>Произошла ошибка при загрузке ставок.</p>');
    });
}

function updateLotCard(updatedLot, userBid = null, pageType = '') {
    const card = $(`.lot-details-button[data-lot-id="${updatedLot.id}"]`).closest('.card');
    if (card.length === 0) {
        console.warn("Карточка с лотом не найдена:", updatedLot.id);
    } else {
        card.find('.card-title').text(updatedLot.nameLots);
        card.find('.card-text').each(function(index) {
            switch (index) {
                case 0:
                    $(this).text(updatedLot.descriptionLots);
                    break;
                case 1:
                    $(this).text('Статус: ' + statusTranslations[updatedLot.statusLots]);
                    break;
                case 2:
                    $(this).text('Текущая цена: ' + (updatedLot.currentPrice !== undefined && updatedLot.currentPrice !== null ? `${updatedLot.currentPrice} руб.` : 'Ставки еще не сделаны'));
                    break;
                case 3:
                    if (pageType === 'userBids' && userBid) {
                        $(this).text('Ваша ставка: ' + userBid.bidAmount);
                    } else {
                        $(this).remove();
                    }
                    break;
                default:
                    break;
            }
        });
        const imageUrl = updatedLot.hasImage ? `/lots/${updatedLot.id}/image?timestamp=${new Date().getTime()}` : 'placeholder.jpg';
        card.find('.card-img-top').attr('src', imageUrl);
    }
}



function createUserBidCard(bid, pageType = 'userBids') {
    const lot = bid.lotDTO;
    const card = $(`
        <div class="col-md-4">
            <div class="card" data-lot-id="${lot.id}">
                <img src="${lot.hasImage ? `/lots/${lot.id}/image` : 'placeholder.jpg'}" class="card-img-top" alt="Изображение лота">
                <div class="card-body">
                    <h5 class="card-title">${lot.nameLots}</h5>
                    <p class="card-text">${lot.descriptionLots}</p>
                    <p class="card-text">Текущая ставка: ${lot.currentPrice !== undefined && lot.currentPrice !== null ? `${lot.currentPrice} руб.` : 'Ставки еще не сделаны'}</p>
                    ${pageType === 'userBids' ? `<p class="card-text">Ваша ставка: ${bid.bidAmount}</p>` : ''}
                    <button class="btn btn-primary lot-details-button" data-lot-id="${lot.id}">Подробнее</button>
                </div>
            </div>
        </div>
    `);
    card.find('.btn-primary').on('click', function() {
        showLotDetails(lot.id, [lot], bid);
    });
    return card;
}





