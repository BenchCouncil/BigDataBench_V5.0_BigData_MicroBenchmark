insert into table item_temp select order_id from bigdatabench_dw_item;
insert into table result select BDO.*,TP.ORDER_ID as ORDER_IDA from bigdatabench_dw_order BDO left outer join item_temp TP on BDO.ORDER_ID=TP.ORDER_ID where TP.ORDER_ID is null;
