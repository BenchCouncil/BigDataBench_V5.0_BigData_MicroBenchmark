drop table tmp32;
create table tmp32 as SELECT SUM(GOODS_NUMBER) FROM bigdatabench_dw_item;
