insert into table result select GOODS_ID, sum(GOODS_NUMBER) from bigdatabench_dw_item group by GOODS_ID;
