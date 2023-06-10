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

drop table if exists courier_regions cascade;

drop table if exists courier_working_hours cascade;

drop table if exists couriers cascade;

drop table if exists group_orders cascade;

drop table if exists order_delivery_hours cascade;

drop table if exists orders cascade;

drop table if exists regions cascade;

drop sequence if exists couriers_seq;

drop sequence if exists group_orders_seq;

drop sequence if exists orders_seq;

drop table if exists databasechangelog cascade;

drop table if exists databasechangeloglock cascade;

