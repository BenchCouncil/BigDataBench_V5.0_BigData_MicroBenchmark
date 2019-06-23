drop table tmp33;
create table tmp33 as select DO.order_id as order_ida,DO.buyer_id,DO.create_date,DI.*  from bigdatabench_dw_item  DI join bigdatabench_dw_order  DO  on DI.order_id = DO.order_id;
