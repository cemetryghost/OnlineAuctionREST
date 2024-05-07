create index if not exists idx_lot_name_lots on lot(name_lots);

alter table bid drop constraint if exists bid_buyer_fk;
alter table bid add constraint bid_buyer_fk
    foreign key (buyer_id) references users(id_users) on delete cascade;

alter table bid drop constraint if exists bid_lot_fk;
alter table bid add constraint bid_lot_fk
    foreign key (lot_id) references lot(id_lots) on delete cascade;

alter table lot drop constraint if exists lot_seller_fk;
alter table lot add constraint lot_seller_fk
    foreign key (seller_id) references users(id_users) on delete cascade;

alter table lot drop constraint if exists lot_current_buyer_fk;
alter table lot add constraint lot_current_buyer_fk
    foreign key (current_buyer_id) references users(id_users) on delete set null;
