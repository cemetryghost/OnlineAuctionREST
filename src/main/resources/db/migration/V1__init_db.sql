create table bid
(
    bid_amount numeric(38, 2) not null,
    buyer_id   bigint         not null,
    id_bids    bigserial      not null,
    lot_id     bigint         not null,
    primary key (id_bids)
);
create table category
(
    id_category   bigserial    not null,
    name_category varchar(255) not null unique,
    primary key (id_category)
);
create table lot
(
    closing_date     date           not null,
    current_price    numeric(38, 2),
    publication_date date           not null,
    start_price      numeric(38, 2) not null,
    step_price       numeric(38, 2) not null,
    category_id      bigint         not null,
    current_buyer_id bigint,
    id_lots          bigserial      not null,
    seller_id        bigint         not null,
    condition_lots   varchar(255)   not null,
    description_lots varchar(2048)  not null,
    name_lots        varchar(255)   not null,
    status_lots      varchar(255)   not null check (status_lots in
                                                    ('AWAITING_CONFIRMATION_LOT', 'ACTIVE_LOT', 'COMPLETED_LOT')),
    image            oid,
    primary key (id_lots)
);
create table users
(
    birth_date date         not null,
    id_users   bigserial    not null,
    email      varchar(255) not null unique,
    login      varchar(255) not null unique,
    name       varchar(255) not null,
    password   varchar(255) not null,
    role       varchar(255) not null check (role in ('SELLER', 'BUYER', 'ADMIN')),
    status     varchar(255) not null check (status in ('ACTIVE', 'BLOCKED')),
    surname    varchar(255) not null,
    primary key (id_users)
);

alter table if exists bid
    add constraint bid_buyer_fk foreign key (buyer_id) references users;
alter table if exists bid
    add constraint bid_lot_fk foreign key (lot_id) references lot;
alter table if exists lot
    add constraint lot_category_fk foreign key (category_id) references category;
alter table if exists lot
    add constraint lot_current_buyer_fk foreign key (current_buyer_id) references users;
alter table if exists lot
    add constraint lot_seller_fk foreign key (seller_id) references users;