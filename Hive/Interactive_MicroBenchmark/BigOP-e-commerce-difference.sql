drop table tmp34;
create table tmp34 as select BDO.*,TP.ORDER_ID as ORDER_IDA from bigdatabench_dw_order BDO left outer join item_temp TP on BDO.ORDER_ID=TP.ORDER_ID where TP.ORDER_ID is null;
