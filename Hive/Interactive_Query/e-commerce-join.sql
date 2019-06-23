drop table tmp28;
create table tmp28 as select bigdatabench_dw_order.buyer_id,  sum(bigdatabench_dw_item.goods_amount) as total from bigdatabench_dw_item join bigdatabench_dw_order on bigdatabench_dw_item.order_id = bigdatabench_dw_order.order_id group by bigdatabench_dw_order.buyer_id limit 10;
