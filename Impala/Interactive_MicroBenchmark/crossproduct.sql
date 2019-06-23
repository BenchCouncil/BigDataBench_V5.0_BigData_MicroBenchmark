insert into table result select * from bigdatabench_dw_item join bigdatabench_dw_order on bigdatabench_dw_item.order_id = bigdatabench_dw_order.order_id;
