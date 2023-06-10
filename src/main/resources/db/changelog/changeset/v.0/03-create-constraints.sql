--liquibase formatted sql

--changeset ivan:3
alter table if exists courier_regions
   add constraint FKgsc0ppq4f3nw5j14fl4t0ca62
   foreign key (region_id)
   references regions;

alter table if exists courier_regions
   add constraint FKro7s6ktu4okreaxi13hmvq47a
   foreign key (courier_id)
   references couriers;

alter table if exists courier_working_hours
   add constraint FK7no9r1x57jw9sg3j2ybvvwcj5
   foreign key (courier_id)
   references couriers;

alter table if exists group_orders
   add constraint FKe5hap6wv7qp3ypea10n06lpbr
   foreign key (courier_id)
   references couriers;

alter table if exists order_delivery_hours
   add constraint FKcqunfy5fxg64jhncqw9sflku7
   foreign key (order_id)
   references orders;

alter table if exists orders
   add constraint FKh1bmn22a83497t3itaom9ab1b
   foreign key (group_order_id)
   references group_orders;

alter table if exists orders
   add constraint FKdcd3f7yaq82k1gxpxfnajpgby
   foreign key (region_id)
references regions;

--rollback
alter table if exists courier_regions
   drop constraint if exists FKgsc0ppq4f3nw5j14fl4t0ca62;

alter table if exists courier_regions
   drop constraint if exists FKro7s6ktu4okreaxi13hmvq47a;

alter table if exists courier_working_hours
   drop constraint if exists FK7no9r1x57jw9sg3j2ybvvwcj5;

alter table if exists group_orders
   drop constraint if exists FKe5hap6wv7qp3ypea10n06lpbr;

alter table if exists order_delivery_hours
   drop constraint if exists FKcqunfy5fxg64jhncqw9sflku7;

alter table if exists orders
   drop constraint if exists FKh1bmn22a83497t3itaom9ab1b;

alter table if exists orders
   drop constraint if exists FKdcd3f7yaq82k1gxpxfnajpgby;


