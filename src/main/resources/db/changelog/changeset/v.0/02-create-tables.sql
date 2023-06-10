 create table courier_regions (
       courier_id bigint not null,
        region_id integer not null
    ); 
    
    create table courier_working_hours (
       courier_id bigint not null,
        working_hours varchar(255) not null
    ); 
    
    create table couriers (
       id bigint not null,
        courier_type smallint not null,
        earnings integer,
        rating integer,
        primary key (id)
    ); 
    
    create table group_orders (
       id bigint not null,
        date date not null,
        courier_id bigint,
        primary key (id)
    ); 
    
    create table order_delivery_hours (
       order_id bigint not null,
        delivery_hours varchar(255) not null
    ); 
    
    create table orders (
       id bigint not null,
        assigned_time timestamp(6),
        completed_time timestamp(6),
        cost integer not null,
        weight float4 not null,
        group_order_id bigint,
        region_id integer,
        primary key (id)
    ); 
    
    create table regions (
       id integer not null check (id>=1 AND id<=2147483647),
        primary key (id)
    )
GO
